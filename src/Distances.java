import java.util.ArrayList;

/**
 * Created by andream16 on 13.04.17.
 */
public class Distances {

    public ArrayList<Node> nodes;
    private double[][] distances;

    public Distances(ArrayList<Node> nodes){
        this.nodes = nodes;
    }

    public double [][] calculateDistances (ArrayList<Node> nodes){
        int nodesSize = nodes.size();
        distances = new double[nodesSize][nodesSize];
        for( int i=0; i<=nodesSize; i++){
            for( int j=0; j<=nodesSize; j++){
                distances[i][j] = Math.sqrt((nodes.get(i).coordinates.x - nodes.get(j).coordinates.x) + (nodes.get(i).coordinates.y - nodes.get(j).coordinates.y));
            }
        }

        return distances;

    }

    public double getDistance (Node node_1, Node node_2){
        return distances[node_1.index][node_2.index];
    }

    public double[][] getDistances(){
        return distances;
    }

}
