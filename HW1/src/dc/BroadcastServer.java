package dc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.PriorityBlockingQueue;

 class serverThread extends Thread{
   public serverThread(int clientId, Socket clientSocket, 
                       PriorityBlockingQueue<Message> requests) {
     super();
     this.clientSocket = clientSocket;
     this.requests = requests;
   }
  
   public void run() {
     Message request = null;
     try {
       request = readRequest();
       if (request == null) {
         System.out.println("Something went wrong while parsing the request");
       } else {
         requests.add(request);
       }
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
  
   private Message readRequest() throws IOException {
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

   private Socket clientSocket;
   private PriorityBlockingQueue<Message> requests;
}

/**
 * @author kunal
 *
 */
public class BroadcastServer {
  public BroadcastServer(Config config) {
    super();
    this.config = config;
    clientSockets = new Socket[config.getNumProcesses()];
    requests = new PriorityBlockingQueue<Message>();
    this.startTime = 0;
  }

  public void start() throws IOException {
    serverSocket = new ServerSocket(Config.PORT);
    System.out.println("Started a new Broadcast Server on port: " + Config.PORT
            + "\nWaiting for clients to join.");
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

    // All the processes have established a connection. Notify all the clients
    // to reset their clocks and start the simulation.
    int[] vc = new int[config.getNumProcesses()];
    for (int i = 0; i < config.getNumProcesses(); i++) {
      vc[i] = 0;
    }
    startTime = ((System.currentTimeMillis() / 1000) + 1) * 1000;
    Message reset = new Message(-1, -1, 0, vc);
    broadcastMsg(reset);
    
    // Start processing pending requests.
    while(true) {
      processNextRequest();
    }
  }

  public void processNextRequest() {
    if (requests.isEmpty()) {
      return;
    }
    Message request = requests.peek();
    if (request.getTs() <= getCurrentTime()) {
      try {
        broadcastMsg(request);
      } catch (IOException e) {
        System.out.println("Broadcast failed for msg:\n" + request.toString());
        e.printStackTrace();
      }
      try {
        requests.take();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return;
  }
  
  public void broadcastMsg(Message msg) throws IOException {
    for (int i = 0; i < config.getNumProcesses(); i++) {
      int dest = i;
      Message m = new Message(msg.getSrc(), dest, msg.getTs(), msg.getVc());
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
    int dest = msg.getDest();
    ObjectOutputStream oos = new ObjectOutputStream(
            clientSockets[dest].getOutputStream());
    oos.writeObject(msg);
    oos.flush();
  }
  
  public long getCurrentTime() {
    if (startTime == 0) {
      return 0;
    } else {
      return (System.currentTimeMillis() - startTime)/1000;
    }
  }

  private Config config;
  private ServerSocket serverSocket;
  private Socket[] clientSockets;
  private PriorityBlockingQueue<Message> requests;
  private long startTime;

  public static void main(String[] args) throws IOException {
    Config config = new Config(args[0]);
    System.out.println(config.toString());
    BroadcastServer bc = new BroadcastServer(config);
    bc.start();
  }
}
