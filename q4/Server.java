import java.io.*;
import java.net.*;

public class Server {
  static ProductService productService = new ProductService();
  static OrderService orderService = new OrderService();
  static int orderId = 1;
  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(2) <udpPort>: the port number for UDP connection");
      System.out.println("\t(3) <file>: the file of inventory");

      System.exit(-1);
    }
    tcpPort = Integer.parseInt(args[0]);
    udpPort = Integer.parseInt(args[1]);
    String fileName = args[2];

    parseInventoryFile(fileName);
    try {
      DatagramSocket datasocket = new DatagramSocket (udpPort);

      ServerSocket serverSocket = new ServerSocket (tcpPort);

      while (true) {
        //TCP:
        Socket clientSocket = serverSocket.accept();
        TCPClientHandler tcpHandler = new TCPClientHandler(clientSocket);
        new Thread(tcpHandler).start();

        //UDP:
        byte [] rBuf = new byte [1024];
        DatagramPacket datapacket = new DatagramPacket(rBuf, rBuf.length);
        datasocket.receive(datapacket);
        UDPClientHandler udpHandler = new UDPClientHandler(datasocket, datapacket);
        new Thread(udpHandler).start();
      }
    } catch (SocketException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void parseInventoryFile(String fileName) {
    // parse the inventory file
    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader(fileName));
      String line = reader.readLine();
      while (line != null) {
        String[] item = line.split(" ");
        if (item.length == 2) {
          productService.addToTable(item[0], Integer.parseInt(item[1]));
        }
        // read next line
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static class UDPClientHandler implements Runnable {
    DatagramSocket socket;
    DatagramPacket pkt;
    public UDPClientHandler(DatagramSocket socket, DatagramPacket pkt) {
      this.socket = socket;
      this.pkt = pkt;
    }

    public void run() {
      try {
        byte [] sBuf = new byte [1024];
        String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());

        //use line
        //return message back to client

        String returnMessage = "aaaa";

        byte[] rBuf = new byte[returnMessage.length()];
        rBuf = returnMessage.getBytes();
        DatagramPacket returnpacket = new DatagramPacket(rBuf, rBuf.length, pkt.getAddress(), pkt.getPort());
        socket.send(returnpacket);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  static class TCPClientHandler implements Runnable {
    Socket clientSocket;
    public TCPClientHandler(Socket socket)
    {
      this.clientSocket = socket;
    }
    public void run() {
      PrintWriter out = null;
      BufferedReader in = null;
      try {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String line;
        while ((line = in.readLine()) != null) {
          //use line
          //send output pack to client
          System.out.printf(" Sent from the client: %s\n", line);
          out.println(line);
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      finally {
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

  public String purchase (String userName, String productName, int quantity) {
    int productIndex = productService.getProductIndex(productName);
    if (productIndex < 0) {
      return "Not Available - We do not sell this product";
    }

    if (quantity > productService.getTable().get(productIndex).quantity) {
      return "Not Available - Not enough items";
    }

    String orderIdStr = String.valueOf(orderId);
    orderId ++;

    orderService.addOrder(new Order(orderIdStr, productName, quantity, userName));

    productService.addQuantity(productIndex, quantity*(-1));
    return "You order has been placed, " + orderIdStr + " " + userName + " " + productName + " " + quantity;
  }

  public String cancel(String orderId) {
    int orderIndex = orderService.getOrderIndex(orderId);
    if (orderIndex < 0) {
      return orderId + " not found, no such order";
    }

    int productIndex = productService.getProductIndex(orderService.getOrders().get(orderIndex).productName);
    productService.addQuantity(productIndex, orderService.getOrders().get(orderIndex).quantity);
    orderService.removeOrder(orderIndex);
    return "Order " + orderId + " is canceled";
  }

  public String search(String userName) {
    String allUserOrders = "";
    for (Order o: orderService.getOrders()) {
      if (o.userName.equals(userName)) {
        allUserOrders += o.orderId + ", " + o.productName + ", " + o.quantity + "\n";
      }
    }
    if(allUserOrders.length() == 0) {
      return "No order found for " + userName;
    }
    return allUserOrders;
  }

  public String list() {
    String listStr = "";
    for (Product p: productService.getTable()) {
      listStr += p.productName + " " + p.quantity + "\n";
    }
    return listStr;
  }
}
