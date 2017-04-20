import java.util.ArrayList;

/**
 * Created by loriz on 4/13/17.
 */
public class Instance {

    int nodeCount;
    int routeCount;
    int maxWeight;
    ArrayList<Node> nodesList = new ArrayList<>();
    ArrayList<Node> completeTSP = new ArrayList<>();
    ArrayList<Node> backHaulTSP = new ArrayList<>();
    ArrayList<Node> lineHaulTSP = new ArrayList<>();

    ArrayList<Integer> indexesLineHaulTSP = new ArrayList<>();
    ArrayList<Integer> indexesBackHaulTSP = new ArrayList<>();
    ArrayList<Integer> indexesCompleteTSP = new ArrayList<>();


    /**
     * createTSPFromNodes creates a TSP (as an ArrayList<Node>) given a specified array of indexes by getting the nodes
     *                    corresponding to the indexes
     * @param nodes
     * @param indexes
     * @return the corresponding TSP
     */
    public  ArrayList<Node> createTSPFromNodes(ArrayList<Node> nodes, ArrayList<Integer> indexes){
        ArrayList<Node> tsp=new ArrayList<>();
        for (Integer i : indexes) tsp.add(nodes.get(i));
        return tsp;
    }
}
