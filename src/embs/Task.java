package embs;

import ptolemy.data.RecordToken;
import ptolemy.data.IntToken;

/**
 * The Task class makes it easier to read data from the tokens
 *
 */
public class Task {

  public final int sourceId;
  public final RecordToken token;
  public final int destId;
  public final int messageLength;

  public Task(int sourceId, RecordToken token) {
    this.sourceId = sourceId;
    RecordToken comToken = (RecordToken) token.get("communication");
    this.destId = ((IntToken) comToken.get("destination")).intValue();
    this.token = token;
    this.messageLength = ((IntToken) comToken.get("messagelength")).intValue();
  }
  
  /** Gets whether sourceId equals destId **/
  public boolean isLocalProcessor() {
    return sourceId == destId;
  }
}
