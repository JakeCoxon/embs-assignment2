package embs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class BridgeBus extends TypedAtomicActor {
  protected TypedIOPort[] inputs;
  protected TypedIOPort output;
  protected Parameter pbandwidth;
  private ArrayList<Task> tempList = new ArrayList<Task>();
  
  private InnerBus[] buses;
  
  public BridgeBus(CompositeEntity container, String name)
   throws NameDuplicationException, IllegalActionException {
    super(container,name);
    
    inputs = new TypedIOPort[8];
    
    for (int i = 0; i < 8; i++) {
      inputs[i] = new TypedIOPort(this, "input"+i, true, false);
      inputs[i].setTypeEquals(BaseType.GENERAL);
    }
    
    output = new TypedIOPort(this, "output", false, true);
    output.setTypeEquals(BaseType.GENERAL);
    output.setMultiport(true);
  
    pbandwidth = new Parameter(this,"bandwidth");
    pbandwidth.setExpression("1");
    
  }
  
  @Override
  public void initialize() throws IllegalActionException {
    super.initialize();
    
    double bandwidth = Double.valueOf(pbandwidth.getValueAsString());
    
    buses = new InnerBus[] {
      new InnerBus(bandwidth), new InnerBus(bandwidth)
    };
  }
  
  
  /** Checks if a bus has finished a task, if so send the task through
   *  the output port **/
  private void checkFinished(InnerBus bus) {
    Task finishedTask = bus.checkFinished(getDirector().getModelTime());
    
    if (finishedTask != null) {
      send(finishedTask);
    }
  }
  
  @Override
  public void fire() throws IllegalActionException {
    super.fire();

    for (int i = 0; i < 8; i++) {
      if (inputs[i].hasToken(0)) {

        RecordToken token = (RecordToken) inputs[i].get(0);
        Task task = new Task(i, token);
        
        // Tasks on local processor use local memory and should
        // instantly send
        // Other task push to the end of the source bus' queue
        
        if (task.isLocalProcessor())
          send(task);
        else
          getSourceBus(task).addTask(task);
        
      }
    }
    
    // Check if any tasks are finished and send them out
    checkFinished(buses[0]);
    checkFinished(buses[1]);

    // Peek the head of both buses, make an array of these two or 
    // fewer items, sort items based on release-time, start the first
    // available task
    
    ArrayList<Task> taskPair = tempList;
    taskPair.clear();
    
    if (buses[0].peek() != null) taskPair.add(buses[0].peek());
    if (buses[1].peek() != null) taskPair.add(buses[1].peek());
    
    Collections.sort(taskPair);
    
    processTaskList(taskPair);
  }
  
  /** Given a list of tasks, start the first one that has free buses */
  private void processTaskList(List<Task> tasks) {
    for (Task task : tasks) {
      // Source and destination bus may be the same
      // but that's okay
      boolean srcBusFree = !getSourceBus(task).isBusy();
      boolean dstBusFree =   !getDestBus(task).isBusy();
      
      if (srcBusFree && dstBusFree) {
        startTask(task);
        return;
      }
    }
  }
  
  
  /** Starts a task on the relevant bus */
  public void startTask(Task task) {
    // Again source and destination bus may be equal
    // make sure method still works in this case
    InnerBus srcBus = getSourceBus(task);
    InnerBus dstBus =   getDestBus(task);
    
    Time finishTime = srcBus.startTask(getDirector().getModelTime(), task);
    dstBus.setBusyUntil(finishTime);
    
    try {
      getDirector().fireAt(this, finishTime);
      
    } catch (IllegalActionException e) { e.printStackTrace(); }
    
  }
  
  /** Sends a completed task through the output port */
  public void send(Task task) {
    try {
      output.send(task.srcId, task.token);
    } catch (NoRoomException e) {
      e.printStackTrace();
    } catch (IllegalActionException e) {
      e.printStackTrace();
    }
  }
  

  

  /** Gets the bus based on a tasks source processor **/
  public InnerBus getSourceBus(Task task) {
    return task.srcId < 4 ? buses[0] : buses[1];
  }
  /** Gets the bus based on a tasks destination processor **/
  public InnerBus getDestBus(Task task) {
    return task.dstId < 4 ? buses[0] : buses[1];
  }
  
  
}
