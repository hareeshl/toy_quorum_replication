package startup;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/*
    This class should be responsible for things like.
    TODO: 1. Node registration: When a node comes up and wants to be a part of a cluster, it broadcasts it's ipaddr, port
                          to other nodes in the cluster.
 */
public class DummyNodeDiscoveryProcess implements StartupProcess {
    Logger logger = Logger.getLogger(this.getClass().getName());

    //Using this list to store list of hosts(in this example, ports) of all nodes in the cluster. This is mutated
    //as part of node discovery. Marking it public solely for the purpose of adjusting hosts during local testing.
    public static List<Integer> hosts = new ArrayList<>();
    public static String hostname = "127.0.0.1";

    @Override
    public void start() {
        logger.log(Level.INFO, "This class is a no-op node discovery implementation. Exists solely to demonstrate" +
                " how a mock object can be injected for testing. Does nothing useful");
    }

    @Override
    public void stop() {

    }
}
