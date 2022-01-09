import util.RequestProcessor;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public static void main(String[] args) throws IOException {
        new Server(Integer.parseInt(args[0])).run();
    }

    public void run() {
        System.out.println("Starting server in port:" + serverSocket.getLocalPort());

        new CommandProcessor(serverSocket,
                            new RequestProcessor()).run();
        //new NodeRegistration(this.port).run();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }
}
