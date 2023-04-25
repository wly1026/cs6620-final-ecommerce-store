package server;

import api.CoordinatorInterface;
import api.CartPaxosServer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import common.*;
import common.Response.Response;
import common.Response.ResultState;
import common.ecommerce.*;
import common.ecommerce.Proposal;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

//Producer of MQ

public class CartServer extends UnicastRemoteObject implements CartPaxosServer {
    private static final Logger LOG = Logger.getLogger("CartServer.class");
    private static final String TASK_QUEUE_NAME = "task_queue";

    private int port;
    private String hostname;

    private Map<Integer, Customer> customers;
    private Map<String, Product> products;
    private long maxId;
    private Proposal acceptedProposal;
    private CoordinatorInterface coordinator;

    public CartServer(int port, String hostname, Map<Integer, Customer> customers, Map<String, Product> products) throws RemoteException {
        this.maxId = 0;
        this.port = port;
        this.hostname = hostname;
        this.products = products;
        this.customers = customers;
    }

    @Override
    public Response checkout(int customerId) throws RemoteException {
        LOG.info(String.format("server %s perform CHECKOUT request", this.port));
        Response response = new Response(customerId, CartOperation.CheckOut, "");
        int totalPrice = 0;

        Map<String, Integer> cart = this.customers.get(customerId).getCart();

        // check if the products in cart has enough stock
        for (String productName: cart.keySet()) {
            if (!this.products.containsKey(productName)) {
                LOG.info(String.format("Invalid product. Please check and try again."));
                response.setAdditionalInfo(productName);
                response.setState(ResultState.PRODUCT_NOT_FOUND);
                return response;
            }
            Product product = products.get(productName);
            if (product.getStock() < cart.get(productName)) {
                LOG.info(String.format("Insufficient stock. Please check and try again."));
                response.setAdditionalInfo(productName);
                response.setState(ResultState.INSUFFICIENT_STOCK);
                return response;
            }
            totalPrice += cart.get(productName) * product.getPrice();
        }

        // deduct the stock. (work done in checkoutserver after pushing the customer id to mq)
//        for (String productName: cart.keySet()) {
//            Product product = products.get(productName);
//            product.deductStock(cart.get(productName));
//        }

        // set the total price.
        this.customers.get(customerId).setTotalPrice(totalPrice);

        response.setState(ResultState.SUCCESS);
        response.setAdditionalInfo("Total price: " + totalPrice + ", Cart: " + this.customers.get(customerId).printCart());

        // reset the cart.
        this.customers.get(customerId).resetCart();

        return response;
    }

    private void produceMessage(String customerId) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

            channel.basicPublish("", TASK_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    customerId.getBytes("UTF-8"));
            System.out.println(" [x] Sent checkout request for customer: '" + customerId + "'");
        }
    }

    @Override
    public Response add(int customerId, String productName, int count) throws RemoteException {
        LOG.info(String.format("server %s perform ADD request for add product: %s's count with %d", this.port, productName, count));
        Response response = new Response(customerId, CartOperation.Add, productName);

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
            LOG.info(String.format("Updated cart: product: %s's count changed to: %d", productName, newCount));
            response.setAdditionalInfo(String.valueOf(newCount));
        }

        return response;
    }

    @Override
    public Response delete(int customerId, String productName, int count) throws RemoteException {
        LOG.info(String.format("server %s perform DELETE request for delete product: %s's count with %d", this.port, productName, count));
        Response response = new Response(customerId, CartOperation.Delete, productName);

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
        LOG.info(String.format("Updated cart: product: %s's count changed to: %d", productName, newCount));
        response.setAdditionalInfo(String.valueOf(newCount));

        return response;
    }

    @Override
    public Response remove(int customerId, String productName) throws RemoteException {
        LOG.info(String.format("server %s perform REMOVE request for product: %s", this.port, productName));
        Response response = new Response(customerId, CartOperation.Remove, productName);
        Customer customer = customers.get(customerId);
        Map<String, Integer> customerCart = customer.getCart();
        if(!customerCart.containsKey(productName)) {
            LOG.info(String.format("Product is not in cart. Please check and try again."));
            response.setState(ResultState.PRODUCT_NOT_FOUND);
            return response;
        }

        customerCart.remove(productName);
        LOG.info(String.format("Updated cart: product: %s has been removed from the cart.", productName));
        response.setState(ResultState.SUCCESS);
        return response;
    }

    @Override
    public Response get(int customerId) throws RemoteException {
        LOG.info(String.format("server %s perform GET request for all products in his/her cart", this.port));
        Response response = new Response(customerId, CartOperation.Get, "");

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
            LOG.warning(String.format("Failed to recover the server from the live server at port %d, error: %s", peerPort, e.getMessage()));
        }
    }

    @Override
    public Map<Integer, Customer> copyCustomersMap() throws RemoteException {
        Map<Integer, Customer> copy = new HashMap<>();
        for (Map.Entry<Integer, Customer> entry: this.customers.entrySet()){
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
    public String handleClientRequest(int customerId, CartOperation operation, String productName, int count) throws RemoteException {
        Response response = null;
        if (!this.customers.containsKey(customerId)){
            LOG.info(String.format("Invalid customer Id. Please check and try again."));
            response = new Response(customerId, operation, productName);
        } else {
            Request request = new Request(customerId, operation, productName, count);
            Proposal proposal = Proposal.generateProposal(request);
            response = this.coordinator.execute(proposal);
        }

        if (operation.equals(CartOperation.CheckOut) && response.getState().equals(ResultState.SUCCESS)){
            // publish the customer id to the consumer
            System.out.println();
            try{
                produceMessage(String.valueOf(customerId));
            } catch (Exception e){
                e.getMessage();
            }

        }
        if (response != null){
            return response.getMessage();

        }
        return "FAILED: There has no response";
    }
}
