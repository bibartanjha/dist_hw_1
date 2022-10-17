import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    tcpThread(tcpPort);
    udpThread(udpPort);
  }

  public static void tcpThread(int tcpPort) {
    new Thread(() -> {
      ExecutorService executor = null;
      try (ServerSocket serverSocket = new ServerSocket (tcpPort)) {
        executor = Executors.newFixedThreadPool(5);
        while(true) {
          Socket clientSocket = serverSocket.accept();
          executor.execute(() -> {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
              out = new PrintWriter(clientSocket.getOutputStream(), true);
              in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

              String line;
              while ((line = in.readLine()) != null) {
                String returnMessage = implementClientMessage(line);
                out.println(returnMessage);
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
          });
        }
      } catch (IOException e) {
        System.err.println("Cannot open the port on TCP");
        e.printStackTrace();
      } finally {
        System.out.println("Closing TCP server");
        if (executor != null) {
          executor.shutdown();
        }
      }
    }).start();
  }

  public static void udpThread(int udpPort) {
    new Thread(() -> {
      try (DatagramSocket dataSocket = new DatagramSocket(udpPort)) {
        byte[] buf = new byte[dataSocket.getReceiveBufferSize()];
        DatagramPacket dataPacket = new DatagramPacket(buf, buf.length);

        while(true) {
          dataSocket.receive(dataPacket);
          String messageFromClient = new String(dataPacket.getData(), 0, dataPacket.getLength());

          String returnMessage = implementClientMessage(messageFromClient);

          byte[] returnMessageBuf = new byte[returnMessage.length()];
          returnMessageBuf = returnMessage.getBytes();
          DatagramPacket returnpacket = new DatagramPacket(returnMessageBuf, returnMessageBuf.length, dataPacket.getAddress(), dataPacket.getPort());
          dataSocket.send(returnpacket);
        }
      } catch (SocketException e) {
        System.err.println("Cannot open the port on UDP");
        e.printStackTrace();
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        System.out.println("Closing UDP server");
      }
    }).start();
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

  public static String implementClientMessage(String clientMessage) {
    String[] tokens = clientMessage.split(" ");

    if (tokens[0].equals("purchase")) {
      return purchase(tokens[1], tokens[2], Integer.parseInt(tokens[3]));
    } else if (tokens[0].equals("cancel")) {
      return cancel(tokens[1]);
    } else if (tokens[0].equals("search")) {
      return search(tokens[1]);
    } else if (tokens[0].equals("list")) {
      return list();
    }
    return "";
  }

  public static String purchase(String userName, String productName, int quantity) {
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

  public static String cancel(String orderId) {
    int orderIndex = orderService.getOrderIndex(orderId);
    if (orderIndex < 0) {
      return orderId + " not found, no such order";
    }

    int productIndex = productService.getProductIndex(orderService.getOrders().get(orderIndex).productName);
    productService.addQuantity(productIndex, orderService.getOrders().get(orderIndex).quantity);
    orderService.removeOrder(orderIndex);
    return "Order " + orderId + " is canceled";
  }

  public static String search(String userName) {
    String allUserOrders = "";
    for (Order o: orderService.getOrders()) {
      if (o.userName.equals(userName)) {
        allUserOrders += o.orderId + ", " + o.productName + ", " + o.quantity + ";";
      }
    }
    if(allUserOrders.length() == 0) {
      return "No order found for " + userName;
    }
    return allUserOrders;
  }

  public static String list() {
    String listStr = "";
    for (Product p: productService.getTable()) {
      listStr += p.productName + " " + p.quantity + ";";
    }
    return listStr;
  }
}
