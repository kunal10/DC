package dc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 */
public class BroadcastServer {
  public BroadcastServer(Config config) {
    super();
    this.config = config;
    clientSockets = new Socket[config.getNumProcesses()];
  }

  public void start() throws IOException {
    serverSocket = new ServerSocket(Config.PORT);
    System.out.println("Started a new Broadcast Server on port: " + Config.PORT + 
                       "\nWaiting for clients to join.");
    for (int i = 0; i < config.getNumProcesses(); i++) {
      System.out.println("Waiting for client: " + i);
      Socket clientSocket = serverSocket.accept();
      System.out.println("Some client requested a connection");
      Message request = readRequest(clientSocket);
      if (request == null) {
        System.out.println("Something went wrong while parsing the request");
      }
      int clientId = request.getSrc();
      System.out.println("Established Connection to client: " + clientId);
      clientSockets[clientId] = clientSocket;
    }
    serverSocket.close();
    // Start the clock and send start time.
  }
  
  public void broadcastMsg(Message msg) throws IOException {
    for (int i = 0; i < config.getNumProcesses(); i++) {
      Message m = new Message(i, msg.getDest(), msg.getVc());
      sendMsg(m);
    }
  }
  
  private Message readRequest(Socket clientSocket) throws IOException {
    ObjectInputStream ois = new ObjectInputStream(
            clientSocket.getInputStream());
    try {
      Message msg = (Message) ois.readObject();
      return msg;
    } catch (ClassNotFoundException e) {
      System.out.println("Could not parse the received client msg");
      e.printStackTrace();
    }
    return null;
  }
  
  private void sendMsg(Message msg) throws IOException {
    int clientId = msg.getSrc();
    ObjectOutputStream oos = new ObjectOutputStream(
            clientSockets[clientId].getOutputStream());
    oos.writeObject(msg);
    oos.flush(); 
  }
  
  private Config config;
  private ServerSocket serverSocket;
  private Socket[] clientSockets;

  public static void main(String[] args) throws IOException {
    Config config = new Config(args[0]);
    System.out.println(config.toString());
    BroadcastServer bc = new BroadcastServer(config);
    bc.start();
  }
}
