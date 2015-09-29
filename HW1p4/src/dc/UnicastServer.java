package dc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

class ServerThread extends Thread {
  public ServerThread(Config config, int clientId, Socket clientSocket,
          PriorityBlockingQueue<Message> requests) {
    super();
    this.startTime = System.currentTimeMillis();
    this.config = config;
    this.clientId = clientId;
    this.clientSocket = clientSocket;
    this.requests = requests;
  }

  public void run() {
    System.out.println("Started Server Thread for client: " + clientId);
    while (getCurrentTimeSecs() < config.getStopTime()) {
      Message request = null;
      try {
        request = readRequest();
        if (request == null) {
          System.out.println("Something went wrong while parsing the request");
        } else {
          System.out.println(
                  "Server received Unicast request: " + request.toString());
          requests.add(request);
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
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

  /** Returns time since start of simulation in milli sec. */
  private long getCurrentTime() {
    if (startTime == 0) {
      return 0;
    } else {
      return (System.currentTimeMillis() - startTime);
    }
  }

  /** Returns current time in secs. Used for logging the output. */
  private long getCurrentTimeSecs() {
    return (getCurrentTime() / 1000);
  }

  private long startTime;
  private Config config;
  private int clientId;
  private Socket clientSocket;
  private PriorityBlockingQueue<Message> requests;
}

/**
 *
 */
public class UnicastServer {
  public UnicastServer(Config config) {
    super();
    this.config = config;
    clientSockets = new Socket[config.getNumProcesses()];
    requests = new PriorityBlockingQueue<Message>();
    Comparator<Message> messageDeliveryComparator = new Message.MessageDeliveryComparator();
    responses = new PriorityQueue<Message>(messageDeliveryComparator);
    this.startTime = 0;
  }

  public void start() throws IOException {
    serverSocket = new ServerSocket(config.getPort());
    System.out.println("Started a new Unicast Server on port: "
            + config.getPort() + "\nWaiting for clients to join.");
    for (int i = 0; i < config.getNumProcesses(); i++) {
      Socket clientSocket = serverSocket.accept();
      Message request = readRequest(clientSocket);
      if (request == null) {
        System.out.println("Something went wrong while parsing the request");
      }
      int clientId = request.getSrc();
      System.out.println("Established Connection to client: " + clientId);
      clientSockets[clientId] = clientSocket;
      // Start a server thread for incoming requests on this client socket.
      ServerThread serverThread = new ServerThread(config, clientId,
              clientSocket, requests);
      serverThread.start();
    }

    // All the processes have established a connection. Notify all the clients
    // to reset their clocks and start the simulation.
    int[][] vc = new int[config.getNumProcesses()][config.getNumProcesses()];
    for (int i = 0; i < config.getNumProcesses(); i++) {
      for (int j = 0; j < config.getNumProcesses(); j++)
        vc[i][j] = 0;
    }

    // NOTE : We purposely add 1 so that simulation starts after every process
    // has received this message.
    long startTimeSecs = (System.currentTimeMillis() / 1000) + 1;
    setStartTime(startTimeSecs * 1000);
    Message reset = new Message(-1, -1, 0, vc);
    reset.setStartTime(startTime);
    // Wait for currentTime to be 0 before starting.
    while (getCurrentTime() < 0) {
    }
    broadcastMsg(reset);
    sendNextMessage();

    // Start processing pending requests.
    while (getCurrentTimeSecs() < config.getStopTime()) {
      processNextRequest();
      sendNextMessage();
    }

    // serverSocket.close();

    System.out.println("Simulation Completed !!");
  }

  /**
   * Process next request if its timestamp is > current time.
   * if so schedules the message for broadcast.
   */
  public void processNextRequest() {
    if (requests.isEmpty()) {
      return;
    }
    Message request = requests.peek();
    if (request.getTs() > getCurrentTime()) {
      return;
    }
    try {
      requests.take();
      unicastMsg(request);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return;
  }

  /**
   * Process next response if its timestamp + delay is > current time.
   * If so sends the message to the appropriate client.
   */
  public void sendNextMessage() {
    if (responses.isEmpty()) {
      return;
    }
    Message response = responses.peek();
    if (response.getTs() + response.getDelay() > getCurrentTime()) {
      return;
    }
    response = responses.remove();
    try {
      sendMsg(response);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*
   * Schedules send of the message to all the clients by adding the message
   * corresponding to each client in the responses queue.
   */
  public void broadcastMsg(Message msg) throws IOException {
    long[][] delaySpec = config.getDelaySpec();
    for (int i = 0; i < config.getNumProcesses(); i++) {
      int src = msg.getSrc();
      int dest = i;
      long delay = (src == -1 || dest == -1) ? 0 : delaySpec[src][dest];
      Message m = new Message(src, dest, msg.getTs(), delay, msg.getVc());
      m.setStartTime(startTime);
      responses.add(m);
    }
  }

  /*
   * Schedules send of the message to a particular client by adding the message
   * in the responses queue.
   */
  public void unicastMsg(Message msg) throws IOException {
    long[][] delaySpec = config.getDelaySpec();
    int src = msg.getSrc();
    int dest = msg.getDest();
    long delay = (src == -1 || dest == -1) ? 0 : delaySpec[src][dest];
    Message m = new Message(src, dest, msg.getTs(), delay, msg.getVc());
    m.setStartTime(startTime);
    responses.add(m);
  }

  /** Returns current time in Millisec. */
  private long getCurrentTime() {
    return (System.currentTimeMillis() - startTime);
  }

  /** Returns current time in secs. Used for logging the output. */
  private long getCurrentTimeSecs() {
    return (getCurrentTime() / 1000);
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

  /** Set startTime for simulation */
  private void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  private Config config;
  private ServerSocket serverSocket;
  private Socket[] clientSockets;
  private PriorityBlockingQueue<Message> requests;
  private PriorityQueue<Message> responses;
  private long startTime;

  public static void main(String[] args) throws IOException {
    Config config = new Config(args[0]);
    System.out.println(config.toString());
    UnicastServer us = new UnicastServer(config);
    us.start();
  }
}
