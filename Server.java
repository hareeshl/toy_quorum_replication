public class Server {
    private int port;

    public Server(int PORT) {
        this.port = PORT;
    }

    public static void main(String[] args) {
        new Server(Integer.parseInt(args[0])).run();
    }

    public void run() {
        System.out.println("Starting server in port:" + port);

        new CommandProcessor(this.port).run();
        new NodeRegistration(this.port).run();
    }
}
