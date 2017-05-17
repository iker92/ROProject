package core;

import java.util.ArrayList;

/**
 * Instance is the class which holds all the information bond to a single Instance of the problem to optimize.
 * These informations are usually retrieved from a .txt file given as a Linehaul/Backhaul transportation problem.
 */
public class Instance {

    public int nodeCount;
    public int routeCount;
    public int maxWeight;
    public String fileName;
    public ArrayList<Node> nodesList = new ArrayList<>();
    public ArrayList<Node> completeTSP = new ArrayList<>();
    public ArrayList<Node> backHaulTSP = new ArrayList<>();
    public ArrayList<Node> lineHaulTSP = new ArrayList<>();

    public ArrayList<Integer> indexesLineHaulTSP = new ArrayList<>();
    public ArrayList<Integer> indexesBackHaulTSP = new ArrayList<>();
    public ArrayList<Integer> indexesCompleteTSP = new ArrayList<>();


    /**
     * createTSPFromNodes creates a TSP (as an ArrayList<Node>) given a specified array of indexes by getting the nodes
     *                    corresponding to the indexes
     * @param nodes is the ArrayList containing the nods that must be placed into the tsp
     * @param indexes is the ArrayList containing the indexes of the nodes to retrieve from the other input ArrayList
     * @return the corresponding TSP
     */
    public  ArrayList<Node> createTSPFromNodes(ArrayList<Node> nodes, ArrayList<Integer> indexes){
        ArrayList<Node> tsp=new ArrayList<>();
        for (Integer i : indexes) tsp.add(nodes.get(i));
        return tsp;
    }
}
