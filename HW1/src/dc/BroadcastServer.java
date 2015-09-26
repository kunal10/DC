package dc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 */
public class BroadcastServer {
  public BroadcastServer(int port) {
    super();
    this.port = port;
  }
  public void start() throws IOException {
    serverSocket = new ServerSocket(port);
    System.out.println("Started a new Broadcast Server on port: " + port);
    Socket dataSocket = serverSocket.accept();
  }
  
  private int port;
  private ServerSocket serverSocket;
  
  public static void main(String[] args) {

  }

}
