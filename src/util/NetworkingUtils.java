import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

//Utility class with methods to help with reading and writing data to sockets
public class NetworkManager {
    public static void sendMessage(final Socket node,
                                    final String message) throws IOException {

        PrintWriter out = new PrintWriter(node.getOutputStream(), true);

        out.println(message);
        out.flush();
    }
}