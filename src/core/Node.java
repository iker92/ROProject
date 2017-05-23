package core;

/**
 * Node is the class which represents a single Node of the Routes. Every Node has some proprieties and and references to
 * the other structures where it is located (such as the Route).
 *
 * The Node can also be taken and released, a property which helps in determining if a node has been completely optimized.
 */
public class Node {

    private Values.nodeType nodeType;
    private Coordinates coordinates;
    private Route route = null;
    private int weight;
    private boolean taken = false;
    private int index = 0;


    /**
     * Node(Coordinates coordinates, Values.nodeType nodeType, int weight, boolean taken, int index) is the default constructor.
     * For a node to be complete and correct, its properties must be initialized upon its creation.
     * @param coordinates is the Coordinates instance of the Node, which represents its spatial location.
     * @param nodeType is the kind of the Node, which can be set to LINEHAUL, BACKHAUL or WAREHOUSE
     * @param weight is the weight of the Node, directly bond to the nodeType
     * @param taken is the initial value of a Node, usually set as false upon creation
     * @param index is the index of the node inside the tsp.
     */
    public Node(Coordinates coordinates, Values.nodeType nodeType, int weight, boolean taken, int index){
        this.coordinates = coordinates;
        this.nodeType = nodeType;
        this.weight = weight;
        this.taken = taken;
        this.index = index;
    }


    ///////////////////////////////////////////////// METHODS //////////////////////////////////////////////////////////

    public void take(){
        this.taken = true;
    }

    public void release(){
        this.taken = false;
    }

    public boolean isTaken() { return taken; }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Route getRoute() {
        return route;
    }

    public Values.nodeType getType() {
        return nodeType;
    }

    public Coordinates getCoordinates() { return coordinates; }

    public int getWeight() { return weight; }

    public int getIndex() { return index; }
}
