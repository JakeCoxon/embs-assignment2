package embs;

import java.util.LinkedList;

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
  
  protected LinkedList<Task> queue1;
  protected LinkedList<Task> queue2;
  protected LinkedList<Task> currentQueue;
  
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
    
    queue1 = new LinkedList<Task>();
    queue2 = new LinkedList<Task>();
    currentQueue = queue1;
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
        
        if (task.isLocalProcessor())
          send(task);
        else
          currentQueue.add(task);
        
      }
    }
    
    checkFinished(buses[0]); checkFinished(buses[1]);
    
    
    if (currentQueue.size() > 0) {
      System.out.println(currentQueue.size());

      // This is my way of circumventing concurrent modification
      // and also I'm too lazy to make a proper linked list
      
      // Swap current and old queue
      LinkedList<Task> oldQueue = (currentQueue == queue1 ? queue1 : queue2);
      LinkedList<Task> newQueue = (currentQueue == queue1 ? queue2 : queue1);
      newQueue.clear();
      
      
      for (Task task : oldQueue) {
        newQueue.add(task);

        boolean sourceBusFree = !getSourceBus(task).isBusy();
        boolean destBusFree   =   !getDestBus(task).isBusy();
        
        if (sourceBusFree && destBusFree) {
          startTask(task);
          newQueue.removeLast();  // remove the task we just added
        }
      }
      
      currentQueue = newQueue;
    }
    
  }
  

  
  public InnerBus getSourceBus(Task task) {
    return task.sourceId < 4 ? buses[0] : buses[1];
  }
  public InnerBus getDestBus(Task task) {
    return task.destId < 4 ? buses[0] : buses[1];
  }
  
  
  /**
   * Starts a task on the relevant bus
   * @param task
   */
  public void startTask(Task task) {
    InnerBus source = getSourceBus(task);
    InnerBus dest = getDestBus(task);
    
    double finishTime = source.startTask(getDirector().getModelTime(), task);
    dest.setBusyUntil(finishTime);
    
    try {
      getDirector().fireAt(this, finishTime);
      
    } catch (IllegalActionException e) { e.printStackTrace(); }
    
  }
  
  /**
   * Sends a task through the output port
   * @param task
   */
  public void send(Task task) {
    try {
      output.send(task.sourceId, task.token);
    } catch (NoRoomException e) {
      e.printStackTrace();
    } catch (IllegalActionException e) {
      e.printStackTrace();
    }
  }
  
  
}
