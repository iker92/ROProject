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
 * Created by loriz on 4/13/17.
 */
public class Route {

    public int MAX_WEIGHT = -1;

    private RouteListener mRouteListener = null;
    public int weightLinehaul = 0;
    public int weightBackhaul = 0;
    public ArrayList<Node> nodeList = new ArrayList<>();
    private DistanceMatrix distances = null;
    private BigDecimal actualDistance;
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

    public void setOnRouteChangeListener(RouteListener listener) {
        mRouteListener = listener;
    }

    public Route getCopy(){
        newRoute = new Route(MAX_WEIGHT);
        newRoute.nodeList = new ArrayList<>(this.nodeList);
        newRoute.actualDistance = this.actualDistance;
        newRoute.distances = this.distances;
        newRoute.weightBackhaul = this.weightBackhaul;
        newRoute.weightLinehaul = this.weightLinehaul;
        newRoute.MAX_WEIGHT = this.MAX_WEIGHT;

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
        if ( (node.getType() == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) + node.getWeight() > MAX_WEIGHT && !this.nodeList.contains(node)) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {
            nodeList.add(position, node);
            if (node.getType() == Values.nodeType.LINEHAUL)  {weightLinehaul += node.getWeight();} else {weightBackhaul += node.getWeight();}
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
        if ( node.getRoute() != this && (node.getType() == Values.nodeType.LINEHAUL ? weightLinehaul : weightBackhaul) + node.getWeight() > MAX_WEIGHT ) {
            throw new MaxWeightException("Cannot add node to route! Weight would exceed the maximum weight!");
        } else {

            if (node.getType() != Values.nodeType.WAREHOUSE) {

                if (nodeList.size() != 0 && nodeList.get(nodeList.size()-1).getType() == Values.nodeType.WAREHOUSE) {
                    nodeList.add(nodeList.size()-1, node);
                } else {
                    nodeList.add(node);
                }

                if (node.getType() == Values.nodeType.LINEHAUL) {
                    weightLinehaul += node.getWeight();
                } else {
                    weightBackhaul += node.getWeight();
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
            if (nodeList.get(position).getType() == Values.nodeType.LINEHAUL)  {weightLinehaul -= nodeList.get(position).getWeight();} else {weightBackhaul -= nodeList.get(position).getWeight();}
            nodeList.get(position).setRoute(null);
            nodeList.remove(position);
        } else {
            System.err.println("!!! Error - Node to remove is out of range !!!");
        }
        updateRouteDistance();
    }


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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////// MOVEMENT METHODS //////////////////////////////////////////////////

    public void swap(Node first, Node second) {
        //swap inside the same route

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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////// DISTANCE METHODS //////////////////////////////////////////////////

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

    public void forceUpdate() {
        updateRouteDistance();
    }

    public BigDecimal getActualDistance() {
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
        return ((node.getType() == Values.nodeType.LINEHAUL ? node.getWeight() + weightLinehaul : node.getWeight() + weightBackhaul) <= MAX_WEIGHT);
    }



    public boolean validate() {

        boolean valid = true;

        if (nodeList.get(0).getType() != Values.nodeType.WAREHOUSE || nodeList.get(nodeList.size()-1).getType() != Values.nodeType.WAREHOUSE) valid = false;


        for (int index = 1; index < nodeList.size()-1; index++) {
            if (nodeList.get(index).getType() == Values.nodeType.BACKHAUL && nodeList.get(index+1).getType() == Values.nodeType.LINEHAUL) valid = false;
        }

        if(this.nodeList.get(1).getType() == Values.nodeType.BACKHAUL) return false;

        return valid;

    }



    public interface RouteListener {
         void OnRouteChange(Route route, BigDecimal oldDistance);
    }

}

