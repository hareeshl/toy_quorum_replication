import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket client = null;
        PrintWriter out;
        BufferedReader in;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            String userInput = br.readLine();
            client = new Socket("127.0.0.1", 1000);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            out = new PrintWriter(client.getOutputStream(), true);

            if("bye".equals(userInput)) {
                break;
            }

            System.out.println("Writing to socket:" + userInput);

            out.println(userInput);

            if(userInput.startsWith("GET")) {
                String resp = in.readLine();
                if (resp != null) {
                    System.out.println(resp);
                }
            }
        }

        in.close();
        out.close();
        client.close();
    }

}
