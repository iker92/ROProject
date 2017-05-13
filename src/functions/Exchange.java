package functions;

import core.*;
import utils.DistanceMatrix;
import utils.Helper;
import exceptions.MaxWeightException;
import exceptions.NodeNotFoundException;
import exceptions.SwapFailedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Created by andream16 on 25.04.17.
 */
public class Exchange {

    private final ArrayList<Node> tsp;
    private Boolean isDebug = Values.isDebug();

    RouteList routes;
    Helper helper;

    public Exchange(RouteList routes, Helper helper, ArrayList<Node> completeTSP){
        this.routes = routes;
        this.helper = helper;
        this.tsp = completeTSP;
    }

    public RouteList findBestExchange() throws MaxWeightException, NodeNotFoundException {

        //ALWAYS take the warehouse
        tsp.get(0).take();

        boolean isDone = false;
        int steps = 0;

        while (!isDone){

            isDone = true;
            //For each route
            for(int routeIndex = 0; routeIndex < routes.size(); routeIndex++){
                //Initializing map to contain the current best move to do.
                TreeMap<BigDecimal, Node> bestMove = new TreeMap(Collections.reverseOrder());

                Route route = routes.get(routeIndex);
                ArrayList<Node> currentNodes = route.nodeList;
                int currentRouteSize = currentNodes.size();
                BigDecimal routeWeight = route.getActualDistance();

                //For each node in current route except first and last (WAREHOUSE)
                for(int nodeIndex=1; nodeIndex <= currentRouteSize-2; nodeIndex++){

                    //Get Current Node
                    Node currentNode = currentNodes.get(nodeIndex);

                    /** Swaps on the same Route **/
                    for(int innerNodeIndex=1; innerNodeIndex <= currentRouteSize-2; innerNodeIndex++) {

                        //Get Current Inner Node
                        Node currentInnerNode = currentNodes.get(innerNodeIndex);

                        //If we are analyzing the same node, or one or both of them are WAREHOUSE nodes, skip
                        if(currentNode.equals(currentInnerNode) || currentNode.getType().equals(Values.nodeType.WAREHOUSE) || currentInnerNode.getType().equals(Values.nodeType.WAREHOUSE)) continue;

                        if (canSwap(currentNode, currentInnerNode)) {
                            //Current Route Actual Distance
                            BigDecimal oldObjFun = routes.getObjectiveFunction();

                            BigDecimal newObjFun = new BigDecimal(0);

                            for (Route inner : routes) {
                                if (!inner.equals(route) && inner != currentInnerNode.getRoute()) {
                                    newObjFun = newObjFun.add(inner.getActualDistance());

                                } else if (inner == currentNode.getRoute()){

                                    newObjFun = newObjFun.add(simulateExchange(currentNode, currentInnerNode));

                                } else {

                                    newObjFun = newObjFun.add(simulateExchange(currentInnerNode, currentNode));

                                }
                            }


                            // ex1.compareTo(ex2)
                            // returns -1 if ex2 > ex1
                            // returns 1 if ex1 > ex2
                            // returns 0 if equal
                            if(oldObjFun.compareTo(newObjFun) == 1){
                                //If so, put currentNode and Its bound value to bestMove map
                                bestMove.put(newObjFun, currentInnerNode);
                            }
                        }
                    }

                    /** Swaps on Other Routes **/
                    for(int currentRouteIndex = 0; currentRouteIndex < routes.size(); currentRouteIndex++){

                        //Get Current Route
                        Route currentRoute = routes.get(currentRouteIndex);

                        //If we are analyzing same routes, skip
                        if(currentRoute.equals(route)) continue;

                        //Get currentRoute actual distance
                        BigDecimal currentRouteActualDistance = currentRoute.getActualDistance();

                        //Get Route's Actual Distance + Original Route's one
                        BigDecimal currentActualDistance = currentRouteActualDistance.add(routeWeight);

                        //For each node inside currentRoute
                        for(int currentRouteNodeIndex = 1; currentRouteNodeIndex <= currentRoute.nodeList.size()-2; currentRouteNodeIndex++){

                            //Get currentRoute node
                            Node currentRouteNode = currentRoute.nodeList.get(currentRouteNodeIndex);

                            //If it is possible to swap
                            if( canSwap(currentNode, currentRouteNode) && canSwap(currentRouteNode, currentNode) ){

                                BigDecimal oldObjFun = routes.getObjectiveFunction();

                                BigDecimal newObjFun = new BigDecimal(0);

                                for (Route inner : routes) {
                                    if (inner != currentNode.getRoute() && inner != currentRouteNode.getRoute()) {
                                        newObjFun = newObjFun.add(inner.getActualDistance());

                                    } else if (inner == currentNode.getRoute()){

                                        newObjFun = newObjFun.add(simulateExchange(currentNode, currentRouteNode));

                                    } else {

                                        newObjFun = newObjFun.add(simulateExchange(currentRouteNode, currentNode));

                                    }
                                }

                                //If it is worth indeed
                                if(oldObjFun.compareTo(newObjFun) == 1){
                                    //Then add current Node and its weight to the map
                                    bestMove.put(newObjFun, currentRouteNode);
                                }

                            }
                        }

                    }

                    //If the map has at least one candidate
                    if(!bestMove.isEmpty()){
                        //Get the best node from it
                        BigDecimal bestNodeToSwap = bestMove.lastKey();
                        try {
                            swapNodes(currentNode, bestMove.get(bestNodeToSwap));
                            steps++;
                            System.out.println("Exchanged node " + currentNode.index + " with node  " + bestMove.get(bestNodeToSwap).index);
                            bestMove.clear();
                            isDone = false;
                            helper.printRoutes(routes);

                            for (Node node : tsp) {
                                if (node.getType() == Values.nodeType.WAREHOUSE) continue;
                                node.release();
                            }


                        } catch (SwapFailedException e) {}
                    } else {

                        tsp.get(tsp.indexOf(currentNode)).take();

                        for (Node node : tsp) {
                            if (node.taken == false) {
                                isDone = false;
                                break;
                            }
                        }

                    }

             
                }

            }
        }

        System.out.println("\nExchange successfully terminated!\nExchange moves done: " + steps + "\nObjective Function: " + routes.getObjectiveFunction().toString());

        for (Node node : tsp) {
            if (node.getType() == Values.nodeType.WAREHOUSE) continue;
            node.release();
        }

        return routes;

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
            if (internal.getRoute() != external.getRoute()) {

                Node nextIntNode = internal.getRoute().nodeList.get(internal.getRoute().nodeList.indexOf(internal) + 1);
                Node prevIntNode = internal.getRoute().nodeList.get(internal.getRoute().nodeList.indexOf(internal) - 1);

                // if BACKHAUL -> LINEHAUL, the current internal prev cannot be BACKHAUL
                // if LINEHAUL -> BACKHAUL, the current internal next cannot be LINEHAUL
                if (inType != exType) {

                    if ((inType == Values.nodeType.BACKHAUL && prevIntNode.getType() == Values.nodeType.BACKHAUL || inType == Values.nodeType.LINEHAUL && (nextIntNode.getType() == Values.nodeType.LINEHAUL || prevIntNode.getType() != Values.nodeType.LINEHAUL))) {
                        return false;
                    }

                    int actualInTypeWeight = (inType == Values.nodeType.LINEHAUL ? internal.getRoute().weightLinehaul : internal.getRoute().weightBackhaul) - internal.weight;
                    int actualExTypeWeight = (exType == Values.nodeType.LINEHAUL ? internal.getRoute().weightLinehaul : internal.getRoute().weightBackhaul) + external.weight;
                    return (actualInTypeWeight <= internal.getRoute().MAX_WEIGHT && actualExTypeWeight <= internal.getRoute().MAX_WEIGHT);

                } else {

                    int actualWeight = (inType == Values.nodeType.LINEHAUL ? internal.getRoute().weightLinehaul : internal.getRoute().weightBackhaul) - internal.weight + external.weight;
                    return actualWeight <= internal.getRoute().MAX_WEIGHT;
                }


            } else
                return internal.getRoute() == external.getRoute() && inType == exType;

        }


    }

    public void swapNodes(Node first, Node second) throws SwapFailedException {
        // if called with nodes of the same route, call the appropriate function
        if (first.getRoute() == second.getRoute() && canSwap(first, second) && canSwap(second, first)) {
            first.getRoute().swap(first,second);
        } else {

            // check if swapping the nodes (second in place of the first and vice versa) would cause trouble
            if (canSwap(first, second) && canSwap(second, first)) {

                int firstPosition = first.getRoute().nodeList.indexOf(first);
                int secondPosition = second.getRoute().nodeList.indexOf(second);

                Route firstRoute = first.getRoute();
                Route secondRoute = second.getRoute();

                firstRoute.removeNode(first);
                secondRoute.removeNode(second);

                try {
                    firstRoute.addNode(firstPosition, second);
                    secondRoute.addNode(secondPosition, first);
                } catch (MaxWeightException e) {
                    throw new SwapFailedException("!!! Swap failed! !!!");
                }

            }
        }
    }

    private BigDecimal simulateExchange(Node first, Node second) {

        if (first.getRoute() == second.getRoute()) return simulateExchangeSiblings(first, second);

        DistanceMatrix distances = DistanceMatrix.getInstance();
        Route firstRoute = first.getRoute();

        int firstIndex = firstRoute.nodeList.indexOf(first);
        BigDecimal actualDistanceFirst = new BigDecimal(0);

        Node actual;
        Node next;

        for (int index = 0; index < firstRoute.nodeList.size() - 1; index++) {

            actual = firstRoute.nodeList.get(index);
            next = firstRoute.nodeList.get(index + 1);

            if (firstIndex == index) actual = second;

            if (firstIndex == index + 1) next = second;

            actualDistanceFirst = actualDistanceFirst.add(distances.getDistance(actual, next));

        }

        return actualDistanceFirst;

    }

    private BigDecimal simulateExchangeSiblings(Node first, Node second) {

        DistanceMatrix distances = DistanceMatrix.getInstance();
        Route route = first.getRoute();

        int firstIndex = route.nodeList.indexOf(first);
        int secondIndex = route.nodeList.indexOf(second);
        BigDecimal actualDistanceFirst = new BigDecimal(0);

        Node actual;
        Node next;



        for (int index = 0; index < route.nodeList.size() - 1; index++) {

            actual = route.nodeList.get(index);
            next = route.nodeList.get(index + 1);

            if (index == firstIndex) actual = route.nodeList.get(secondIndex);
            if (index == secondIndex) actual = route.nodeList.get(firstIndex);

            if (index + 1 == firstIndex) next = route.nodeList.get(secondIndex);
            if (index + 1 == secondIndex) next = route.nodeList.get(firstIndex);

            actualDistanceFirst = actualDistanceFirst.add(distances.getDistance(actual, next));

        }

        return actualDistanceFirst;


    }


}
