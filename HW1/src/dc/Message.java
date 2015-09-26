package dc;

/** Class to encapsulate the messages sent over the network. */
public class Message {
  public int getSrc() {
    return src;
  }
  public void setSrc(int src) {
    this.src = src;
  }
  public int getDest() {
    return dest;
  }
  public void setDest(int dest) {
    this.dest = dest;
  }
  public int[] getTs() {
    return ts;
  }
  public void setTs(int[] ts) {
    this.ts = ts;
  }
  private int src;
  private int dest;
  // Vector Clock time stamp corresponding to this message.
  // NOTE : We are assuming that we know the number of processes in the system. But this may
  // change with time. If ts contains less entries than the #processes then we can assume the
  // rest to be 0.
  private int[] ts;
}
