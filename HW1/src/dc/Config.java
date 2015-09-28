package dc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;

/**
 * Class to encapsulate the configuration properties of IPC simulation like:
 * 1. Number of processes.
 * 2. Type of communication (Broadcast/Unicast).
 * 3. Message delay specification.
 * 4. List of {source,destination,timestamp} tuples to simulate. (NOTE : For
 * Broadcast destinations don't need to be specified.
 */
public class Config {
  public Config(String configFile) {
    super();
    try {
      ReadConfig(configFile);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(
              "Could not read config file. See the stack trace for details");
    }
  }

  public int getPort() {
    return port;
  }
  
  public int getNumProcesses() {
    return numProcesses;
  }
  
  public int getStopTime() {
    return stopTime;
  }

  public long[][] getDelaySpec() {
    return delaySpec;
  }
  
  public HashMap<Integer, Vector<Long>> getBroadcastTs() {
    return broadcastTs;
  }

  public String getLogDir() {
    return logDir;
  }
  
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("\nPort: " + port);
    result.append("\nNumProcesses: " + numProcesses);
    result.append("\nSimulationStopTime: " + stopTime);
    result.append("\nDelay Spec: ");
    for (int i = 0; i < numProcesses; i++) {
      result.append("\n");
      for (int j = 0; j < numProcesses; j++) {
        result.append(delaySpec[i][j] + " ");
      }
    }
    result.append("\nBroadcastTs: \n" + broadcastTs.toString());
    return result.toString();
  }

  private void ReadConfig(String filename) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(filename));
   
    // Set log directory.
    logDir = br.readLine();
    
    // Set numProcess, server port and max simulation time
    String line = br.readLine();
    try {
      String[] params = line.split(" ");
      port = Integer.parseInt(params[0]);
      numProcesses = Integer.parseInt(params[1]);
      stopTime = Integer.parseInt(params[2]);
    } catch (Exception e) {
      br.close();
      System.out.println("Unable to parse #processes. Exiting..");
    }

    // Set delay specs for all processes.
    delaySpec = new long[numProcesses][numProcesses];
    try {
      for (int i = 0; i < getNumProcesses(); i++) {
        line = br.readLine();
        if (line.isEmpty()) {
          continue;
        }
        String[] delay = line.split(" ");
        for (int j = 0; j < numProcesses; j++) {
          delaySpec[i][j] = Long.parseLong(delay[j]) * 1000;
        }
      } 
    } catch (Exception e) {
      br.close();
      System.out.println(e.getStackTrace());
      System.out.println("Unable to parse delay spec matrix. \n"
              + "Expecting a sq integer matrix of dim: " + numProcesses
              + "\nExiting..");
    }

    // Set broadcast timestamps for all processes.
    try {
      broadcastTs = new HashMap<Integer, Vector<Long>> ();
      while ((line = br.readLine()) != null) {
        String[] msgSpec = line.split(" ");
        if (msgSpec.length < 1) {
          // Ignore the line if process no. is not mentioned.
          continue;
        }
        int src = Integer.parseInt(msgSpec[0]);
        Vector<Long> ts = new Vector<Long>();
        for (int i = 1; i < msgSpec.length; i++) {
          ts.add(Long.parseLong(msgSpec[i]) * 1000);
        }
        broadcastTs.put(src, ts);
      }
    } catch (Exception e) {
      br.close();
      System.out.println("Unable to parse msg spec list. Exiting..");
    }
    br.close();
  }

  // Class members.
  private int numProcesses;
  private int port;
  private int stopTime;
  // Delay in Millisec for channel between pi and pj.
  private long[][] delaySpec;
  // Broadcast timestamps for each client in Millisec.
  private HashMap<Integer, Vector<Long>> broadcastTs;
  private String logDir;

  public static void main(String[] args) {
    Config config = new Config(args[0]);
    System.out.println(config.toString());
  }
}
