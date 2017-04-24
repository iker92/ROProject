import core.Coordinates;

/**
 * Created by pippo on 11/04/17.
 */
public class Node {

    private Values.nodeType nodeType;
    Coordinates coordinates;
    private Route route = null;
    int weight;
    boolean taken = false;
    int index = 0;

    public Node(Coordinates coordinates, Values.nodeType nodeType, int weight, boolean taken, int index){
        this.coordinates = coordinates;
        this.nodeType = nodeType;
        this.weight = weight;
        this.taken = taken;
        this.index = index;
    }

    public void take(){
        this.taken = true;
    }

    public void release(){
        this.taken = false;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Route getRoute() {
        return route;
    }

    public Values.nodeType getType() {
        return nodeType;
    }

}
