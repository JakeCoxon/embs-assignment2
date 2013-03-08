package embs;

import java.util.LinkedList;

import ptolemy.actor.util.Time;

public class InnerBus {
  
  private Task current;
  private Time busyTime;
  private final double bandwidth;
  private LinkedList<Task> queue;
  
  public InnerBus(double bandwidth) {
    this.bandwidth = bandwidth;
    this.queue = new LinkedList<Task>();
  }
  
  public void addTask(Task task) { queue.add(task);         }
  
  public boolean isBusy()        { return busyTime != null; }
  public Task    peek()          { return queue.peek();     }
  
  /** Checks if the current task has finished at currentTime and 
   *  updates isBusy. Will remove finished task from the queue.
   *  If this is the source bus it returns the task that finished **/
  public Task checkFinished(Time currentTime) {
    
    if (isBusy() && currentTime.getDoubleValue() >= busyTime.getDoubleValue()) {
      
      busyTime = null;
      Task finishedTask = current;
      
      if (finishedTask != null) {
        if (finishedTask == queue.peek())
          queue.remove();
        else
          throw new RuntimeException("Completed task isn't at the head of the queue");
      }
      
      current = null;
      return finishedTask;
    }
    
    return null;
  }
  

  /** Sets the currently processing task to the given task
   *  and returns the time that the task will finish **/
  public Time startTask(Time currentTime, Task task) {
    current = task;
    double offset = task.messageLength/bandwidth;
    
    Time fin = currentTime.add(offset);
    setBusyUntil(fin);

    return fin;
  }

  /** Sets this bus to be busy until the given time **/
  public void setBusyUntil(Time time) {
    busyTime = time;
  }
}
