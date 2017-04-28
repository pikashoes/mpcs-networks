import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by pikashoes on 2/1/17.
 */
public class WebServer
{
    private static HashMap<String, String> redirects; // Stores redirect path with corresponding urls

    /**
     * Main  method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception
    {

        if (args.length != 1)
        {
            System.err.println("Usage: java WebServer --serverPort=<port number>");
            return;
        }

        String flag = args[0].split("=")[0];

        if (!flag.equals("--serverPort"))
        {
            System.err.println("Usage: java WebServer --serverPort=<port number>");
            return;
        }

        String port = args[0].split("=")[1];

        // Starting the Server
        ServerSocket s;

        System.out.println("Webserver starting on port " + port);
        System.out.println("Press Ctrl-C to exit.");

        // Put into HashMap
        redirect("www/redirect.defs");

        // Open socket
        try
        {
            s = new ServerSocket(Integer.parseInt(port));

            // Writer/Readers
            while (true)
            {
                try
                {
                    // Reading in from socket, and outputting to it. (Client)
                    Socket remote = s.accept(); // remote = connected socket
                    // Wait for connection
                    BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
                    DataOutputStream out = new DataOutputStream(remote.getOutputStream());
                    System.out.println("Connection Successful!");

                    // Do the actual request
                    do_http(in, out);

                    // Close everything
                    System.out.println("Closing connection");
                    out.close();
                    in.close();
                    remote.close();

                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return; }
    }
    
    /**
     * Initializes the HashMap redirects with the redirect path name corresponding to the redirect URL
     * @param file
     * @throws IOException
     */
    private static void redirect(String file) throws IOException
    {
        redirects = new HashMap<>();

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            String[] split = line.split(" ");
//            System.out.println(split[0] + ": " + split[1]);
            redirects.put(split[0], split[1]);
        }

    }

    /**
     * Conducts the actual request
     * @param in
     * @param out
     * @throws IOException
     */
    private static void do_http(BufferedReader in, DataOutputStream out) throws IOException
    {
        String method = "GET";
        int type;

        // Read in the header
        // First line looks something like: GET /directoryname/page.html HTTP/1.1
        String header = in.readLine();

        if (header == null)
        {
            System.out.println("No input");
            return;
        }

        // Discard the rest
        String remainder;
        do
        {
            remainder = in.readLine();
        } while (remainder != null && remainder.length() > 0);

        // Parse the header: [GET, /directoryname/page.html, HTTP/1.1]
        String[] headerSplit = header.split(" ");

        String request = headerSplit[0]; // GET (or HEAD)
        String path = headerSplit[1]; // //directoryname/page.html
        String protocol = headerSplit[2]; // HTTP/1.1

// ==========================================================================================
//          DEBUGGING
//        System.out.println("Request: " + request);
//        System.out.println("Path: " + path);
//        System.out.println("Protocol: " + protocol);
// ==========================================================================================

        out.writeUTF(protocol);

        // Ensure it's only GET or HEAD
        if (request.equals("GET"))
        {
            method = "GET";
        }
        else if (request.equals("HEAD"))
        {
            method = "HEAD";
        }
        else
        {
            out.writeBytes(createHeader(403, 0, ""));
            return;
        }

        // Examine the path for redirects
        if (path.equals("/redirect.defs"))
        {
            out.writeBytes(createHeader(404, 0, ""));
            return;
        }

        // If there is a redirect, redirect path is added and we return
        String redirectTo = redirects.get(path);
        if (redirectTo != null)
        {
            out.writeBytes(createHeader(301, 0, redirectTo));
            return;
        }

        // Examine the type of file (not redirects)
        FileInputStream fS;
        try
        {
            fS = new FileInputStream("www" + path);
        } catch (IOException e) {
            fS = null;
        }

        if (fS == null)
        {
            out.writeBytes(createHeader(404, 0, ""));
            return;
        }

        // Switch type based on type of file
        if (path.endsWith(".txt"))
        {
            type = 1;
        } else if (path.endsWith(".html")) {
            type = 2;
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            type = 3;
        } else if (path.endsWith(".png")) {
            type = 4;
        } else if (path.endsWith(".pdf")) {
            type = 5;
        } else {
            type = 1;
        }

        // Create header that will come after HTTP/1.1
        out.writeBytes(createHeader(200, type, ""));

        // If HEAD, no data added. If GET, then output data as well.
        if (method.equals("HEAD")) {
            fS.close();
        }
        else if (method.equals("GET"))
        {
            byte[] buffer = new byte[1000];
            int len;

            // Read file in
            while ((len = fS.read(buffer)) >= 1)
            {
                out.write(buffer, 0, len);
            }

        }

        fS.close();
        out.flush();
    }

    /**
     * Creates header based on the status code, file type, and redirect url ("" if none).
     * @param code
     * @param fileType
     * @param url
     * @return
     * @throws IOException
     */
    private static String createHeader(int code, int fileType, String url) throws IOException
    {

// ==========================================================================================
//          DEBUGGING
//        System.out.println("--------------");
//        System.out.println("CODE: " + code);
//        System.out.println("FILE TYPE: " + fileType);
//        System.out.println("URL: " + url);
// ==========================================================================================

        String pre = " ";
        Date date = new Date();

        // Status code
        switch(code)
        {
            case 200:
                pre += "200 OK";
                break;
            case 301:
                pre += "301 Moved Permanently\r\n";
                pre += "Location: " + url;
                break;
            case 400:
                pre += "400 Bad Request";
                break;
            case 403:
                pre += "403 Forbidden";
                break;
            case 404:
                pre += "404 Not Found";
                break;
        }

        // New line, and then add date, connection, and server
        pre += "\r\n";
        pre += "Date: " + date + "\r\n";
        pre += "Connection: close\r\n";
        pre += "Server: WebServer\r\n";

        // Add content type (if applicable)
        switch (fileType) {
            case 0:
                break;
            case 1:
                pre += "Content-Type: text/plain\r\n";
            case 2:
                pre += "Content-Type: text/html\r\n";
                break;
            case 3:
                pre += "Content-Type: image/jpeg\r\n";
                break;
            case 4:
                pre += "Content-Type: image/png\r\n";
                break;
            case 5:
                pre += "Content-Type: application/pdf\r\n";
        }

        pre +=  "\r\n"; //Space between content
        return pre;
    }

}
