package dc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

class ClientThread extends Thread {
  public ClientThread(long startTime, Config config, Socket socket,
          List<Message> toDeliver, StringBuilder log) {
    super();
    this.startTime = startTime;
    this.config = config;
    this.socket = socket;
    this.toDeliver = toDeliver;
    this.log = log;
  }

  public void run() {
    while (getCurrentTimeSecs() < config.getStopTime()) {
      try {
        Message msg = readMsg();
        if (msg != null) {
          synchronized(log) {
            log.append(getReceiveDescription(msg));
          }
          toDeliver.add(msg);
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private Message readMsg() throws IOException {
    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
    try {
      Message msg = (Message) ois.readObject();
      return msg;
    } catch (ClassNotFoundException e) {
      System.out.println("Could not parse the received msg");
      e.printStackTrace();
    }
    return null;
  }
  
  private String getReceiveDescription(Message m) {
    int[] vc = m.getVc();
    String desc = new String("\nt = " + getCurrentTimeSecs() + " p" + 
            m.getDest() + " REC p_" + m.getSrc() + ":" + vc[m.getSrc()]);
    return desc;
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
  private Socket socket;
  private List<Message> toDeliver;
  private StringBuilder log;
}

public class Client {

  public Client(int clientId, Config config) {
    super();
    this.clientId = clientId;
    this.config = config;
    this.sent = 0;
    this.delivered = new int[config.getNumProcesses()];
    this.toDeliver = Collections.synchronizedList(new ArrayList<Message>());
    this.startTime = 0;
    vc = new int[config.getNumProcesses()];
    for (int i = 0; i < vc.length; i++) {
      delivered[i] = 0;
      vc[i] = 0;
    }
    log = new StringBuilder();
  }

  public void connect() throws UnknownHostException, IOException {
    BufferedWriter bw = new BufferedWriter(
            new FileWriter(config.getLogDir() + "p" + clientId + ".log"));
    
    socket = new Socket("localhost", config.getPort());
    log.append("\nClient: " + clientId + " connected to server at "
            + "localhost " + socket.getPort());

    // Send a dummy message to notify the server about the cliendId.
    int server_dest = -1;
    Message msg = new Message(clientId, server_dest, startTime, vc);
    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    oos.writeObject(msg);
    oos.flush();

    // Read startTime message sent by server.
    Message startMsg = readMsg();
    setStartTime(startMsg.getStartTime());
    System.out.println("Starting Client Thread");

    // Start a thread to receive incoming messages.
    ClientThread clientThread = new ClientThread(startTime, config, socket,
            toDeliver, log);
    clientThread.start();

    while (getCurrentTimeSecs() < config.getStopTime()) {
      if (System.currentTimeMillis() < startTime) {
        // Start simulation at the designated time.
        continue;
      }
      // Send next message if its due.
      sendNextMsg();
      // Deliver all deliverable messages.
      deliverMessages();
    }

    bw.write(log.toString());
    bw.close();
    // Wait for one sec before closing the connection.
    // socket.close();
  }

  private void deliverMessages() {
    synchronized (toDeliver) {
      Iterator<Message> itr = toDeliver.iterator();
      while (itr.hasNext()) {
        Message m = new Message(itr.next());
        if (isDeliverable(m)) {
          itr.remove();
          updateVc(m);
          delivered[m.getSrc()]++;
          log.append(getDelvierDescription(m));
        }
      }
    }
  }

  private boolean isDeliverable(Message m) {
    int src = m.getSrc();
    int dest = m.getDest();
    if (dest != clientId) {
      return false;
    }
    if (delivered[src] != m.getVc()[src] - 1) {
      return false;
    }
    boolean deliverable = true;
    for (int i = 0; i < config.getNumProcesses(); i++) {
      if (i != src) {
        deliverable = deliverable && (delivered[i] >= m.getVc()[i]);
      }
    }
    return deliverable;
  }

  private void sendNextMsg() throws IOException {
    HashMap<Integer, Vector<Long>> broadcastTs = config.getBroadcastTs();
    if (!broadcastTs.containsKey(clientId)) {
      return;
    }
    Vector<Long> msgRequests = broadcastTs.get(clientId);
    if (sent < msgRequests.size()) {
      long nextSendTime = msgRequests.get(sent);
      long curTime = getCurrentTime();
      if (curTime < nextSendTime) {
        return;
      }
      synchronized (vc) {
        vc[clientId] += 1;
        Message msg = new Message(clientId, -1, curTime, vc);
        ObjectOutputStream oos = new ObjectOutputStream(
                socket.getOutputStream());
        oos.writeObject(msg);
        oos.flush();
        sent++;
        log.append(getSendDescription(msg));
      }
    }
  }

  private Message readMsg() throws IOException {
    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
    try {
      Message msg = (Message) ois.readObject();
      return msg;
    } catch (ClassNotFoundException e) {
      System.out.println("Could not parse the received msg");
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Updates the clients vc as per the delivery of msg at this client.
   * NOTE : This should be called after after the delivery of messages
   * (not on receipt).
   */
  private void updateVc(Message msg) {
    int src = msg.getSrc();
    int dest = msg.getDest();
    int[] msgVc = msg.getVc();
    synchronized (vc) {
      // Broadcast requests sent from client.
      if (msg.isClientServerMsg()) {
        // Ignore.
      } else {
        // Don't update clocks for receipt of messages in which client is not
        // involved.
        if (dest != clientId) {
          return;
        }
        // Ignore delivery of messages from itself.
        if (src == dest && src == clientId) {
          return;
        }
        // When a message is delivered from other process take max of each
        // component of vc.
        for (int i = 0; i < vc.length; i++) {
          vc[i] = Math.max(vc[i], msgVc[i]);
        }
      }
    }
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

  /** Set startTime for simulation */
  private void setStartTime(long startTime) {
    this.startTime = startTime;
  }
  
  private String getSendDescription(Message m) {
    String desc = new String("\nt = " + getCurrentTimeSecs() + " p" + 
            m.getSrc() + " BRC p_" + m.getSrc() + ":" + sent);
    return desc;
  }
  
  private String getDelvierDescription(Message m) {
    String desc = new String("\nt = " + getCurrentTimeSecs() + " p" + clientId + 
            " DLR p_" + m.getSrc() + ":" + delivered[m.getSrc()]);
    return desc;
  }

  private int clientId;
  private Config config;
  private Socket socket;
  // Keeps track of number of requests sent to the server.
  private int sent;
  // Keeps track of number of messages delivered from all the clients.
  private int[] delivered;
  private List<Message> toDeliver;
  private long startTime;
  /** Vector clock timestamp of latest send/receive event on this client */
  private int vc[];
  private StringBuilder log;

  public static void main(String[] args)
          throws UnknownHostException, IOException {
    Config config = new Config(args[0]);
    System.out.println(config.toString());
    Client client = new Client(Integer.parseInt(args[1]), config);
    client.connect();
  }
}
