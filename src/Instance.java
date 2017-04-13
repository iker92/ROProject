import java.util.ArrayList;

/**
 * Created by loriz on 4/13/17.
 */
public class Instance {

    int nodeCount;
    int routeCount;
    ArrayList<Node> nodesList = new ArrayList<>();

    ArrayList<Integer> indexesLineHaulTSP = new ArrayList<>();
    ArrayList<Integer> indexesBackHaulTSP = new ArrayList<>();
    ArrayList<Integer> indexesCompleteTSP = new ArrayList<>();


    ArrayList<Node> completeTSP=createTSPFromNodes(nodesList,indexesCompleteTSP);
    ArrayList<Node> backHaulTSP=createTSPFromNodes(nodesList,indexesBackHaulTSP);
    ArrayList<Node> lineHaulTSP=createTSPFromNodes(nodesList,indexesLineHaulTSP);

    public static ArrayList<Node> createTSPFromNodes(ArrayList<Node> nodes, ArrayList<Integer> indexes){

        ArrayList<Node> completeTSP=new ArrayList<>();

        for (int i = 0; i <indexes.size() ; i++) {

            completeTSP.add(nodes.get(indexes.get(i)));
        }
        return completeTSP;
    }
}
