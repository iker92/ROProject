import Utils.MaxWeightException;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;

/**
 * Created by loriz on 4/13/17.
 */
public class Route {

    public int MAX_WEIGHT = -1;

    public int weight = 0;
    public ArrayList<Node> nodeList = new ArrayList<>();
    private DistanceMatrix distances = null;
    private double actualDistance = 0.0;


    public Route(int maxWeight) {
        MAX_WEIGHT = maxWeight;

        distances = DistanceMatrix.getInstance();
        if (distances == null) {
            System.err.println("!!! Error - Distance Matrix wasn't initialized !!!");
        }

    }

    @Nullable
    public Node getNode(int position) {
        if (nodeList.size() != 0 && nodeList.size() > position) {
            return nodeList.get(position);
        } else {
            System.err.println("!!! Error - Node to get is out of range !!!");
        }
        return null;
    }

    public void addNode(int position, Node node) throws MaxWeightException{
        if (weight + node.weight > MAX_WEIGHT) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {
            nodeList.add(position, node);
            weight += node.weight;
        }
        node.setRoute(this);
        updateRouteDistance();
    }

    public void addNode(Node node) throws MaxWeightException{
        if (weight + node.weight > MAX_WEIGHT) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {
            nodeList.add(node);
            weight += node.weight;
        }
        node.setRoute(this);
        updateRouteDistance();
    }


    public void removeNode(int position) {

        if (nodeList.size() > position) {
            weight -= nodeList.get(position).weight;
            nodeList.get(position).setRoute(null);
            nodeList.remove(position);
        } else {
            System.err.println("!!! Error - Node to remove is out of range !!!");
        }
        updateRouteDistance();
    }


    public void removeNode(Node node) {
        if (nodeList.contains(node)) {
            weight -= node.weight;
            nodeList.remove(node);
        } else {
            System.err.println("!!! Error - Node to remove wasn't found in this route !!!");
        }
        node.setRoute(null);
        updateRouteDistance();
    }

    private void updateRouteDistance() {
        if (nodeList.size() > 1 && distances != null) {

            for (int i = 0; i < nodeList.size(); i++) {
                actualDistance += distances.getDistance(nodeList.get(i), nodeList.get((i + 1) % nodeList.size()));
            }

        } else actualDistance = 0.0;
    }

    public double getActualDistance() {
        return actualDistance;
    }
}

