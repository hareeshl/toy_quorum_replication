import startup.StartupProcess;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    Logger logger = Logger.getLogger(Server.class.getName());

    private ServerSocket serverSocket;

    //All threads/proceses that needs to be kicked-off as part of starting a server.
    private Set<StartupProcess> startupProcesses;

    public Server() {
    }

    //Overloaded constructor to instantiate from terminal
    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            //If there are any errors with startup, fail straight away
            logger.log(Level.SEVERE, "Error starting server", e.getCause());
            throw new RuntimeException(e);
        }
    }

    public String getHostIdentifier() {
        return hostIdentifier;
    }

    private String hostIdentifier;


    //Below setters are only for dependency injection purposes and allow flexibility with testing
    public void setStartupProcesses(Set<StartupProcess> startupProcesses) {
        this.startupProcesses = startupProcesses;
    }

    //This would be a unique id to identify the host. FQDN or IpAddr are good candidates.
    public void setHostIdentifier(String hostIdentifier) {
        this.hostIdentifier = hostIdentifier;
    }

    public static void main(String[] args) throws IOException {
        new Server(Integer.parseInt(args[0])).run();
    }

    public void run() {
        logger.log(Level.INFO, "Starting server:" + this.hostIdentifier);

        for(StartupProcess startupProcess: this.startupProcesses) {
            startupProcess.start();
        }
    }

    public void stop() throws IOException {
        for(StartupProcess startupProcess: this.startupProcesses) {
            startupProcess.stop();
        }
    }
}
