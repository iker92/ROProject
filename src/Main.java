import java.util.ArrayList;

/**
 * Created by pippo on 11/04/17.
 */
public class Main {

    public static void main(String [] args) {

        Helper helper = new Helper();

        // The name of the file to open.
        String fileName = "Instances/A1.txt";

        Instance instance1 = helper.fileToInstance(fileName);


    ArrayList<Node> completeTSP=createTSPFromNodes(instance1.nodesList,instance1.completeTSP);
    ArrayList<Node> backHaulTSP=createTSPFromNodes(instance1.nodesList,instance1.backHaulTSP);
    ArrayList<Node> lineHaulTSP=createTSPFromNodes(instance1.nodesList,instance1.lineHaulTSP);


        for (Node node : lineHaulTSP) {
            System.out.println(node.coordinates.x+" "+ node.coordinates.y+ " "+ node.nodeType + " "+ node.weight);
        }
    }

    public static ArrayList<Node> createTSPFromNodes(ArrayList<Node> nodes, ArrayList<Integer> indexes){

        ArrayList<Node> completeTSP=new ArrayList<>();

        for (int i = 0; i <indexes.size() ; i++) {

            completeTSP.add(nodes.get(indexes.get(i)));
        }
        return completeTSP;
    }

}
