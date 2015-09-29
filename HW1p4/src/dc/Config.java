package dc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

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
  
  public ArrayList<ArrayList<ArrayList<Long>>> unicastTs() {
    return unicastTs;
  }

  public String getLogDir() {
    return logDir;
  }
  
  public ArrayList<ArrayList<ArrayList<Long>>> getUnicastTs() {
    return unicastTs;
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
    result.append("\nUnicastTs: \n");
    for (int i = 0; i < unicastTs.size(); i++) {
      for (int j = 0; j < unicastTs.get(i).size(); j++) {
        if (unicastTs.get(i).get(j).isEmpty()) {
          continue;
        }
        result.append(i + "->" + j + " : "); 
        for (long ts : unicastTs.get(i).get(j)) {
          result.append(ts + ",");
        }
        result.append("\n");
      }
    }
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
      unicastTs = new ArrayList<ArrayList<ArrayList<Long>>> (numProcesses);
      for (int i = 0; i < numProcesses; i++) {
        unicastTs.add(new ArrayList<ArrayList<Long>>(numProcesses));
        for (int j = 0; j < numProcesses; j++) {
          unicastTs.get(i).add(new ArrayList<Long>());
        }
      }
      while ((line = br.readLine()) != null) {
        String[] msgSpec = line.split(" ");
        if (msgSpec.length < 3) {
          // Ignore the line if it has incorrect no. of params.
          continue;
        }
        int src = Integer.parseInt(msgSpec[0]);
        int dest = Integer.parseInt(msgSpec[1]); 
        String[] timestamps = msgSpec[2].split(",");
        for (int i = 0; i < timestamps.length; i++) {
          long ts = Long.parseLong(timestamps[i]) * 1000;
          unicastTs.get(src).get(dest).add(ts);
        }
      }
    } catch (Exception e) {
      br.close();
      System.out.println("Unable to parse msg spec list. Exiting..");
      e.printStackTrace();
    }
    br.close();
  }

  // Class members.
  private int numProcesses;
  private int port;
  private int stopTime;
  // Delay in Millisec for channel between pi and pj.
  private long[][] delaySpec;
  // Unicast timestamps for each client in Millisec.
  private ArrayList<ArrayList<ArrayList<Long>>> unicastTs;

  private String logDir;

  public static void main(String[] args) {
    Config config = new Config(args[0]);
    System.out.println(config.toString());
  }
}