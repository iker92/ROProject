/**
 * Created by pippo on 11/04/17.
 */
public class Node {

    Coordinates coordinates;
    Values.nodeType nodeType;
    int weight;
    boolean taken;
    int index = 0;

    public Node(Coordinates coordinates, Values.nodeType nodeType, int weight, boolean taken, int index){
        this.coordinates = coordinates;
        this.nodeType = nodeType;
        this.weight = weight;
        this.taken = taken;
        this.index = index;
    }

    public void take(Node node){
        node.taken = true;
    }

    public void release(Node node){
        node.taken = false;
    }

}
