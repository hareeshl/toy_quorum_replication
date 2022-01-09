import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkManager {
    public static void sendMessage(final Socket node,
                                    final String message) throws IOException {

        PrintWriter out = new PrintWriter(node.getOutputStream(), true);

        out.println(message);
        out.flush();
    }

    private static String waitAndReceiveResponse(final Socket node) {
        return "";
    }
}