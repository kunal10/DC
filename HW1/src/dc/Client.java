package dc;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

  public Client(int clientId, Config config) {          
    super();
    this.clientId = clientId;
    this.config = config;
  }
  
  public void sendMessageReq(int dest, int[] vc) throws IOException {
    Message msg = new Message(clientId, dest, vc);
    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    oos.writeObject(msg);
    oos.flush(); 
  }
  
  public void connect() throws UnknownHostException, IOException {
    socket = new Socket("localhost", Config.PORT);
    System.out.println("Client: " + clientId + " connected to server at " + 
            "localhost " + socket.getPort());
    // Send a msg to notify the server about the cliendId.
    int[] vc = new int[config.getNumProcesses()];
    for (int i = 0; i < vc.length; i++) {
      vc[i] = 0;
    }
    sendMessageReq(-1, vc);
  }

  private int clientId;
  private Config config;
  private Socket socket;
  
  public static void main(String[] args) {
    Config config = new Config(args[0]);
    System.out.println(config.toString());
  }
}
