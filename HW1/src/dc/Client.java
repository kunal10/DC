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

  public void sendMessageReq(int dest, int ts, int[] vc) throws IOException {
    Message msg = new Message(clientId, dest,ts, vc);
    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    oos.writeObject(msg);
    oos.flush();
  }

  public void connect() throws UnknownHostException, IOException {
    socket = new Socket("localhost", Config.PORT);
    System.out.println("Client: " + clientId + " connected to server at "
            + "localhost " + socket.getPort());
    // Send a dummy message to notify the server about the cliendId.
    int dest = -1;
    int ts = 0;
    int[] vc = new int[config.getNumProcesses()];
    for (int i = 0; i < vc.length; i++) {
      vc[i] = 0;
    }
    sendMessageReq(dest, ts, vc);
    socket.close();
  }

  private int clientId;
  private Config config;
  private Socket socket;

  public static void main(String[] args)
          throws UnknownHostException, IOException {
    Config config = new Config(args[0]);
    System.out.println(config.toString());
    Client client = new Client(Integer.parseInt(args[1]), config);
    client.connect();
  }
}
