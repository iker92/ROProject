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


    public Route(int maxWeight) {
        MAX_WEIGHT = maxWeight;
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
    }

    public void addNode(Node node) throws MaxWeightException{
        if (weight + node.weight > MAX_WEIGHT) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {
            nodeList.add(node);
            weight += node.weight;
        }
    }


    public void removeNode(int position) {

        if (nodeList.size() > position) {
            weight -= nodeList.get(position).weight;
            nodeList.remove(position);
        } else {
            System.err.println("!!! Error - Node to remove is out of range !!!");
        }



    }

    public void removeNode(Node node) {
        if (nodeList.contains(node)) {
            weight -= node.weight;
            nodeList.remove(node);
        } else {
            System.err.println("!!! Error - Node to remove wasn't found in this route !!!");
        }


    }

}

