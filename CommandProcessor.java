import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandProcessor {
    private int port;

    Map<String, String> db = new HashMap<String, String>();

    public CommandProcessor(int port) {
        this.port = port;
    }

    public void run() {
        System.out.println("Setting up command processor for: " + port);

        ExecutorService pool = Executors.newFixedThreadPool(10);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                //Start server socket
                ServerSocket s = null;
                Socket c = null;
                PrintWriter out = null;
                BufferedReader in = null;

                try {
                    s = new ServerSocket(port);

                    System.out.println("Waiting for clients");

                    while (true) {
                        //Wait till registration complete with other servers
                        c = s.accept();

                        out = new PrintWriter(c.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(c.getInputStream()));

                        String inputLine = in.readLine();

                        System.out.println("Request received: " + inputLine);

                        if (inputLine.startsWith("REGISTRATION")) {
                            out.println("Registration request successful");
                            System.out.println("Registration request received from " + port);
                        } else if (inputLine.startsWith("GET")) {


                        } else if (inputLine.startsWith("PUT")) {
                            String key = inputLine.split(" ")[1];
                            String value = inputLine.split(" ")[2];

                            db.put(key, value);

                            for (int p : NodeRegistration.PORTS) {
                                if (port != p) {
                                    Socket client = new Socket("127.0.0.1", p);
                                    PrintWriter out1 = new PrintWriter(client.getOutputStream(), true);
                                    BufferedReader in1 = new BufferedReader(new InputStreamReader(client.getInputStream()));

                                    out1.println("PUBLISH key1 value1");

                                    String input = "";

                                    while ((input = in1.readLine()) != null) {
                                        System.out.println("Response received for publish: " + input);
                                        break;
                                    }

                                    System.out.println("Successfully sent reg req to " + port);
                                }
                            }

                        } else if (inputLine.startsWith("FETCH")) {

                        } else if (inputLine.startsWith("PUBLISH")) {
                            System.out.println("Responding to publish: " + inputLine);

                            String key = inputLine.split(" ")[1];
                            String value = inputLine.split(" ")[2];

                            db.put(key, value);
                        }

                        System.out.println("Procesing request complete");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        c.close();
                        out.close();
                        in.close();
                        s.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread serverThread = new Thread(task);
        serverThread.start();
    }
}
