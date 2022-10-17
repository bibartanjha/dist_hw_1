import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Client {
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    boolean usingTCP = true;

    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <hostAddress>: the address of the server");
      System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(3) <udpPort>: the port number for UDP connection");
      System.exit(-1);
    }

    hostAddress = args[0];
    tcpPort = Integer.parseInt(args[1]);
    udpPort = Integer.parseInt(args[2]);

    Set<String> possibleCommands = new HashSet<>(Arrays.asList("setmode", "purchase", "cancel", "search", "list"));

    Socket clientSocket = null;
    PrintWriter out = null;
    BufferedReader in = null;

    try {
      //UDP
      InetAddress ia = InetAddress.getByName(hostAddress);
      DatagramSocket dataSocket = new DatagramSocket();
      byte[] rbuffer = new byte[1024];

      //TCP
      clientSocket = new Socket(ia.getHostName(), tcpPort);
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      Scanner sc = new Scanner(System.in);
      while(sc.hasNextLine()) {
        String cmd = sc.nextLine();
        String[] tokens = cmd.split(" ");

        if (tokens[0].equals("setmode")) {
          if (tokens[1].equals("T")) {
            usingTCP = true;
            System.out.println("New communication is TCP");
          } else if (tokens[1].equals("U")) {
            usingTCP = false;
            System.out.println("New communication is UDP");
          }
        } else if (!possibleCommands.contains(tokens[0])) {
          System.out.println("ERROR: No such command");
        }
        else {
          if (usingTCP) { //TCP
            out.println(cmd);
            String resp = in.readLine();
            System.out.println(resp.replaceAll(";", "\n"));
          } else { //UDP
            byte[] buffer = new byte[cmd.length()];
            buffer = cmd.getBytes();
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            dataSocket.send(request);

            DatagramPacket response = new DatagramPacket(rbuffer, rbuffer.length);
            dataSocket.receive(response);
            String resp = new String(response.getData(), 0, response.getLength());

            System.out.println(resp.replaceAll(";", "\n"));
          }
        }
      }
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    } catch (SocketException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (out != null) {
          out.close();
        }
        if (in != null) {
          in.close();
          clientSocket.close();
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
