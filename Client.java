import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket client = new Socket("127.0.0.1", 1500);
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            String userInput = br.readLine();
            if("bye".equals(userInput)) {
                break;
            }

            System.out.println("Writing to socket");

            out.println(userInput);
            String resp = in.readLine();
            if(resp != null) { System.out.println(resp); };
        }

        in.close();
        out.close();
        client.close();
    }

}
