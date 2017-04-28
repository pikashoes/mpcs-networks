import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by pikashoes on 2/28/17.
 */
public class PingClient
{
    private static int seqNum;
    private static long time;
    private static long timeSent;

    public static void main(String[] args) throws Exception
    {
        // Not hard-coded, but initiated at first and replaced with given arguments
        int port = 8080;
        String ip = "127.0.0.1";
        int count = 0;
        int period = 0;
        int timeout = 0;
        int packetsReceived = 0;

        // Set up from args -- returns if wrong usage and does NOT continue with above values
        for (String arg : args)
        {
            String[] splitArg = arg.split("=");
            if (splitArg.length == 2 && splitArg[0].equals("--server_ip"))
            {
                ip = splitArg[1];
            } else if (splitArg.length == 2 && splitArg[0].equals("--server_port"))
            {
                port = Integer.parseInt(splitArg[1]);
            } else if (splitArg.length == 2 && splitArg[0].equals("--count"))
            {
                count = Integer.parseInt(splitArg[1]);
            } else if (splitArg.length == 2 && splitArg[0].equals("--period"))
            {
                period = Integer.parseInt(splitArg[1]);
            } else if (splitArg.length == 2 && splitArg[0].equals("--timeout"))
            {
                timeout = Integer.parseInt(splitArg[1]);
            } else {
                // Print out usage instructions
                System.out.println("Usage: java PingClient --server_ip=<server ip addr>\n" +
                        "--server_port=<server port>\n" +
                        "--count=<number of pings to send>\n" +
                        "--period=<wait interval>\n" +
                        "--timeout=<timeout>");
                return;
            }
        }

        // Server to ping
        InetAddress server = InetAddress.getByName(ip);

        // Create socket
        DatagramSocket socket = new DatagramSocket();

        // Start the sequence number at 1 (like in the practice test cases)
        seqNum = 1;
        ArrayList<Long> times = new ArrayList<>();

        // The start time before any packets are sent
        Date dateStart = new Date();
        long timeStart = dateStart.getTime();

        // First line
        System.out.println("PING " + ip);

        // Begin sending packets
        while (seqNum <= count)
        {
            Date date = new Date();
            timeSent = date.getTime();

            // String to print out:
            String string = "PING " + seqNum + " " + timeSent;
            byte[] bytes = new byte[1024];
            bytes = string.getBytes();

            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, server, port);

            // Send packet to server
            socket.send(datagramPacket);

            // Try receiving responses
            try
            {
                // Set timeout time to wait to receive reply
                socket.setSoTimeout(timeout);
                DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
                // Try to receive the response
                socket.receive(response);
                // If received, will continue. If not, will go to catch.

                // Timestamp
                date = new Date();
                long timeRec = date.getTime();

                time = timeRec - timeSent;
                times.add(time);
                printData(response);

                // Only increments when it doesn't go to the exception
                packetsReceived++;

            } catch (IOException e){
                // Do nothing here (print nothing)
            }

            seqNum++;

            // Makes the thread wait for the period
            Thread.sleep(period);
        }

        // The final time after all packets are sent
        Date dateEnd = new Date();
        long timeEnd = dateEnd.getTime();

        // Cumulative time (total)
        long totalTime = timeEnd - timeStart;

        // Calculating the packet loss
        long sum = 0;
        for (long item : times)
        {
            sum += item;
        }

        double loss = (1 - (double) packetsReceived/(seqNum-1))*100.0;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        // End calculations

        // ----------------
        // Final print out
        // ----------------
        System.out.println("--- " + ip + " ping statistics ---");
        System.out.println(seqNum - 1 + " transmitted, " + packetsReceived + " received, " + df.format(loss) + "% loss, time " + totalTime + " ms");
        if (times.size() != 0)
        {
            System.out.println("rtt min/avg/max = " + Collections.min(times) + "/" + sum/times.size() + "/" + Collections.max(times) + " ms");
        } else {
            System.out.println("rtt min/avg/max = 0/0/0");
        }

        socket.close();
    }


    /**
     * Prints ping data to standard output stream.
     * I used the PingServer.java file as base code for this.
     * @param request
     * @throws Exception
     */
    private static void printData(DatagramPacket request) throws Exception
    {
        // Obtain references to packet's array of bytes
        byte[] buf = request.getData();

        // Wrap bytes in byte array input stream so that you can read data as stream of bytes
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);

        // Wrap the byte array output stream in an input stream reader
        // So you can read the data as a stream of characters
        InputStreamReader isr = new InputStreamReader(bais);

        // Wrap input stream reader in buffered reader,
        // so you can read the character data a line at a time
        BufferedReader br = new BufferedReader(isr);

        // Message data is contained in a single line, so read this line.
        String line = br.readLine();

        System.out.println("PONG " + request.getAddress().getHostAddress() + ": seq=" + seqNum + " time=" + time + " ms");
    }
}




// ========================================================================
// Below is my attempt to use multithreading, but is not a part of my official
// project submission. See README for explanation.
// ========================================================================

// public class PingClient {
//    // Not hard-coded, but initiated at first and replaced with given arguments
//    private static int port = 8080;
//    private static String ip = "127.0.0.1";
//    private static int count = 0;
//    private static int period = 0;
//    private static int timeout = 0;
//    private static int seqNum = 1;
//
//    public static void main(String[] args) throws Exception
//    {
//        // Set up from args -- returns if wrong usage
//        for (String arg : args)
//        {
////            System.out.println(arg);
//            String[] splitArg = arg.split("=");
////            System.out.println(splitArg.length);
//            if (splitArg.length == 2 && splitArg[0].equals("--server_ip"))
//            {
//                ip = splitArg[1];
//            } else if (splitArg.length == 2 && splitArg[0].equals("--server_port"))
//            {
//                port = Integer.parseInt(splitArg[1]);
//            } else if (splitArg.length == 2 && splitArg[0].equals("--count"))
//            {
//                count = Integer.parseInt(splitArg[1]);
//            } else if (splitArg.length == 2 && splitArg[0].equals("--period"))
//            {
//                period = Integer.parseInt(splitArg[1]);
//            } else if (splitArg.length == 2 && splitArg[0].equals("--timeout"))
//            {
//                timeout = Integer.parseInt(splitArg[1]);
//            } else {
//                System.out.println("Usage: java PingClient --server_ip=<server ip addr>\n" +
//                        "--server_port=<server port>\n" +
//                        "--count=<number of pings to send>\n" +
//                        "--period=<wait interval>\n" +
//                        "--timeout=<timeout>");
//                return;
//            }
//        }
//
//        System.out.println("PING " + ip);
//        Date dateStart = new Date();
//        long timeStart = dateStart.getTime();
//
//        SendThread sendThread = new SendThread(ip, port, count);
//        sendThread.start();
//        long time = sendThread.getTimeSent();
//        ReceiveThread receiveThread = new ReceiveThread(port, timeout, time, count, seqNum);
//        receiveThread.start();
//
//        Date dateEnd = new Date();
//        long timeEnd = dateEnd.getTime();
//
//        long totalTime = timeEnd - timeStart;
//
//        ArrayList<Long> times = receiveThread.getTimes();
//        int packetsReceived = receiveThread.getPacketsReceived();
//
//        long sum = 0;
//        for (long item : times)
//        {
//            sum += item;
//        }
//
//        double loss = (1 - (double) packetsReceived/(seqNum-1))*100.0;
//        DecimalFormat df = new DecimalFormat();
//        df.setMaximumFractionDigits(2);
//
//        System.out.println("--- " + ip + " ping statistics ---");
//        System.out.println(seqNum - 1 + " transmitted, " + packetsReceived + " received, " + df.format(loss) + "% loss, time " + totalTime + " ms");
//        if (times.size() != 0)
//        {
//            System.out.println("rtt min/avg/max = " + Collections.min(times) + "/" + sum/times.size() + "/" + Collections.max(times) + " ms");
//        } else {
//            System.out.println("rtt min/avg/max = 0/0/0");
//        }
//
//    }
//
//
// }
//
//


//===

//class SendThread extends Thread
//{
//    private DatagramSocket socket;
//    private String server;
//    private boolean stopped = false;
//    private int port;
//    private int seqNum;
//    private InetAddress receiver;
//    private long timeSent;
//    private int count;
//
//    public SendThread(String address, int port, int count) throws Exception
//    {
//        this.server = address;
//        this.receiver = InetAddress.getByName(server);
//        this.port = port;
//        this.socket = new DatagramSocket();
//        this.socket.connect(receiver, port);
//        this.count = count;
//    }
//
//    public void end()
//    {
//        this.stopped = true;
//    }
//
//    public DatagramSocket getSocket()
//    {
//        return this.socket;
//    }
//
//    public long getTimeSent()
//    {
//        return this.timeSent;
//    }
//
//    public void run()
//    {
//        while (seqNum < count)
//        {
//            try {
//
//                Date dateStart = new Date();
//                timeSent = dateStart.getTime();
//
//                // String to print out:
//                String string = "PING " + seqNum + " " + timeSent;
//                byte[] bytes = new byte[1024];
//                bytes = string.getBytes();
//
//                // Create datagram packet for message to send
//                DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, receiver, port);
//
//                // No need to bind to port, just connect
////            socket = new DatagramSocket();
//                socket.send(datagramPacket);
//
//            } catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        }
//    }
//}
//
//class ReceiveThread extends Thread
//{
//    private DatagramSocket socket;
//    private int port;
//    private int timeout;
//    private long timeSent;
//    private int packetsReceived;
//    private boolean stopped = false;
//    private ArrayList<Long> times;
//    private int seqNum;
//    private int count;
//
//    public ReceiveThread(int port, int timeout, long timeSent, int count, int seqNum)
//    {
//        this.port = port;
//        this.timeout = timeout;
//        this.timeSent = timeSent;
//        this.packetsReceived = 0;
//        this.times = new ArrayList<>();
//        this.seqNum = seqNum;
//        this.count = count;
//
//    }
//
//    public void end()
//    {
//        this.stopped = true;
//    }
//
//    public ArrayList<Long> getTimes()
//    {
//        return times;
//    }
//
//    public int getPacketsReceived()
//    {
//        return this.packetsReceived;
//    }
//
//    public void run()
//    {
//        while(seqNum < count)
//        {
//            if (stopped)
//            {
//                return;
//            }
//
//            try
//            {
//                socket = new DatagramSocket(port);
//                // Set timeout time to wait to receive reply
//                socket.setSoTimeout(timeout);
//                DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
//                // Try to receive the response
//                socket.receive(response);
//                // If received, will continue. If not, will go to catch.
//
//                // Timestamp
//                Date date = new Date();
//                long timeRec = date.getTime();
//
//                long time = timeRec - timeSent;
//                times.add(time);
//                printData(response, time);
//
//                packetsReceived++;
//
//            } catch (Exception e)
//            {
//                e.printStackTrace();
//                System.out.println("Timeout on packet " + seqNum);
//            }
//
//            seqNum++;
//
//        }
//
//    }