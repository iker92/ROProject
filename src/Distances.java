import java.util.ArrayList;

import static java.lang.Math.abs;

/**
 * Created by andream16 on 13.04.17.
 */
public class Distances {

    public ArrayList<Node> nodes;
    private double[][] distances;

    private static Distances instance = null;

    public static Distances getInstance(ArrayList<Node> nodes) {
        if(instance == null) {
            instance = new Distances(nodes);
        }
        return instance;
    }

    public static Distances getInstance() {
        if(instance == null) {
           return null;
        }
        return instance;
    }


    private Distances(ArrayList<Node> nodes){
        this.nodes = nodes;
        calculateDistances();
    }

    private void calculateDistances(){
        int nodesSize = nodes.size();
        distances = new double[nodesSize][nodesSize];
        for( int i=0; i<nodesSize; i++){
            for( int j=0; j<nodesSize; j++){
                distances[i][j] = Math.sqrt(abs((nodes.get(i).coordinates.x - nodes.get(j).coordinates.x)) + abs(nodes.get(i).coordinates.y - nodes.get(j).coordinates.y));
            }
        }
    }

    public double getDistance (Node node_1, Node node_2){
        return distances[node_1.index][node_2.index];
    }

    public double[][] getDistances(){
        return distances;
    }

}
