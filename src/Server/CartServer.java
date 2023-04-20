package Server;

import api.CartPaxosServer;

import api.CoordinatorInterface;
import api.CartPaxosServer;
import common.*;
import common.Response.Response;
import common.Response.ResultState;
import common.ecommerce.*;
import common.ecommerce.Proposal;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;


public class CartServer extends UnicastRemoteObject implements CartPaxosServer {
    private static final Logger LOG = Logger.getLogger("CartServer.class");

    private int port;
    private String hostname;

    private Map<UUID, Customer> customers = new HashMap();
    private Map<String, Product> products = new HashMap<>();
    private long maxId;
    private Proposal acceptedProposal;
    private CoordinatorInterface coordinator;

    public CartServer(int port, String hostname) throws RemoteException {
        this.maxId = 0;
        this.port = port;
        this.hostname = hostname;
    }
    @Override
    public Response checkout(UUID customerId) throws RemoteException {
        Response response = new Response(customerId, CartOperation.CheckOut);
        int totalPrice = 0;

        Map<String, Integer> cart = this.customers.get(customerId).getCart();
        // check if the products in cart has enough stock
        for (String productName: cart.keySet()) {
            if (!this.products.containsKey(productName)) {
                LOG.info(String.format("Invalid product. Please check and try again."));
                response.setState(ResultState.PRODUCT_NOT_FOUND);
                return response;
            }
            Product product = products.get(productName);
            if (product.getStock() < cart.get(productName)) {
                LOG.info(String.format("Insufficient stock. Please check and try again."));
                response.setState(ResultState.INSUFFICIENT_STOCK);
                return response;
            }
            totalPrice += cart.get(productName) * product.getPrice();
        }

        // deduct the stock.
        for (String productName: cart.keySet()) {
            Product product = products.get(productName);
            product.deductStock(cart.get(productName));
        }

        // reset the cart.
        this.customers.get(customerId).resetCart();

        // set the total price.
        this.customers.get(customerId).setTotalPrice(totalPrice);

        response.setState(ResultState.SUCCESS);
        return response;
    }

    @Override
    public Response add(UUID customerId, String productName, int count) throws RemoteException {
        LOG.info(String.format("server %s perform ADD request for add product: %s's count with %d", this.port, productName, count));
        Response response = new Response(customerId, CartOperation.Add);

        Customer customer = customers.get(customerId);
        int productStock = products.get(productName).getStock();

        //Compare with the stock
        if (productStock < count){
            LOG.info(String.format("There is not enough stock to add product: %s.", productName));
            response.setState(ResultState.INSUFFICIENT_STOCK);
        }else{
            customer.addItem(productName, count);
            response.setState(ResultState.SUCCESS);
            int newCount = customer.getCart().get(productName);
            LOG.info(String.format("Updated cart: product: &s's count changed to: &d", productName, newCount));
            response.setAdditionalInfo(String.valueOf(newCount));
        }

        return response;
    }

    @Override
    public Response delete(UUID customerId, String productName, int count) throws RemoteException {
        LOG.info(String.format("server %s perform DELETE request for delete product: &s's count with %d", this.port, productName, count));
        Response response = new Response(customerId, CartOperation.Delete);

        Customer customer = customers.get(customerId);
        if(!customer.getCart().containsKey(productName)) {
            LOG.info(String.format("Product is not in cart. Please check and try again."));
            response.setState(ResultState.PRODUCT_NOT_FOUND);
            return response;
        }

        customer.deleteItem(productName, count);
        response.setState(ResultState.SUCCESS);
        int newCount;
        if(!customer.getCart().containsKey(productName)) {
            LOG.info(String.format("Product has been removed"));
            newCount = 0;
        } else {
            newCount = customer.getCart().get(productName);
        }
        LOG.info(String.format("Updated cart: product: &s's count changed to: &d", productName, newCount));
        response.setAdditionalInfo(String.valueOf(newCount));

        return response;
    }

    @Override
    public Response remove(UUID customerId, String productName) throws RemoteException {
        LOG.info(String.format("server %s perform REMOVE request for product: &s", this.port, productName));
        Response response = new Response(customerId, CartOperation.Remove);
        Customer customer = customers.get(customerId);
        Map<String, Integer> customerCart = customer.getCart();
        if(!customerCart.containsKey(productName)) {
            LOG.info(String.format("Product is not in cart. Please check and try again."));
            response.setState(ResultState.PRODUCT_NOT_FOUND);
            return response;
        }

        customerCart.remove(productName);
        LOG.info(String.format("Updated cart: product: &s has been removed from the cart.", productName));
        response.setState(ResultState.SUCCESS);
        return response;
    }

    @Override
    public Response get(UUID customerId) throws RemoteException {
        LOG.info(String.format("server %s perform GET request for all products in his/her cart", this.port));
        Response response = new Response(customerId, CartOperation.Get);

        Customer customer = customers.get(customerId);
        response.setAdditionalInfo(customer.printCart());
        response.setState(ResultState.SUCCESS);
        return response;
    }

    // phase 1 -- promise
    @Override
    public Message promise(Proposal proposal) throws RemoteException {
        LOG.info(String.format("Acceptor at port %d received PREPARE message for proposal with id %d.", port, proposal.getId()));
        return asyncExecution(PaxosState.PROMISE, proposal);
    }

    // phase 2 -- accept
    @Override
    public Message accept(Proposal proposal) throws RemoteException {
        LOG.info(String.format("Acceptor at port %d received ACCEPT message for proposal with id %d.", port, proposal.getId()));
        return asyncExecution(PaxosState.ACCEPT, proposal);
    }

    private Message asyncExecution(PaxosState phase, Proposal proposal){
        // set random failure
        if (Math.random() < 0.1){
            return null;
        }

        FutureTask<Message> futureTask = new FutureTask<Message>(new Callable<Message>() {
            @Override
            public Message call() {
                // ignore any proposal lower than maxId
                if (proposal.getId() < maxId){
                    return new Message(PaxosState.REJECT, null);
                }else{
                    if (phase.equals(PaxosState.PROMISE)){
                        // promise to ignore any request lower than this proposal id
                        maxId = proposal.getId();
                        // if has accepted anything, reply the accepted proposal with the maxId
                        if (acceptedProposal != null) {
                            return new Message(PaxosState.PROMISE, new Proposal(maxId, acceptedProposal.getRequest()));
                        } else {
                            //otherwise, reply with promise proposal
                            return new Message(PaxosState.PROMISE, proposal);
                        }
                    } else if(phase.equals(PaxosState.ACCEPT)) {
                        acceptedProposal = proposal;
                        return new Message(PaxosState.ACCEPT, acceptedProposal);
                    }
                }
                return null;
            }
        });

        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            executor.submit(futureTask);
            return futureTask.get(10, TimeUnit.SECONDS);
        }catch (Exception e){
            LOG.warning(e.getMessage());
        }
        return null;
    }

    // phase 3 -- learn
    @Override
    public Response learn(Proposal proposal) throws RemoteException {
        LOG.info(String.format("Learner at port %d received ACCEPT message for proposal with id %d.", port, proposal.getId()));
        Request request = proposal.getRequest();
        CartOperation operation = request.getOperation();
        return switch (operation) {
            case Add -> add(request.getCustomerId(), request.getProductName(), request.getCount());
            case Get -> get(request.getCustomerId());
            case Delete -> delete(request.getCustomerId(), request.getProductName(), request.getCount());
            case CheckOut -> checkout(request.getCustomerId());
            case Remove -> remove(request.getCustomerId(), request.getProductName());
        };
    }

    @Override
    public void recover(int peerPort, String peerHostname) throws RemoteException {
        try{
            CartPaxosServer peer = (CartPaxosServer) Naming.lookup("rmi://" + peerHostname + ":" + peerPort + "/kvServer");
            this.customers = peer.copyCustomersMap();
            this.products = peer.copyProductsMap();
            LOG.info(String.format("Recover from the live server at port %d.", peerPort));
        } catch (Exception e){
            LOG.warning(String.format("Failed to recover the server from the live server at port %d.", peerPort));
        }
    }

    @Override
    public Map<UUID, Customer> copyCustomersMap() throws RemoteException {
        Map<UUID, Customer> copy = new HashMap<>();
        for (Map.Entry<UUID, Customer> entry: this.customers.entrySet()){
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    @Override
    public Map<String, Product> copyProductsMap() throws RemoteException {
        Map<String, Product> copy = new HashMap<>();
        for (Map.Entry<String, Product> entry: this.products.entrySet()){
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    @Override
    public void setCoordinator(CoordinatorInterface coordinator) throws RemoteException {
        this.coordinator = coordinator;
    }

    @Override
    public int getPort() throws RemoteException {
        return this.port;
    }

    @Override
    public String handleClientRequest(UUID customerId, CartOperation operation, String key, int value) throws RemoteException {
        Response response = null;
        if (!this.customers.containsKey(customerId)){
            LOG.info(String.format("Invalid customer Id. Please check and try again."));
            response = new Response(customerId, ResultState.CUSTOMER_NOT_FOUND);
        } else {
            Request request = new Request(customerId, operation, key, value);
            Proposal proposal = Proposal.generateProposal(request);
            response = this.coordinator.execute(proposal);
        }

        if (response != null){
            return response.getMessage();

        }
        return "FAILED: There has no response";
    }
}
