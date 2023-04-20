package Server;

import api.CoordinatorInterface;
import api.CartPaxosServer;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;

public class ServerApp {
    private static final Logger LOG = Logger.getLogger("ServerApp.class");

    public static void main(String[] args) throws Exception {
        //String[] dummyargs = new String[]{ "1234","2345","4567"};
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
            CartPaxosServer server = new CartServer(port, hostname);
            coordinator.addAcceptor(port, hostname);
            server.setCoordinator(coordinator);

            //if wants to restart
            if (args.length == 3){
                int peerPort = Integer.parseInt(args[2]);
                server.recover(peerPort, hostname);
            }

            LocateRegistry.createRegistry(port);
            Naming.bind("rmi://" + hostname + ":" + port + "/kvServer", server);
            LOG.info("Server connect at port " + port);

        } catch (NumberFormatException e){
            LOG.warning(e.getMessage());
            LOG.warning("Invalid number input");
            System.exit(1);
        }
    }
}
