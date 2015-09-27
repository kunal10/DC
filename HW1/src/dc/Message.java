package dc;

import java.io.Serializable;

/** Class to encapsulate the messages sent over the network. */
public class Message implements Serializable{
  public Message(int src, int dest, int[] vc) {
    super();
    this.src = src;
    this.dest = dest;
    this.vc = vc;
  }

  public int getSrc() {
    return src;
  }

  public int getDest() {
    return dest;
  }

  public int[] getVc() {
    return vc;
  }

  private int src;
  private int dest;
  /**
   *  Vector Clock time stamp corresponding to this message.
   *  NOTE : We are assuming that we know the number of processes in the system.
   *  But this may change with time. If vc contains less entries than the 
   *  #processes then we can assume the rest to be 0.
   */
  private int[] vc;
}
