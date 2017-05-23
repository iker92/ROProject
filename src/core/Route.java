package core;

import utils.DistanceMatrix;
import utils.Helper;
import exceptions.ImproperUsageException;
import exceptions.MaxWeightException;
import com.sun.istack.internal.Nullable;
import exceptions.NodeNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Route is the class which represents a Route of the problem and its characteristics, such as the nodes who compose it,
 * the weights of all BackHauls and LineHauls, the cost/objective function of the Route etc.
 * In this class are also defined some Route-oriented methods such as addNode and removeNode and their variation, which behave
 * as a check to ensure that the Routes are sane (for instance, avoiding to exceed the maximum weights, set the Route of new or removed
 * nodes etc.)
 */
public class Route {


    public int MAX_WEIGHT = -1;

    private RouteListener mRouteListener = null;
    public int weightLinehaul = 0;
    public int weightBackhaul = 0;
    private ArrayList<Node> nodeList = new ArrayList<>();
    private DistanceMatrix distances = null;
    private BigDecimal actualDistance;


    /**
     * setOnRouteChangeListener relies on the Listener interface to communicate with the RouteList it belongs to to request
     * a global objective function update.
     * @param listener
     */
    public void setOnRouteChangeListener(RouteListener listener) {
        mRouteListener = listener;
    }


    /**
     * Route(int maxWeight) is the default constructor, which creates an empty Route, sets its max weight and recovers
     * the instance of DistanceMatrix.
     * @param maxWeight
     */
    public Route(int maxWeight) {
        MAX_WEIGHT = maxWeight;

        distances = DistanceMatrix.getInstance();
        if (distances == null) {
            System.err.println("!!! Error - Distance Matrix wasn't initialized !!!");
        }
    }


    /**
     * getCopy() is the method which returns a copy of the actual Route. Remember that all Nodes are used by reference, this
     * doesn't duplicate them.
     * @return
     */
    public Route getCopy(){
        Route newRoute = new Route(MAX_WEIGHT);
        newRoute.nodeList = new ArrayList<>(this.nodeList);
        newRoute.actualDistance = this.actualDistance;
        newRoute.distances = this.distances;
        newRoute.weightBackhaul = this.weightBackhaul;
        newRoute.weightLinehaul = this.weightLinehaul;
        newRoute.MAX_WEIGHT = this.MAX_WEIGHT;

        return newRoute;
    }


    /////////////////////////////////////////// METOD TO RETRIEVE NODES ////////////////////////////////////////////////

    /**
     * getNode(int position) is a method which returns the Node occupying the position "position" in the Route. If the position
     * is not found, returns null
     * @param position is the index of the desired node
     * @return the Node at position position if present, else null
     */
    @Nullable
    public Node getNode(int position) {
        if (nodeList.size() != 0 && nodeList.size() > position) {
            return nodeList.get(position);
        } else {
            System.err.println("!!! Error - Node to get is out of range !!!");
        }
        return null;
    }


    ///////////////////////////////////////////// METHODS TO ADD NODES ///////////////////////////////////////////////

    /**
     * addNode(int position, Node node) is the method which adds the passed Node in passed position if possible.
     * This method proceeds to addition only if:
     * - the addition of the node wouldn't exceed the Route nodeType's actual weight (LINEHAUL or BACKHAUL)
     * When the addition is possible, the methods proceeds to remove the Node from the previous Route it belonged to (if any)
     * and set its belonging to the actual Route.
     * @throws MaxWeightException if trying to add a node which would exceed the maximum weight of the route
     */
    public void addNode(int position, Node node) throws MaxWeightException{

        if ( (node.getType() == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) + node.getWeight() > MAX_WEIGHT && !this.nodeList.contains(node)) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {
            //proceed to add the Node to the internal nodeList and update the weight of the same Type
            nodeList.add(position, node);
            if (node.getType() == Values.nodeType.LINEHAUL)  {weightLinehaul += node.getWeight();} else {weightBackhaul += node.getWeight();}
        }

        //the Route of belonging is never set to the WAREHOUSE, beacuse it is a special nodes which belongs to every route
        if (node.getType() != Values.nodeType.WAREHOUSE) {
            if (node.getRoute() != null) {
                //remove the Node from the previous Route
                node.getRoute().removeNode(node);
            }
            //assign the Node to this Route
            node.setRoute(this);
        }

        //request an update of the Route distance (local objective function)
        updateRouteDistance();
    }


    /**
     * addNode(Node node) is the method which adds the passed Node to the internal nodeList
     * This method proceeds to addition only if:
     * - the addition of the node wouldn't exceed the Route nodeType's actual weight (LINEHAUL or BACKHAUL)
     * When the addition is possible, the methods proceeds to remove the Node from the previous Route it belonged to (if any)
     * and set its belonging to the actual Route.
     * @throws MaxWeightException if trying to add a node which would exceed the maximum weight of the route
     */
    public void addNode(Node node) throws MaxWeightException{
        if ( node.getRoute() != this && (node.getType() == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) + node.getWeight() > MAX_WEIGHT ) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {

            if (node.getType() != Values.nodeType.WAREHOUSE) {

                // manage the addition of the node in the last position (BEFORE the WAREHOUSE)
                if (nodeList.size() != 0 && nodeList.get(nodeList.size()-1).getType() == Values.nodeType.WAREHOUSE) {
                    nodeList.add(nodeList.size()-1, node);
                } else {
                    nodeList.add(node);
                }

                //adds the weight to the Route's one of the same Type
                if (node.getType() == Values.nodeType.LINEHAUL) {
                    weightLinehaul += node.getWeight();
                } else {
                    weightBackhaul += node.getWeight();
                }

                //remove Node from old Route if any and set it to the new one
                if (node.getRoute() != null) {
                    node.getRoute().removeNode(node);
                }
                node.setRoute(this);

            } else {

                //if the Node is a WAREHOUSE, just add it to the nodeList (no checks needed)
                nodeList.add(node);

            }

            //request an update of the local objective function
            updateRouteDistance();
        }
    }


    ////////////////////////////////////////// METHODS TO REMOVE NODES ///////////////////////////////////////////////

    /**
     * removeNode(int position) removes the Node occupying the input position in the nodeList. If the position is not in the
     * range of the nodeList, returns an error.
     * This method also removes the Route reference from the Node and request an update of the objective function.
     * @param position is the position of the Node to remove.
     */
    public void removeNode(int position) {

        if (nodeList.size() > position) {
            if (nodeList.get(position).getType() == Values.nodeType.LINEHAUL)  {weightLinehaul -= nodeList.get(position).getWeight();} else {weightBackhaul -= nodeList.get(position).getWeight();}
            nodeList.get(position).setRoute(null);
            nodeList.remove(position);
        } else {
            System.err.println("!!! Error - Node to remove is out of range !!!");
        }
        updateRouteDistance();
    }


    /**
     * removeNode(Node node) removes the Node from the nodeList if present, else returns an error.
     * This method also removes the Route reference from the Node and request an update of the objective function.
     * @param node is the Node to remove.
     */
    public void removeNode(Node node) {
        if (nodeList.contains(node)) {
            if (node.getType() == Values.nodeType.LINEHAUL)  {weightLinehaul -= node.getWeight();} else {weightBackhaul -= node.getWeight();}
            nodeList.remove(node);
        } else {
            System.err.println("!!! Error - Node to remove wasn't found in this route !!!");
        }
        node.setRoute(null);
        updateRouteDistance();
    }


    ////////////////////////////////////////////// MOVEMENT METHODS //////////////////////////////////////////////////

    /**
     * swap(Node first, Node second) is a method which swaps two nodes in the same Route.
     * This has some conditions to met such as:
     * - the nodes must be on the same route
     * - the nodes must not be WAREHOUSEs
     * @param first is the first Node
     * @param second is the secondo Node
     */
    public void swap(Node first, Node second) {
        try {
            if (first.getType() != Values.nodeType.WAREHOUSE && second.getType() != Values.nodeType.WAREHOUSE) {

                if (first.getRoute() == second.getRoute() && first.getRoute() == this){
                    Collections.swap(nodeList, nodeList.indexOf(first), nodeList.indexOf(second));
                    //Update ObjectiveFunction with new distance
                    updateRouteDistance();
                }

            } else {
                throw new ImproperUsageException();
            }

        } catch (ImproperUsageException e) {
            if (first.getRoute() != second.getRoute()) {
                System.err.println("!!! Improper usage of Route.swap() method! Maybe you were looking for utils.Helper.swapNodes()? !!!");
            } else {
                System.err.println("!!! Illegal move detected! Did you try putting a BACKHAUL after a LINEHAUL or moving a WAREHOUSE? !!!");
            }
        }
    }


    ////////////////////////////////////////////// DISTANCE METHODS //////////////////////////////////////////////////

    /**
     * updateRouteDistance() is the method which updates the local Objective Function and requires a global objective function update
     * to the RouteList via the implemented interface
     */
    private void updateRouteDistance() {

        BigDecimal oldDistance = actualDistance;

        if (nodeList.size() > 1 && distances != null) {

            actualDistance = new BigDecimal(0);

            for (int i = 0; i < nodeList.size() -1; i++) {
                actualDistance = actualDistance.add(distances.getDistance(nodeList.get(i), nodeList.get(i + 1)));
            }

        } else actualDistance = new BigDecimal(0);

        if (mRouteListener != null) {
            mRouteListener.OnRouteChange(this, oldDistance);
        }

    }


    public void forceUpdate() { updateRouteDistance(); }


    public BigDecimal getActualDistance() { return actualDistance; }


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


    public ArrayList<Node> getNodeList() { return nodeList; }


    /**
     * validate() is the method which validates a route, by checking that:
     * - first and last Nodes are WAREHOUSEs
     * - there are always at least one LINEHAUL
     * - the LINEHAULs are always before the BACKHAULs (if present)
     * - there are no BACKHAUL-only Routes
     * - there are no empty route
     * @return true if the Route is valid
     */
    public boolean validate() {

        boolean valid = true;

        if (nodeList.get(0).getType() != Values.nodeType.WAREHOUSE || nodeList.get(nodeList.size()-1).getType() != Values.nodeType.WAREHOUSE) valid = false;


        for (int index = 1; index < nodeList.size()-1; index++) {
            if (nodeList.get(index).getType() == Values.nodeType.BACKHAUL && nodeList.get(index+1).getType() == Values.nodeType.LINEHAUL) valid = false;
        }

        if(nodeList.get(1).getType() != Values.nodeType.LINEHAUL) return false;

        return valid;

    }


    ////////////////////////////////////////////// INTERFACES //////////////////////////////////////////////////////////

    /**
     * RouteListener is an inner interface class that binds Route to its RouteList when implemented, allowing to update
     * automatically the global objective function when the Route gets updated.
     */
    public interface RouteListener {
        void OnRouteChange(Route route, BigDecimal oldDistance);
    }

}

