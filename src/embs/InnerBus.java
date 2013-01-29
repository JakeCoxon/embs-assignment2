package embs;

import ptolemy.actor.util.Time;

public class InnerBus {
  
  private Task current;
  private double busyTime;
  private final double bandwidth;
  
  public InnerBus(double bandwidth) {
    this.bandwidth = bandwidth;
  }
  
  public boolean isBusy() {
    return busyTime != 0;
  }
  
  public Task getCurrent() {
    return current;
  }
  
  /** Checks if the current task has finised and updates isBusy
   *  If this is the source bus it returns the task that finished **/
  public Task checkFinished(Time currentTime) {
    //if (isBusy() && currentTime.compareTo(busyTime) != 0) {
    if (isBusy() && currentTime.getDoubleValue() > busyTime) {
      busyTime = 0;
      Task ret = current;
      current = null;
      
      return ret;
    }
    return null;
  }
  

  /** Sets the currently processing task to the given task
   *  and returns the time that the task will finish **/
  public double startTask(Time currentTime, Task task) {
    current = task;
    double offset = task.messageLength/bandwidth;
    System.out.println("Starting task that will take "+task.messageLength/bandwidth+"ms");
    return currentTime.getDoubleValue() + offset;
    //return currentTime.add(offset);
  }

  /** Sets this bus to be busy for task's time **/
  public void setBusyUntil(double finishTime) {
    busyTime = finishTime;
  }
}
