package embs;

import ptolemy.actor.gui.MoMLSimpleApplication;

public class Program extends MoMLSimpleApplication {

  public static void main(String[] args) {
    try {
      new Program();
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  public Program() throws Throwable {
    super("OpenAssessment2_jake.xml");
  }
  
}
