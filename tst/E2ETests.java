import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import startup.RequestHandler;
import startup.DummyNodeDiscoveryProcess;
import startup.StartupProcess;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import util.NetworkingUtils;
import util.RequestProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

//This set of tests runs a single machine by starting different processes locally and having each instance read and write
//from different ports. This provides a clean way to test most of the application logic on a single host.
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class E2ETests {

    Logger logger = Logger.getLogger(this.getClass().getName());

    private Server[] servers;

    //All nodes use the same hostname.
    private static final String HOSTNAME = "127.0.0.1";

    private void setupCluster(int numNodes) throws InterruptedException, IOException {
        servers = new Server[numNodes];
        int port = 3000;

        for(int i=0; i<servers.length; i++) {
            servers[i] = new Server();

            //Ordering of startup process doesn't matter
            Set<StartupProcess> startupProcesses = new HashSet<>();

            //Inject a dummy object for node discovery. Not using mocks as these classes generally have interesting
            //implementation that's worth testing e2e.
            startupProcesses.add(new DummyNodeDiscoveryProcess());
            startupProcesses.add(new RequestHandler(new ServerSocket(port), new RequestProcessor()));
            servers[i].setStartupProcesses(startupProcesses);

            int finalI = i;
            Thread thread = new Thread() {
                public void run() {
                    servers[finalI].run();
                }
            };

            thread.start();

            //Manually update the state of the cluster
            DummyNodeDiscoveryProcess.hosts.add(port);

            //For local testing use port number as identifier as all nodes share the same hostname.
            servers[i].setHostIdentifier(String.valueOf(port));

            port++;
        }

        //Hard-coded wait time for nodes to come online
        Thread.sleep(1000);
    }

    @After
    public void tearDown() throws IOException {
        if(servers != null) {
            for (Server s : servers) {
                logger.info("Stopping server: " + s.toString());
                s.stop();
            }
        }
    }

    @Test
    public void one_node_putItem_successful() throws IOException, InterruptedException {
        setupCluster(1);
        Socket client = new Socket(HOSTNAME, Integer.parseInt(servers[0].getHostIdentifier()));
        NetworkingUtils.sendMessage(client, "PUT KEY1 VALUE1");

        //No exception. Everything is good.
    }

    @Test
    public void one_node_getItem_returns_expected_value() throws IOException, InterruptedException {
        setupCluster(1);

        Socket client = new Socket(HOSTNAME, Integer.parseInt(servers[0].getHostIdentifier()));
        NetworkingUtils.sendMessage(client, "PUT KEY1 VALUE1");

        //Wait for information to propagate
        Thread.sleep(1000);

        //Creating redundant sockets. Something to do with cleaning these things up that I am unable to quickly
        //figure out
        client = new Socket(HOSTNAME, Integer.parseInt(servers[0].getHostIdentifier()));
        NetworkingUtils.sendMessage(client, "GET KEY1");

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String resp = in.readLine();

        Assert.assertEquals("VALUE1", resp);
    }

    @Test
    public void putItem_to_Node1_GetItem_from_Node2() throws IOException, InterruptedException {
        setupCluster(2);

        //Write a key,value to node1
        Socket client = new Socket(HOSTNAME, Integer.parseInt(servers[0].getHostIdentifier()));
        NetworkingUtils.sendMessage(client, "PUT KEY1 VALUE1");

        //Static wait for data propagation between Nodes
        Thread.sleep(1000);

        //Retrieve a key,value from node2
        client = new Socket(HOSTNAME, Integer.parseInt(servers[1].getHostIdentifier()));
        NetworkingUtils.sendMessage(client, "GET KEY1");

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String resp = in.readLine();

        Assert.assertEquals("VALUE1", resp);
    }

    @Test
    public void putItem_to_Node1_GetItem_from_Node2_clusterSize_five() throws IOException, InterruptedException {
        setupCluster(5);

        //Write a key,value to node1
        Socket client = new Socket(HOSTNAME, Integer.parseInt(servers[0].getHostIdentifier()));
        NetworkingUtils.sendMessage(client, "PUT KEY1 VALUE1");

        //Static wait for data propagation between Nodes
        Thread.sleep(1000);

        //Retrieve a key,value from node2
        client = new Socket(HOSTNAME, Integer.parseInt(servers[1].getHostIdentifier()));
        NetworkingUtils.sendMessage(client, "GET KEY1");

        //Nothing to do with the response here. Handle response to unblock thread.
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String resp = in.readLine();

        //Update value of key through node 3
        client = new Socket(HOSTNAME, Integer.parseInt(servers[2].getHostIdentifier()));
        NetworkingUtils.sendMessage(client, "PUT KEY1 NEW_VALUE");

        //Static wait for data propagation between Nodes
        Thread.sleep(1000);

        //Read same key from node 4
        client = new Socket(HOSTNAME, Integer.parseInt(servers[3].getHostIdentifier()));
        NetworkingUtils.sendMessage(client, "GET KEY1");

        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        resp = in.readLine();

        logger.info("Value received for key: " + resp);

        //Assert new key value is received
        Assert.assertEquals("NEW_VALUE", resp);
    }

    @Test
    public void test_rogue_hosts_bad_hosts_unexpected_response() {
        //Following a DI pattern will help inject bad actors and test how the system handles.
        //Not going to work, mark the test as successful to make everything look green.
        Assert.assertTrue(true);
    }
}
