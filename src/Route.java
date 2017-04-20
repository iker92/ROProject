import utils.MaxWeightException;
import com.sun.istack.internal.Nullable;
import utils.NodeNotFoundException;

import java.util.ArrayList;

/**
 * Created by loriz on 4/13/17.
 */
public class Route {

    public int MAX_WEIGHT = -1;

    public int weightLinehaul = 0;
    public int weightBackhaul = 0;
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////// METHODS TO ADD NODES ///////////////////////////////////////////////

    public void addNode(int position, Node node) throws MaxWeightException{
        if ( (node.nodeType == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) + node.weight > MAX_WEIGHT) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {
            nodeList.add(position, node);
            if (node.nodeType == Values.nodeType.LINEHAUL)  {weightLinehaul += node.weight;} else {weightBackhaul += node.weight;}
        }
        node.setRoute(this);
        updateRouteDistance();
    }

    public void addNode(Node node) throws MaxWeightException{
        if ( (node.nodeType == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) + node.weight > MAX_WEIGHT) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {
            nodeList.add(node);
            if (node.nodeType == Values.nodeType.LINEHAUL)  {weightLinehaul += node.weight;} else {weightBackhaul += node.weight;}
        }
        node.setRoute(this);
        updateRouteDistance();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////// METHODS TO REMOVE NODES ///////////////////////////////////////////////

    public void removeNode(int position) {


        if (nodeList.size() > position) {
            if (nodeList.get(position).nodeType == Values.nodeType.LINEHAUL)  {weightLinehaul -= nodeList.get(position).weight;} else {weightBackhaul -= nodeList.get(position).weight;}
            nodeList.get(position).setRoute(null);
            nodeList.remove(position);
        } else {
            System.err.println("!!! Error - Node to remove is out of range !!!");
        }
        updateRouteDistance();
    }


    public void removeNode(Node node) {
        if (nodeList.contains(node)) {
            if (node.nodeType == Values.nodeType.LINEHAUL)  {weightLinehaul -= node.weight;} else {weightBackhaul -= node.weight;}
            nodeList.remove(node);
        } else {
            System.err.println("!!! Error - Node to remove wasn't found in this route !!!");
        }
        node.setRoute(null);
        updateRouteDistance();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////// DISTANCE METHODS //////////////////////////////////////////////////

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

    public int getIndexByNode(Node node) throws NodeNotFoundException {
        if (nodeList.contains(node)) {
            return nodeList.indexOf(node);
        } else {
            throw new NodeNotFoundException("Node was not found in route!");
        }
    }

    public Node getNodeByIndex(int index) throws NodeNotFoundException {
        if (index >= 0 && index < nodeList.size()) {
            return nodeList.get(index);
        } else {
            throw new NodeNotFoundException("Index exceeds the number of nodes!");
        }
    }

    public boolean canAdd(Node node) {
        return ((node.nodeType == Values.nodeType.LINEHAUL ? node.weight + weightLinehaul : node.weight + weightBackhaul) <= MAX_WEIGHT);
    }
}

