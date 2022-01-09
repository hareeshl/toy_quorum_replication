import threads.Bootstrap;
import util.RequestProcessor;

public class Server {
    private int port;

    private Bootstrap bootstrap;

    public Server(int PORT) {
        this.port = PORT;
    }

    public static void main(String[] args) {
        new Server(Integer.parseInt(args[0])).run();
    }

    public void run() {
        System.out.println("Starting server in port:" + port);

        new CommandProcessor(this.port,
                                new RequestProcessor()).run();
        //new NodeRegistration(this.port).run();
    }
}
