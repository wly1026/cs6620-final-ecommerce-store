package Server;

import api.CoordinatorInterface;
import api.PaxosServer;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;

public class ServerApp {
    private static final Logger LOG = Logger.getLogger("ServerApp.class");

    public static void main(String[] args) throws Exception {
        //String[] dummyargs = new String[]{ "1234","2345","4567","5678","9999","2000" };
        // 0, 1, 2, 3, 4-> server port; 5 -> coordinator port
        if (args.length < 6) {
            throw new IllegalArgumentException("Enter the 6 port numbers in command line: java ServerApp.java <port#1> <port#2> <port#3> <port#4> <port#5> <coordinator port>");
        }

        String hostname = "localhost";
        CoordinatorInterface coordinator;

        try{
            int coordinatorPort = Integer.parseInt(args[5]);
            coordinator = (CoordinatorInterface) Naming.lookup("rmi://" + hostname + ":" + coordinatorPort + "/coordinator");
            for (int i = 0 ; i < 5 ; i++) {
                int port = Integer.parseInt(args[i]);
                PaxosServer server = new KVServer(port, hostname);
                coordinator.addAcceptor(port, hostname);
                server.setCoordinator(coordinator);
                LocateRegistry.createRegistry(port);
                Naming.bind("rmi://" + hostname + ":" + port + "/kvServer", server);
                LOG.info("Server connect at port " + port);
            }
        } catch (NumberFormatException e){
            LOG.warning(e.getMessage());
            LOG.warning("Invalid number input");
            System.exit(1);
        }
    }
}
