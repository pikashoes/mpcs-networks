

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.in;
import static java.lang.System.out;

/**
 * Created by pikashoes on 1/9/17.
 */
public class EchoClient
{
    public static void main(String[] args)
    {
        String address = args[0].split("=")[0];
        String portAdd = args[1].split("=")[0];

        String IP = "";
        int port = 0;

        if (address.equals("--serverIP"))
        {
            if (portAdd.equals("--serverPort"))
            {
                IP = args[0].split("=")[1];
                port = Integer.parseInt(args[1].split("=")[1]);
            }
            else
            {
                System.err.println("Correct format is: java EchoClient --serverIP=<hostname> --serverPort=<port>");
                System.exit(1);
            }
        }
        else if (address.equals("--serverPort"))
        {
            if (portAdd.equals("--serverIP"))
            {
                IP = args[1].split("=")[1];
                port = Integer.parseInt(args[0].split("=")[1]);
            }
            else
            {
                System.err.println("Correct format is: java EchoClient --serverIP=<hostname> --serverPort=<port>");
                System.exit(1);
            }
        }

        try
        {
            Socket s = new Socket(IP, port);
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(s.getInputStream()));
            PrintWriter out =
                    new PrintWriter(s.getOutputStream(), true);
            BufferedReader stdIn =
                    new BufferedReader(
                            new InputStreamReader(System.in));
            String userInput;
            while ((userInput = stdIn.readLine()) != null)
            {
                out.println(userInput);
                System.out.println("Echo: " + in.readLine());
            }

        } catch (IOException error) {
            System.err.println(error);
            System.exit(1);
        }
    }

}
