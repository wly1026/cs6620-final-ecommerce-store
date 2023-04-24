package server;

import api.CartPaxosServer;
import api.CoordinatorInterface;
import common.ecommerce.Customer;
import common.ecommerce.Product;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;

public class ServerApp {
    private static final Logger LOG = Logger.getLogger("ServerApp.class");

    public static void main(String[] args) throws Exception {
        //String[] dummyargs = new String[]{"2000","2345","4567"};
        // 0 -> coordinator port; 1 -> server port, 2 -> recover server peer port (optional);
        if (args.length < 2) {
            throw new IllegalArgumentException("Enter the port numbers for the server and coordinator in command line: java ServerApp.java <coordinator port> <port#1> <port#recover - optional> ");
        }

        String hostname = "localhost";
        CoordinatorInterface coordinator;

        try{
            int coordinatorPort = Integer.parseInt(args[0]);
            coordinator = (CoordinatorInterface) Naming.lookup("rmi://" + hostname + ":" + coordinatorPort + "/coordinator");

            int port = Integer.parseInt(args[1]);
            CartPaxosServer server = new CartServer(port, hostname, Customer.copyInitCustomersMap(), Product.copyInitProductsMap());
            coordinator.addAcceptor(port, hostname);
            server.setCoordinator(coordinator);

            //if wants to restart
            if (args.length == 3){
                int peerPort = Integer.parseInt(args[2]);
                server.recover(peerPort, hostname);
            }

            LocateRegistry.createRegistry(port);
            Naming.bind("rmi://" + hostname + ":" + port + "/kvServer", server);
            LOG.info("server connect at port " + port);

        } catch (NumberFormatException e){
            LOG.warning(e.getMessage());
            LOG.warning("Invalid number input");
            System.exit(1);
        }
    }

//    public static void main(String[] args) throws Exception {
//        //String[] dummyargs = new String[]{"2000","2345","4567"};
//        // 0 -> coordinator port; 1 -> server port, 2 -> recover server peer port (optional);
//        String hostname = "localhost";
//        CoordinatorInterface coordinator;
//
//        try{
//            int coordinatorPort = 2000;
//            coordinator = (CoordinatorInterface) Naming.lookup("rmi://" + hostname + ":" + coordinatorPort + "/coordinator");
//
//            int[] ports = new int[]{1235, 1236, 1237, 1238, 1239};
//            for (int i = 0; i < 5; i++) {
//                int finalI = i;
//                Runnable r = () -> {
//                    CartPaxosServer server = null;
//                    try {
//                        server = new CartServer(ports[finalI], hostname, Customer.copyInitCustomersMap(), Product.copyInitProductsMap());
//                        coordinator.addAcceptor(ports[finalI], hostname);
//                        server.setCoordinator(coordinator);
//                        LocateRegistry.createRegistry(ports[finalI]);
//                        Naming.bind("rmi://" + hostname + ":" + ports[finalI] + "/kvServer", server);
//                        LOG.info("server connect at port " + ports[finalI]);
//                    } catch (Exception e){
//                        e.printStackTrace();
//                    }
//                };
//                new Thread(r).run();
//            }
//        } catch (NumberFormatException e){
//            LOG.warning(e.getMessage());
//            LOG.warning("Invalid number input");
//            System.exit(1);
//        }
//    }
}
