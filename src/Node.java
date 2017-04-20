/**
 * Created by pippo on 11/04/17.
 */
public class Node {

    Values.nodeType nodeType;
    Coordinates coordinates;
    private Route route = null;
    int weight;
    boolean taken;
    int index = 0;

    public Node(Coordinates coordinates, Values.nodeType nodeType, int weight, boolean taken, int index){
        this.coordinates = coordinates;
        this.nodeType = nodeType;
        this.weight = nodeType == Values.nodeType.BACKHAUL ? -weight :  weight;
        this.taken = taken;
        this.index = index;
    }

    public void take(Node node){
        node.taken = true;
    }

    public void release(Node node){
        node.taken = false;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Route getRoute() {
        return route;
    }

}
