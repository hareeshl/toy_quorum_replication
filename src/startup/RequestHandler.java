package startup;

import util.NetworkingUtils;
import util.RequestProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements StartupProcess {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private final ServerSocket server;
    private RequestProcessor requestProcessor;

    //In-memory map that stores the state on each host.
    ConcurrentHashMap<String, String> db = new ConcurrentHashMap<>();

    public RequestHandler(final ServerSocket serverSocket,
                          final RequestProcessor requestProcessor) {
        this.server = serverSocket;
        this.requestProcessor = requestProcessor;
    }

    public void run() {
        logger.log(Level.INFO, "Setting up request processor");

        Runnable task = new Runnable() {
            @Override
            public void run() {
                //Start server socket
                Socket c = null;
                PrintWriter out = null;
                BufferedReader in = null;

                try {
                    while (true) {
                        //Wait till registration complete with other servers
                        c = server.accept();

                        out = new PrintWriter(c.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(c.getInputStream()));

                        String inputLine = in.readLine();

                        logger.info("Request received: " + inputLine);

                        //Below is a quick implementation to handle supported API's. Maintainable way to do this
                        //would be to abstract out each of them into its own class. Future work!
                        if (inputLine.startsWith("REGISTRATION")) {
                            logger.info("Registration request received as part of discovery. No-op");

                        } else if (inputLine.startsWith("GET")) {
                            String key = inputLine.split(" ")[1];
                            String value = db.get(key);
                            AtomicInteger quorumCount = new AtomicInteger(1);

                            for(int port: DummyNodeDiscoveryProcess.hosts) {

                                //Message every other node in the cluster
                                if(port != server.getLocalPort()) {
                                    logger.info("Sending fetch to " + port);

                                    Socket client = new Socket(DummyNodeDiscoveryProcess.hostname, port);
                                    String message = new StringBuilder()
                                            .append("FETCH")
                                            .append(" ")
                                            .append(key)
                                            .toString();

                                    NetworkingUtils.sendMessage(client, message);

                                    BufferedReader in1 = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                    String resp = in1.readLine();

                                    //Response returned from other node.
                                    if(resp != null) {
                                        //Check the response on other node. If it matches response expected by current
                                        //node, update quorumCount.
                                        if(resp.equals(value)) quorumCount.getAndAdd(1);
                                    }
                                }
                            }

                           logger.info(quorumCount + ": Nodes agree on value:" + value + " for key: " + key);

                            //If atleast half of nodes in the cluster agree on the value for the key, we have a quorum,
                            //return value as response
                            if(quorumCount.get() >= DummyNodeDiscoveryProcess.hosts.size()/2) out.println(value);

                            //Unable to reach a quorum, return a error message.
                            //Ideally I want to have some resolution logic to ensure consistency.
                            else out.println("CONSISTENCY ERROR!!");

                        } else if (inputLine.startsWith("PUT")) {
                            String key = inputLine.split(" ")[1];
                            String value = inputLine.split(" ")[2];

                            db.put(key, value);

                            for(int port: DummyNodeDiscoveryProcess.hosts) {
                                if(port != server.getLocalPort()) {
                                    System.out.println("Connecting to: " + port);

                                    Socket client = new Socket(DummyNodeDiscoveryProcess.hostname, port);
                                    String message = new StringBuilder()
                                            .append("PUBLISH")
                                            .append(" ")
                                            .append(key)
                                            .append(" ")
                                            .append(value)
                                            .toString();

                                    NetworkingUtils.sendMessage(client, message);
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

                        logger.info("Processing request complete");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        c.close();
                        out.close();
                        in.close();
                    } catch (IOException e) {
                        //Being here is bad enough to crash and restart.
                        logger.log(Level.SEVERE, e.getMessage());
                        throw new RuntimeException("Error cleaning things!");
                    }
                }
            }
        };

        Thread serverThread = new Thread(task);
        serverThread.start();
    }

    @Override
    public void start() {
        run();
    }

    @Override
    public void stop() throws IOException {
        server.close();
    }
}
