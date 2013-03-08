package embs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Set;

import embs.Graph;
import embs.KernighanLin;
import embs.KernighanLinProgram;
import embs.Vertex;


public class AssessmentKernighanLin {
  
  /** Main method, choose a graph.txt **/
  public static void main(String[] args) throws IOException {
    System.out.println("Input file: ");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    Graph graph = KernighanLinProgram.graphFromFile(reader.readLine());
    KernighanLinProgram.makeVerticesEven(graph);
    KernighanLinProgram.printGraph(graph);
    
    execute(graph, graph.getVertices());
  }

  /** Applies recursive Kerninghan-Lin **/
  private static void execute(Graph g, Set<Vertex> vertices) {
    KernighanLin k = KernighanLin.processWithVertices(g, vertices);
    
    System.out.print("Group A: ");
    for (Vertex x : k.getGroupA())
      System.out.print(x);
    System.out.print("\nGroup B: ");
    for (Vertex x : k.getGroupB())
      System.out.print(x);
    System.out.println("");
    System.out.println("Cut cost: "+k.getCutCost());
    
    if (k.getGroupA().size() >= 4) {
      execute(g, k.getGroupA());
      execute(g, k.getGroupB());
    }
  }
}
