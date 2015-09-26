package dc;

import java.io.BufferedReader;
import java.io.FileReader;
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

  public Vector<Vector<Integer>> getBroadcastTs() {
    return broadcastTs;
  }

  public Vector<Vector<Vector<Integer>>> getUnicastTs() {
    return unicastTs;
  }
  
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("IpcType: " + ipcType.toString());
    result.append("\nNumProcesses: " + numProcesses);
    result.append("\nDelay Spec: \n" + delaySpec.toString());
    if (ipcType == IpcType.BROADCAST) {
      result.append("\nBroadcastTs: \n" + broadcastTs.toString());
    } else {
      result.append("\nUnicastTs: \n" + unicastTs.toString());
    }
    return result.toString();
  }

  private void ReadConfig(String filename) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(filename));
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
    for (int i = 0; i < getNumProcesses(); i++) {
      try {
        line = br.readLine();
        if (line.isEmpty()) {
          continue;
        }
        String[] delay = line.split(" ");
        for (int j = 0; j < numProcesses; j++) {
          delaySpec[i][j] = Integer.parseInt(delay[j]);
        }
      } catch (Exception e) {
        br.close();
        System.out.println("Unable to parse delay spec matrix. \n"
                + "Expecting a sq integer matrix of dim: " + numProcesses
                + "\nExiting..");
      }
    }

    // Set broadcast timestamps for all processes.
    try {
      if (ipcType == IpcType.BROADCAST) {
        broadcastTs.setSize(numProcesses);
        while ((line = br.readLine()) != null) {
          String[] msgSpec = line.split(" ");
          if (msgSpec.length < 2) {
            // Ignore the line if process is not broadcasting to anyone.
            continue;
          }
          int src = Integer.parseInt(msgSpec[0]);
          for (int i = 0; i < msgSpec.length; i++) {
            broadcastTs.elementAt(src).add(Integer.parseInt(msgSpec[i]));
          }
        }
      } else { // Set unicast timestamps for all messages.
        // Resize 1st 2 dimensions of unicastTs to numProcesses x numProcess.
        unicastTs.setSize(numProcesses);
        for (int src = 0; src < numProcesses; src++) {
          unicastTs.elementAt(src).setSize(numProcesses);
        }
        // Add all message timestamps.
        while ((line = br.readLine()) != null) {
          if (line.isEmpty()) {
            continue;
          }
          String[] msgSpec = line.split(" ");
          int src = Integer.parseInt(msgSpec[0]);
          int dest = Integer.parseInt(msgSpec[1]);
          int ts = Integer.parseInt(msgSpec[2]);
          unicastTs.elementAt(src).elementAt(dest).add(ts);
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
  private Vector<Vector<Integer>> broadcastTs;
  private Vector<Vector<Vector<Integer>>> unicastTs;
  
  public static void main(String[] args) {
    Config config = new Config("sample_config");
    System.out.println(config.toString());
  }
}
