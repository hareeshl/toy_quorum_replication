import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class E2ETests {

    private void setupTest(int numNodes) throws InterruptedException {
        Thread thread = new Thread() {
            public void run() {
                Server s = new Server(1000);
                s.run();
            }
        };

        thread.start();

        //Hard-coded timeout for server to come online
        Thread.sleep(1000);
    }

    @Test
    public void one_node_putItem_successful() throws IOException, InterruptedException {
        setupTest(1);

        System.out.println("Server started");

        Socket client = null;
        PrintWriter out;
        BufferedReader in;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        client = new Socket("127.0.0.1", 1000);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);

        out.println("PUT KEY1 VALUE1");
    }

    @Test
    public void one_node_getItem_successful() throws IOException, InterruptedException {

        Socket client = new Socket("127.0.0.1", 1000);
        NetworkManager.sendMessage(client, "GET KEY1");

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String resp = in.readLine();

        if (resp != null) {
            System.out.println("Response received:" + resp);
        }

    }

}
