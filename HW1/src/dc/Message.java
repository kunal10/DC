package dc;

import java.io.Serializable;

/** Class to encapsulate the messages sent over the network. */
public class Message implements Comparable<Message>, Serializable {
  public Message(int src, int dest, int ts, int[] vc) {
    super();
    this.src = src;
    this.dest = dest;
    this.ts = ts;
    this.vc = vc;
  }
  
  @Override
  public int compareTo(Message o) {
    return (this.ts - o.getTs());
  }
  
  public boolean isServerMsg() {
    return (src == -1 && dest >= 0);
  }
  
  public boolean isClientMsg() {
    return (src > 0 && dest > 0); 
  }
  
  public boolean isClientServerMsg() {
    return (src > 0 && dest == -1);
  } 

  public boolean isValid() {
    return (isServerMsg() || isClientMsg() || isClientServerMsg());
  }
  
  public int getSrc() {
    return src;
  }

  public int getDest() {
    return dest;
  }
  
  public int getTs() {
    return ts;
  }
  
  public int[] getVc() {
    return vc;
  }

  private int src;
  private int dest;
  /** Timestamp corresponding to the message. */
  private int ts;
  /**
   *  Vector Clock time stamp corresponding to this message.
   *  NOTE : We are assuming that we know the number of processes in the system.
   *  But this may change with time. If vc contains less entries than the 
   *  #processes then we can assume the rest to be 0.
   */
  private int[] vc;
}
