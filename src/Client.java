import api.Cart;
import api.CartPaxosServer;
import common.Response.Response;
import common.ecommerce.Customer;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;

public class Client {
    private static final Logger LOG = Logger.getLogger("Client.class");
    private static final HashSet REQUEST = new HashSet(Arrays.asList("put", "get", "delete"));
    private static final Map<String, Customer> customers = new HashMap<>(){{
        put("Andy", new Customer(UUID.randomUUID(), "Andy"));
        put("Beta", new Customer(UUID.randomUUID(), "Beta"));
        put("Cindy", new Customer(UUID.randomUUID(), "Cindy"));
        put("Ella", new Customer(UUID.randomUUID(), "Ella"));
        put("Danial", new Customer(UUID.randomUUID(), "Danial"));
    }};
    public static void main(String[] args) throws Exception {
        //String[] dummyargs = new String[]{ "1234","2345","4567","5678","9999"};
        ArrayList<CartPaxosServer> servers = new ArrayList<>(5);

        if (args.length < 5) {
            throw new IllegalArgumentException("Enter the 5 port numbers in command line: java Client.java <port#1> <port#2> <port#3> <port#4> <port#5>");
        }

        String hostname = "localhost";

        try {
            for (int i = 0; i < 5; i++) {
                int port = Integer.parseInt(args[i]);
                servers.add((CartPaxosServer) Naming.lookup("rmi://" + hostname + ":" + port + "/kvServer"));
                System.out.printf("Cart Client initialized to server %d at port %d.%n", i, port);
            }
        }catch (NumberFormatException  e) {
            LOG.warning(e.getMessage());
            LOG.warning("Please start the Client again");
            System.exit(1);
        }

        // generate customers.

        // prepopulate value
        prePopulateValue(servers);
        Scanner scanner = new Scanner(System.in);
        Random rand = new Random();

        while(true) {
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
        String action = splitMessages[0];
        Response res;
        
        if (!REQUEST.contains(action) || splitMessages.length > 3 || splitMessages.length < 2){
            LOG.warning("Invalid request");
            return;
        }
        
        if (action.equals("put") && splitMessages.length != 3){
            LOG.warning("Invalid request");
            return;
        }
      
        String key = splitMessages[1];
        String value = null;
        KVOperation operation = null;

        switch (action) {
            case "put" -> {
                value = splitMessages[2];
                operation = KVOperation.Put;
            }
            case "get" -> operation = KVOperation.Get;
            case "delete" -> operation = KVOperation.Delete;
        }
        String output = server.handleClientRequest(id, operation, key, value);
        LOG.info(output);
    }


    private static void prePopulateValue(ArrayList<CartPaxosServer> servers) throws RemoteException{
        // 5 put
        parseInputMessage("Danial put jack 3", servers.get(0));
        parseInputMessage("234 put ally 29",  servers.get(1));
        parseInputMessage("put judy nilson", servers.get(2));
        parseInputMessage("put apple drink",  servers.get(3));
        parseInputMessage("put juice 9999.9", servers.get(4));

        // 5 get
        parseInputMessage("get jack",  servers.get(2));
        parseInputMessage("get ally",  servers.get(4));
        parseInputMessage("get judy",  servers.get(0));
        parseInputMessage("get apple", servers.get(1));
        parseInputMessage("get notexisted",  servers.get(3)); // get null

        // 5 delete
        parseInputMessage("delete jack",  servers.get(4));
        parseInputMessage("delete ally",  servers.get(0));
        parseInputMessage("delete judy",  servers.get(3));
        parseInputMessage("delete apple",  servers.get(2));
        parseInputMessage("delete notexisted",  servers.get(1)); // no exsited
    }
}
