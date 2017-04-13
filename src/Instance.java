import java.util.ArrayList;

/**
 * Created by loriz on 4/13/17.
 */
public class Instance {

    int nodeCount;
    int routeCount;
    ArrayList<Node> nodesList = new ArrayList<>();
    ArrayList<Node> completeTSP=new ArrayList<>();
    ArrayList<Node> backHaulTSP= new ArrayList<>();
    ArrayList<Node> lineHaulTSP= new ArrayList<>();

    ArrayList<Integer> indexesLineHaulTSP = new ArrayList<>();
    ArrayList<Integer> indexesBackHaulTSP = new ArrayList<>();
    ArrayList<Integer> indexesCompleteTSP = new ArrayList<>();


    public  ArrayList<Node> createTSPFromNodes(ArrayList<Node> nodes, ArrayList<Integer> indexes){

        ArrayList<Node> TSP=new ArrayList<>();

        for (int i = 0; i <indexes.size() ; i++) {

            TSP.add(nodes.get(indexes.get(i)));
        }
        return TSP;
    }
}
