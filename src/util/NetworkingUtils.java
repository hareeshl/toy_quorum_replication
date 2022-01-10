package util;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

//Utility class with methods to help with reading and writing data to sockets.
//Java's not so neat way to write utility methods (public static)
public class NetworkingUtils {
    public static void sendMessage(final Socket node,
                                    final String message) throws IOException {

        PrintWriter out = new PrintWriter(node.getOutputStream(), true);

        out.println(message);
        out.flush();
    }
}