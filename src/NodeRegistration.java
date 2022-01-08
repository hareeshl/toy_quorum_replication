import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class NodeRegistration implements Runnable {
    private int port;

    public static final int[] PORTS = {1500, 1501};

    public NodeRegistration(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        System.out.println("Starting node registration for: " + this.port);

        for(int port: this.PORTS) {
            if(this.port != port) {
                Socket client = null;
                try {
                    client = new Socket("127.0.0.1", port);
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    out.println("REGISTRATION");

                    String input= "";

                    while( (input = in.readLine()) != null) {
                        System.out.println("Response received for registration: " + input);
                        break;
                    }

                    System.out.println("Successfully sent reg req to " + port);
                } catch (ConnectException e) {
                    System.out.println("Error connecting to: " + port);
                    try {
                        System.out.println("Waiting for a bit");

                        Thread.sleep(100000);

                        client = new Socket("127.0.0.1", port);
                        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                        out.println("REGISTRATION");
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (UnknownHostException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
