import util.RequestProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandProcessor {
    private final ServerSocket server;

    ConcurrentHashMap<String, String> db = new ConcurrentHashMap<>();

    private final RequestProcessor requestProcessor;

    public CommandProcessor(final ServerSocket serverSocket,
                            final RequestProcessor requestProcessor) {
        this.server = serverSocket;
        this.requestProcessor = requestProcessor;
    }

    public void run() {
        System.out.println("Setting up command processor for: " + server);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                //Start server socket
                Socket c = null;
                PrintWriter out = null;
                BufferedReader in = null;

                try {
                    System.out.println("Waiting for clients");

                    while (true) {
                        //Wait till registration complete with other servers
                        c = server.accept();

                        out = new PrintWriter(c.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(c.getInputStream()));

                        String inputLine = in.readLine();

                        System.out.println("Request received: " + inputLine);

                        if (inputLine.startsWith("REGISTRATION")) {
                            out.println("Registration request successful");
                            System.out.println("Registration request received from " + server);

                        } else if (inputLine.startsWith("GET")) {
                            String key = inputLine.split(" ")[1];
                            String value = db.get(key);
                            AtomicInteger quorumCount = new AtomicInteger(1);

                            for(int port: NodeRegistration.PORTS) {

                                //Message every other node in the cluster
                                if(port != server.getLocalPort()) {
                                    System.out.println("Sending fetch to " + port);

                                    Socket client = new Socket("127.0.0.1", port);
                                    String message = new StringBuilder()
                                            .append("FETCH")
                                            .append(" ")
                                            .append(key)
                                            .toString();

                                    NetworkManager.sendMessage(client, message);

                                    BufferedReader in1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                    String resp = in1.readLine();

                                    //Response returned from other node
                                    if(resp != null) {
                                        if(resp.equals(value)) quorumCount.getAndAdd(1);
                                    }
                                }
                            }

                            System.out.println(quorumCount + " :Nodes agree on value:" + value + " for key: " + key);

                            if(quorumCount.get() >= NodeRegistration.PORTS.size()/2) out.println(value);
                            else out.println("CONSISTENCY ERROR!!");

                        } else if (inputLine.startsWith("PUT")) {
                            String key = inputLine.split(" ")[1];
                            String value = inputLine.split(" ")[2];

                            db.put(key, value);

                            for(int port: NodeRegistration.PORTS) {
                                if(port != server.getLocalPort()) {
                                    System.out.println("Connecting to: " + port);

                                    Socket client = new Socket("127.0.0.1", port);
                                    String message = new StringBuilder()
                                            .append("PUBLISH")
                                            .append(" ")
                                            .append(key)
                                            .append(" ")
                                            .append(value)
                                            .toString();

                                    NetworkManager.sendMessage(client, message);
                                }
                            }

                        } else if (inputLine.startsWith("FETCH")) {
                            String key = inputLine.split(" ")[1];
                            String value = db.get(key);
                            out.println(value);

                        } else if (inputLine.startsWith("PUBLISH")) {
                            String key = inputLine.split(" ")[1];
                            String value = inputLine.split(" ")[2];

                            db.put(key, value);
                        }

                        System.out.println("Processing request complete");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        c.close();
                        out.close();
                        in.close();
                        server.close();
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
