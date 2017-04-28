import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by pikashoes on 1/9/17.
 */
public class EchoServer
{
    private ServerSocket serv;
    public EchoServer(int port)
    {
        try
        {
            serv = new ServerSocket(port);
        }
        catch (Exception error)
        {
            System.out.println(error);
        }
    }

    public void serve()
    {
        try
        {
            while (true)
            {
                Socket echoClient = serv.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(echoClient.getInputStream()));
                PrintWriter out = new PrintWriter(echoClient.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    System.out.println(inputLine);
                    out.println(inputLine);
                }
            }
        }
        catch (Exception error)
        {
            System.err.println(error);
        }
    }

    public static void main(String[] args)
    {
        int port = 0;
        if (args[0].split("=")[0].equals("--port"))
        {
            port = Integer.parseInt(args[0].split("=")[1]);
        }
        else
        {
            System.err.println("Correct format is: java EchoServer --port=<port>");
            System.exit(1);
        }
        EchoServer s = new EchoServer(port);

        s.serve();
    }

}
