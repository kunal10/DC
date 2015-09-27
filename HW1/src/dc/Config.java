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

  public enum IpcType {
    BROADCAST, UNICAST
  };

  // Getters and Setters.
  public IpcType getIpcType() {
    return ipcType;
  }

  public int getNumProcesses() {
    return numProcesses;
  }

  public int[][] getDelaySpec() {
    return delaySpec;
  }
  
  public HashMap<Integer, Vector<Integer>> getBroadcastTs() {
    return broadcastTs;
  }
  
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("IpcType: " + ipcType.toString());
    result.append("\nNumProcesses: " + numProcesses);
    result.append("\nDelay Spec: ");
    for (int i = 0; i < numProcesses; i++) {
      result.append("\n");
      for (int j = 0; j < numProcesses; j++) {
        result.append(delaySpec[i][j] + " ");
      }
    }
    if (ipcType == IpcType.BROADCAST) {
      result.append("\nBroadcastTs: \n" + broadcastTs.toString());
    } else {
      // result.append("\nUnicastTs: \n" + unicastTs.toString());
    }
    return result.toString();
  }

  private void ReadConfig(String filename) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader("src/dc/" + filename));
    String line = br.readLine();
    // Set ipcType.
    try {
      if (line.compareToIgnoreCase("BROADCAST") == 0) {
        ipcType = IpcType.BROADCAST;
      } else {
        ipcType = IpcType.UNICAST;
      }
    } catch (Exception e) {
      br.close();
      System.out.println("Unable to parse Ipc Type. \n Expected values are "
              + "{ BROADCAST, UNICAST}. Exiting..");
    }

    // Set numProcess.
    line = br.readLine();
    try {
      numProcesses = Integer.parseInt(line);
    } catch (Exception e) {
      br.close();
      System.out.println("Unable to parse #processes. Exiting..");
    }

    // Set delay specs for all processes.
    delaySpec = new int[numProcesses][numProcesses];
    try {
      for (int i = 0; i < getNumProcesses(); i++) {
        line = br.readLine();
        if (line.isEmpty()) {
          continue;
        }
        String[] delay = line.split(" ");
        for (int j = 0; j < numProcesses; j++) {
          delaySpec[i][j] = Integer.parseInt(delay[j]);
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
      if (ipcType == IpcType.BROADCAST) {
        broadcastTs = new HashMap<Integer, Vector<Integer>> ();
        while ((line = br.readLine()) != null) {
          String[] msgSpec = line.split(" ");
          if (msgSpec.length < 1) {
            // Ignore the line if process no. is not mentioned.
            continue;
          }
          int src = Integer.parseInt(msgSpec[0]);
          Vector<Integer> ts = new Vector<Integer>();
          for (int i = 0; i < msgSpec.length; i++) {
            ts.add(Integer.parseInt(msgSpec[i]));
          }
          broadcastTs.put(src, ts);
        }
      }
    } catch (Exception e) {
      br.close();
      System.out.println("Unable to parse msg spec list. Exiting..");
    }
    br.close();
  }

  // Class members.
  private IpcType ipcType;
  private int numProcesses;
  private int[][] delaySpec;
  private HashMap<Integer, Vector<Integer>> broadcastTs;
  //private Vector<Vector<Vector<Integer>>> unicastTs;
  
  // Port to be used for communicating with server.
  public static final int PORT = 5000;
  
  public static void main(String[] args) {
    Config config = new Config("sample_config");
    System.out.println(config.toString());
  }
}
