package Client;

import api.Cart;
import api.CartPaxosServer;
import common.Response.Response;
import common.ecommerce.CartOperation;
import common.ecommerce.Customer;
import common.ecommerce.Product;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;

public class Client {
    private static final Logger LOG = Logger.getLogger("Client.Client.class");
    private static final HashSet REQUEST = new HashSet(Arrays.asList("add", "get", "delete", "remove", "checkout"));

    public static void main(String[] args) throws Exception {
        //String[] dummyargs = new String[]{ "1234","2345","4567","5678","9999"};
        ArrayList<CartPaxosServer> servers = new ArrayList<>(5);

        if (args.length < 6) {
            throw new IllegalArgumentException("Enter init option and the 5 port numbers in command line: java Client.Client.java <true|false> <port#1> <port#2> <port#3> <port#4> <port#5>");
        }

        String hostname = "localhost";

        boolean isInit = args[0].equals("true");

        try {
            for (int i = 1; i < 6; i++) {
                int port = Integer.parseInt(args[i]);
                servers.add((CartPaxosServer) Naming.lookup("rmi://" + hostname + ":" + port + "/kvServer"));
                System.out.printf("Cart Client.Client initialized to server %d at port %d.%n", i, port);
            }
        }catch (NumberFormatException  e) {
            LOG.warning(e.getMessage());
            LOG.warning("Please start the Client.Client again");
            System.exit(1);
        }

        // prepopulate value
        if (isInit) {
            prePopulateValue(servers);
        }

        Scanner scanner = new Scanner(System.in);
        Random rand = new Random();

        while(true) {
            Product.printProducts();
            System.out.println("Enter a string to send to server");
            String message = scanner.nextLine();
            int rand_server = rand.nextInt(5);
            if (message.isBlank() || message.isEmpty() || message.trim().equals("bye")) {
                LOG.info("Connection close");
                return;
            }
            parseInputMessage(message, servers.get(rand_server));
        }
    }

    private static void parseInputMessage(String message, CartPaxosServer server) throws RemoteException {
        String[] splitMessages = message.split(" ");
        String userName = splitMessages[0];
        int id = Customer.getIDByName(userName);
        String action = splitMessages[1];

        if (!REQUEST.contains(action) || splitMessages.length > 4 || splitMessages.length < 2){
            LOG.warning("Invalid request");
            return;
        }
        
        if ((action.equals("add") || action.equals("delete")) && splitMessages.length != 4){
            LOG.warning("Invalid request");
            return;
        }
      
        String key = null;
        int value = 0;
        CartOperation operation = null;

        switch (action) {
            case "add" -> {
                key = splitMessages[2];
                value = Integer.parseInt(splitMessages[3]);
                operation = CartOperation.Add;
            }
            case "delete" -> {
                key = splitMessages[2];
                value = Integer.parseInt(splitMessages[3]);
                operation = CartOperation.Delete;
            }
            case "remove" -> {
                key = splitMessages[2];
                operation = CartOperation.Remove;
            }
            case "checkout" -> operation = CartOperation.CheckOut;
            case "get" -> operation = CartOperation.Get;
        }
        String output = server.handleClientRequest(id, operation, key, value);
        LOG.info(output);
    }


    private static void prePopulateValue(ArrayList<CartPaxosServer> servers) throws RemoteException{
        // Populate data for the 5 customers
        String[] addCmd = new String[]{"add apple 3", "add shirt 2", "add book 11", "add watermelon 2", "add juice 33"};
        for (int id: Customer.customers.keySet()) {
            String name = Customer.getNameByID(id);
            for (int i = 0; i < addCmd.length; i++) {
                int randomServer = (int) Math.random() * 5;
                parseInputMessage(name + " " + addCmd[i], servers.get(randomServer));
            }
        }

        // Get the info of 5 carts
        for (int id: Customer.customers.keySet()) {
            String name = Customer.getNameByID(id);
            int randomServer = (int) Math.random() * 5;
            parseInputMessage(name + " get", servers.get(randomServer));
        }
        parseInputMessage("nonexistence get",  servers.get(3)); // get null

        // Randomly delete/remove some items
        parseInputMessage("Andy delete apple 1",  servers.get(4));
        parseInputMessage("Beta delete shirt 1",  servers.get(0));
        parseInputMessage("Cindy remove book",  servers.get(3));
        parseInputMessage("Ella remove apple",  servers.get(2));
        parseInputMessage("Danial delete invalid_product",  servers.get(1)); // no exsited

        // Get the info of 5 carts
        for (int id: Customer.customers.keySet()) {
            String name = Customer.getNameByID(id);
            int randomServer = (int) Math.random() * 5;
            parseInputMessage(name + " get", servers.get(randomServer));
        }
    }
}
