package embs;

import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.IntToken;

/**
 * The Task class makes it easier to read data from the tokens
 *
 * ex token: {communication = {destination = 7, messagelength = 80}, 
 *   comptimeCPU = 5.0, comptimeDSP = 5.0, comptimeHW = 5.0, 
 *   id = 15, period = 16.0, releasetime = 2608.0}
 */
public class Task implements Comparable<Task> {

  public final int srcId;
  public final RecordToken token;
  public final int dstId;
  public final int messageLength;
  public final double releaseTime;

  public Task(int sourceId, RecordToken token) {
    this.srcId = sourceId;
    RecordToken comToken = (RecordToken) token.get("communication");
    this.dstId = ((IntToken) comToken.get("destination")).intValue();
    this.token = token;
    this.messageLength = ((IntToken) comToken.get("messagelength")).intValue();
    this.releaseTime = ((DoubleToken) token.get("releasetime")).doubleValue();
  }
  
  /** Gets whether sourceId equals destId **/
  public boolean isLocalProcessor() {
    return srcId == dstId;
  }

  @Override
  public int compareTo(Task o) {
    return Double.compare(o.releaseTime, releaseTime);
  }
  
}
