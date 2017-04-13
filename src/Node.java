/**
 * Created by pippo on 11/04/17.
 */
public class Node {

    public Coordinates coordinates;
    public Values.nodeType nodeType;
    public int weight;
    public boolean taken;

    public Node(Coordinates coordinates, Values.nodeType nodeType, int weight, boolean taken){
        this.coordinates = coordinates;
        this.nodeType = nodeType;
        this.weight = weight;
        this.taken = taken;
    }

}
