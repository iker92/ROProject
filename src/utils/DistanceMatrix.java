package utils;

import core.Node;
import core.Values;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 * This class implements the DistanceMatrix. The distance matrix is used to get the distance between two nodes in O(1).
 * The class also contains some useful methods to get the distance between two nodes, to calculate the actual distance and, given a node, to
 * obtain all the nodes sorted ascending by distance to it.
 */
public class DistanceMatrix {

    public ArrayList<Node> nodes;
    private BigDecimal[][] distances;

    private static DistanceMatrix instance = null;

    /**
     * DistanceMatrix(ArrayList<Node> nodes) is the default constructor.
     * @param nodes all the nodes in the tsp.
     *
     * Initializes the Matrix.
     */
    private DistanceMatrix(ArrayList<Node> nodes){
        this.nodes = nodes;
        calculateDistances();
    }


    /**
     * initialize(ArrayList<Node> nodes) creates a new instance given all the nodes
     * @return instance
     * @param nodes all the nodes in the tsp.
     */
    public static DistanceMatrix initialize(ArrayList<Node> nodes) {
        if(instance == null) {
            instance = new DistanceMatrix(nodes);
        }
        return instance;
    }

    /**
     * getInstance returns the actual instance of the class
     * @return instance
     */
    public static DistanceMatrix getInstance() {
        if(instance == null) {
            return null;
        }
        return instance;
    }

    /**
     * calculateDistances calculates real distances between each node
     */
    private void calculateDistances(){
        int nodesSize = nodes.size();
        distances = new BigDecimal[nodesSize][nodesSize];

        for( int i=0; i<nodesSize; i++){

            for( int j=0; j<nodesSize; j++){
                distances[i][j] = BigDecimal.valueOf(Math.sqrt(abs(pow((nodes.get(i).getCoordinates().x - nodes.get(j).getCoordinates().x), 2) + abs(pow((nodes.get(i).getCoordinates().y - nodes.get(j).getCoordinates().y), 2)))));
            }
        }
        System.out.println("\n");
    }

    public BigDecimal getDistance (int node_1, int node_2){
        return distances[node_1][node_2];
    }

    public BigDecimal getDistance (Node node_1, Node node_2){
        return getDistance(node_1.getIndex(), node_2.getIndex());
    }

    public BigDecimal[][] getDistances(){
        return distances;
    }

    /**
     * getClosestNodes(Node node, ArrayList<Node> tsp) returns a matrix row ordered ascending
     * by the distance between the row index and all the other nodes
     * @return ArrayList<Node> all the nodes sorted by distance
     * @param node is a node which we are interested getting all the nodes distances from
     * @param tsp is the tsp containing all the nodes
     */
    public ArrayList<Node> getClosestNodes(Node node, ArrayList<Node> tsp) {
        BigDecimal[] distanceRow = distances[node.getIndex()];

        //A map default sorted by a property, here, the distance
        TreeMap<BigDecimal, Node> map = new TreeMap<>();

        for (int i=0; i<distanceRow.length; i++) {
            int finalI = i;
            Predicate<Node> predicate = c-> c.getIndex() == finalI;
            Node n = tsp.stream().filter(predicate).findFirst().get();
            if(n.getType().equals(Values.nodeType.WAREHOUSE)) continue;
            map.put(distanceRow[i], n);
        }

        return new ArrayList<Node>(map.values());
    }


}
