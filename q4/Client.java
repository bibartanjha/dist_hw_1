import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

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

    byte[] rbuffer = new byte[1024];
    DatagramPacket sPacket, rPacket;

    try {
      InetAddress ia = InetAddress.getByName(hostAddress);

      DatagramSocket datasocket = new DatagramSocket();

      Socket clientSocket = new Socket(ia.getHostName(), tcpPort);
      PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

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
        }

        else if (tokens[0].equals("purchase")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("cancel")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("search")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("list")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else {
          System.out.println("ERROR: No such command");
        }


        if (usingTCP) {
          out.println(cmd);

          String resp = in.readLine();
          System.out.println(resp);

        } else {
          byte[] buffer = new byte[cmd.length()];
          buffer = cmd.getBytes();
          sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
          datasocket.send(sPacket);

          rPacket = new DatagramPacket(rbuffer, rbuffer.length);
          datasocket.receive(rPacket);
          String resp = new String(rPacket.getData(), 0, rPacket.getLength());

          System.out.println(resp);
        }
      }
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    } catch (SocketException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
