import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class E2ETests {

    private Server[] servers;

    private void setupTest(int numNodes) throws InterruptedException, IOException {
        servers = new Server[numNodes];
        int port = 1000;

        for(int i=0; i<servers.length; i++) {
            servers[i] = new Server(port);

            int finalI = i;
            Thread thread = new Thread() {
                public void run() {
                    servers[finalI].run();
                }
            };

            thread.start();
            NodeRegistration.PORTS.add(port);

            port++;
        }

        //Hard-coded wait time for node to come online
        Thread.sleep(1000);
    }

    @After
    public void tearDown() throws IOException {
        for(Server s: servers) {
            System.out.println("Stopping server: " + s.toString());
            s.stop();
        }
    }

    @Test
    public void one_node_putItem_successful() throws IOException, InterruptedException {
        setupTest(1);

        Socket client = null;
        PrintWriter out;
        BufferedReader in;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        client = new Socket("127.0.0.1", 1000);
        NetworkManager.sendMessage(client, "PUT KEY1 VALUE1");

        //No exception. Everything is good.
    }

    @Test
    public void one_node_getItem_successful() throws IOException, InterruptedException {
        setupTest(1);

        Socket client = new Socket("127.0.0.1", 1000);
        NetworkManager.sendMessage(client, "PUT KEY1 VALUE1");

        //Wait for information to propagate
        Thread.sleep(1000);

        //Creating redundant sockets. Something to do with cleaning these things up that I am unable to quickly
        //figure out
        client = new Socket("127.0.0.1", 1000);
        NetworkManager.sendMessage(client, "GET KEY1");

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String resp = in.readLine();

        if (resp != null) {
            System.out.println("Response received:" + resp);
        }

        Assert.assertEquals("VALUE1", resp);
    }

    @Test
    public void putItem_to_Node1_GetItem_from_Node2() throws IOException, InterruptedException {
        setupTest(2);

        Socket client = new Socket("127.0.0.1", 1000);
        NetworkManager.sendMessage(client, "PUT KEY1 VALUE1");

        //Static wait for data propagation between Nodes
        Thread.sleep(1000);

        client = new Socket("127.0.0.1", 1001);
        NetworkManager.sendMessage(client, "GET KEY1");

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String resp = in.readLine();

        if (resp != null) {
            System.out.println("Response received:" + resp);
        }

        Assert.assertEquals("VALUE1", resp);
    }
}
