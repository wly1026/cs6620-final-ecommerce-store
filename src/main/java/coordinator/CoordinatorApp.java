package coordinator;

import api.CoordinatorInterface;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;

public class CoordinatorApp {
    private static final Logger LOG = Logger.getLogger("CoordinatorApp.class");

    public static void main(String[] args) throws Exception {
        //String[] dummyargs = new String[]{ "2000" };
        // 0 -> coordinator port
        if (args.length != 1) {
            throw new IllegalArgumentException();
        }

        String hostname = "localhost";

        try {
            int coordinatorPort = Integer.parseInt(args[0]);

            CoordinatorInterface coordinator = new Coordinator();
            LocateRegistry.createRegistry(coordinatorPort);
            Naming.bind("rmi://" + hostname + ":" + coordinatorPort + "/coordinator", coordinator);
            System.out.println("coordinator connect at port " + coordinatorPort);
        } catch (NumberFormatException e){
            LOG.warning(e.getMessage());
            LOG.warning("Invalid number input");
            System.exit(1);
        }
    }
}
