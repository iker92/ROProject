import java.math.BigDecimal;
import java.util.ArrayList;

import static java.lang.Math.abs;

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
                distances[i][j] = BigDecimal.valueOf(Math.sqrt(abs((nodes.get(i).coordinates.x - nodes.get(j).coordinates.x)) + abs(nodes.get(i).coordinates.y - nodes.get(j).coordinates.y)));
                System.out.printf("%.2f\t",distances[i][j]);
            }
            System.out.print("\n\n");
        }
    }

    public BigDecimal getDistance (int node_1, int node_2){
        return distances[node_1][node_2];
    }

    public BigDecimal getDistance (Node node_1, Node node_2){
        return getDistance(node_1.index, node_2.index);
    }

    public BigDecimal[][] getDistances(){
        return distances;
    }

}
