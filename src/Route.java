import utils.ImproperUsageException;
import utils.MaxWeightException;
import com.sun.istack.internal.Nullable;
import utils.NodeNotFoundException;

import java.util.ArrayList;
import java.util.Collections;

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
    private Helper helper;
    Route newRoute = null;


    public Route(int maxWeight) {
        MAX_WEIGHT = maxWeight;

        helper = new Helper();

        distances = DistanceMatrix.getInstance();
        if (distances == null) {
            System.err.println("!!! Error - Distance Matrix wasn't initialized !!!");
        }
    }

    public Route getCopyOfRoute(Route route){
        newRoute = new Route(MAX_WEIGHT);
        newRoute.nodeList = new ArrayList<>(route.nodeList);
        newRoute.actualDistance = route.actualDistance;
        newRoute.distances = route.distances;
        newRoute.weightBackhaul = route.weightBackhaul;
        newRoute.weightLinehaul = route.weightLinehaul;
        newRoute.MAX_WEIGHT = route.MAX_WEIGHT;

        return newRoute;
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
        if ( (node.getType() == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) + node.weight > MAX_WEIGHT && !this.nodeList.contains(node)) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {
            nodeList.add(position, node);
            if (node.getType() == Values.nodeType.LINEHAUL)  {weightLinehaul += node.weight;} else {weightBackhaul += node.weight;}
        }

        if (node.getType() != Values.nodeType.WAREHOUSE) {
            if (node.getRoute() != null) {
                node.getRoute().removeNode(node);
            }
            node.setRoute(this);
        }

        updateRouteDistance();
    }

    public void addNode(Node node) throws MaxWeightException{
        if ( node.getRoute() != this && (node.getType() == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) + node.weight > MAX_WEIGHT ) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {

            if (node.getType() != Values.nodeType.WAREHOUSE) {

                if (nodeList.size() != 0 && nodeList.get(nodeList.size()-1).getType() == Values.nodeType.WAREHOUSE) {
                    nodeList.add(nodeList.size()-1, node);
                } else {
                    nodeList.add(node);
                }

                if (node.getType() == Values.nodeType.LINEHAUL) {
                    weightLinehaul += node.weight;
                } else {
                    weightBackhaul += node.weight;
                }

                if (node.getRoute() != null) {
                    node.getRoute().removeNode(node);
                }
                node.setRoute(this);

            } else {

                nodeList.add(node);

            }


            updateRouteDistance();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////// METHODS TO REMOVE NODES ///////////////////////////////////////////////

    public void removeNode(int position) {


        if (nodeList.size() > position) {
            if (nodeList.get(position).getType() == Values.nodeType.LINEHAUL)  {weightLinehaul -= nodeList.get(position).weight;} else {weightBackhaul -= nodeList.get(position).weight;}
            nodeList.get(position).setRoute(null);
            nodeList.remove(position);
        } else {
            System.err.println("!!! Error - Node to remove is out of range !!!");
        }
        updateRouteDistance();
    }


    public void removeNode(Node node) {
        if (nodeList.contains(node)) {
            if (node.getType() == Values.nodeType.LINEHAUL)  {weightLinehaul -= node.weight;} else {weightBackhaul -= node.weight;}
            nodeList.remove(node);
        } else {
            System.err.println("!!! Error - Node to remove wasn't found in this route !!!");
        }
        node.setRoute(null);
        updateRouteDistance();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////// MOVEMENT METHODS //////////////////////////////////////////////////

    //TODO: requires testing! Highly experimental!
    public void swap(Node first, Node second) {
        //swap inside the same route

        try {
            if (first.getType() != Values.nodeType.WAREHOUSE && second.getType() != Values.nodeType.WAREHOUSE) {

                if (first.getRoute() == second.getRoute() && first.getRoute() == this && canSwap(first, second)){
                    Collections.swap(nodeList, nodeList.indexOf(first), nodeList.indexOf(second));
                }

            } else {
                throw new ImproperUsageException();
            }

        } catch (ImproperUsageException e) {
            if (first.getRoute() != second.getRoute()) {
                System.err.println("!!! Improper usage of Route.swap() method! Maybe you were looking for Helper.swapNodes()? !!!");
            } else {
                System.err.println("!!! Illegal move detected! Did you try putting a BACKHAUL after a LINEHAUL or moving a WAREHOUSE? !!!");
            }
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////// DISTANCE METHODS //////////////////////////////////////////////////

    private void updateRouteDistance() {
        if (nodeList.size() > 1 && distances != null) {

            actualDistance = 0.0;


            //TODO: skip last check (distance betwen warehouse and warehouse)
            for (int i = 0; i < nodeList.size(); i++) {
                actualDistance += distances.getDistance(nodeList.get(i), nodeList.get((i + 1) % nodeList.size()));
            }

        } else actualDistance = 0.0;
    }

    public double getActualDistance() {
        return actualDistance;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////// OTHER METHODS //////////////////////////////////////////////////

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
        return ((node.getType() == Values.nodeType.LINEHAUL ? node.weight + weightLinehaul : node.weight + weightBackhaul) <= MAX_WEIGHT);
    }

    // TODO: Highly experimental! To be deeply tested (all tests succeded so far).
    // canSwap MUST BE CALLED with the node of the calling route as first parameter
    public boolean canSwap(Node internal, Node external) {

        Values.nodeType inType = internal.getType();
        Values.nodeType exType = external.getType();

        // if at least one is WAREHOUSE, cannot swap
        if (inType == Values.nodeType.WAREHOUSE || exType == Values.nodeType.WAREHOUSE) {
            return false;
        } else {

            // if the first node passed is part of this route...
            if (internal.getRoute() == this && external.getRoute() != this) {

                Node nextIntNode = internal.getRoute().nodeList.get(internal.getRoute().nodeList.indexOf(internal) + 1);
                Node prevIntNode = internal.getRoute().nodeList.get(internal.getRoute().nodeList.indexOf(internal) - 1);

                // if BACKHAUL -> LINEHAUL, the current internal prev cannot be BACKHAUL
                // if LINEHAUL -> BACKHAUL, the current internal next cannot be LINEHAUL
                if (inType != exType) {

                    if ((inType == Values.nodeType.BACKHAUL && prevIntNode.getType() == Values.nodeType.BACKHAUL
                            || inType == Values.nodeType.LINEHAUL && nextIntNode.getType() == Values.nodeType.LINEHAUL)) {
                        return false;
                    }

                    int actualInTypeWeight = (inType == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) - internal.weight;
                    int actualExTypeWeight = (exType == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) + external.weight;
                    return (actualInTypeWeight <= MAX_WEIGHT && actualExTypeWeight <= MAX_WEIGHT);

                } else {

                    int actualWeight = (inType == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) - internal.weight + external.weight;
                    return actualWeight <= MAX_WEIGHT;
                }


            } else if (internal.getRoute() == external.getRoute() && internal.getRoute() == this){
                return inType == exType;

            } else {
                try {
                    throw new ImproperUsageException();
                } catch (ImproperUsageException e) {
                    System.err.println("!!! Wrong usage of canSwap method! At least the first parameter's node must be part of this route! !!!");
                }
                return false;
            }


        }


    }


    public boolean validate() {

        boolean valid = true;

        if (nodeList.get(0).getType() != Values.nodeType.WAREHOUSE || nodeList.get(nodeList.size()-1).getType() != Values.nodeType.WAREHOUSE) valid = false;


        for (int index = 1; index < nodeList.size()-1; index++) {
            if (nodeList.get(index).getType() == Values.nodeType.BACKHAUL && nodeList.get(index+1).getType() == Values.nodeType.LINEHAUL) valid = false;
        }

        return valid;

    }
}

