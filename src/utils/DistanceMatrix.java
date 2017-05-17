package utils;

import core.Node;
import core.Values;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 * Created by andream16 on 13.04.17.
 */
public class DistanceMatrix {

    public ArrayList<Node> nodes;
    private BigDecimal[][] distances;

    private static DistanceMatrix instance = null;

    public static DistanceMatrix initialize(ArrayList<Node> nodes) {
        if(instance == null) {
            instance = new DistanceMatrix(nodes);
        }
        return instance;
    }

    public static DistanceMatrix getInstance() {
        if(instance == null) {
            return null;
        }
        return instance;
    }


    private DistanceMatrix(ArrayList<Node> nodes){
        this.nodes = nodes;
        calculateDistances();
    }

    private void calculateDistances(){
        int nodesSize = nodes.size();
        distances = new BigDecimal[nodesSize][nodesSize];

        System.out.printf("\t\t");

        for( int i=0; i<nodesSize; i++) {
            System.out.printf(i + "\t\t");
        }

        System.out.printf("\n");


        for( int i=0; i<nodesSize; i++){
            System.out.printf(i + "\t");

            for( int j=0; j<nodesSize; j++){
                distances[i][j] = BigDecimal.valueOf(Math.sqrt(abs(pow((nodes.get(i).getCoordinates().x - nodes.get(j).getCoordinates().x), 2) + abs(pow((nodes.get(i).getCoordinates().y - nodes.get(j).getCoordinates().y), 2)))));
                System.out.printf("%.2f\t",distances[i][j]);
            }
            System.out.print("\n\n");
        }
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

    public ArrayList<Node> getClosestNodes(Node node, ArrayList<Node> tsp) {
        BigDecimal[] distanceRow = distances[node.getIndex()];

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
