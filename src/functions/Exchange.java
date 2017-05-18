package functions;

import com.sun.istack.internal.Nullable;
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
    private static Boolean printRoutesInDebug = Values.printRoutesInDebug();

    RouteList routes;
    Helper helper;

    public Exchange(RouteList routes, Helper helper, ArrayList<Node> completeTSP){
        this.routes = routes;
        this.helper = helper;
        this.tsp = completeTSP;
    }

    public RouteList findBestExchange() throws MaxWeightException, NodeNotFoundException {

        System.out.println("//////////////////////////////////////////////////////////////////\nStarting Exchange...\n");

        // /ALWAYS take the warehouse
        tsp.get(0).take();

        boolean isOptimized = false;
        int steps = 0;

        while (!isOptimized){

            isOptimized = true;
            //For each route
            for(int routeIndex = 0; routeIndex < routes.size(); routeIndex++){
                //Initializing map to contain the current best move to do.
                TreeMap<BigDecimal, Node> bestMove = new TreeMap(Collections.reverseOrder());

                Route route = routes.get(routeIndex);
                ArrayList<Node> currentNodes = route.getNodeList();
                int currentRouteSize = currentNodes.size();
                BigDecimal routeWeight = route.getActualDistance();

                //For each node in current route except first and last (WAREHOUSE)
                for(int nodeIndex=1; nodeIndex <= currentRouteSize-2; nodeIndex++){

                    //Get Current Node
                    Node currentNode = currentNodes.get(nodeIndex);

                    for(int currentRouteIndex = 0; currentRouteIndex < routes.size(); currentRouteIndex++){

                        //Get Current Route
                        Route currentRoute = routes.get(currentRouteIndex);

                        //Get currentRoute actual distance
                        BigDecimal currentRouteActualDistance = currentRoute.getActualDistance();

                        //Get Route's Actual Distance + Original Route's one
                        BigDecimal currentActualDistance = currentRouteActualDistance.add(routeWeight);

                        //For each node inside currentRoute
                        for(int currentRouteNodeIndex = 1; currentRouteNodeIndex <= currentRoute.getNodeList().size()-2; currentRouteNodeIndex++){

                            //Get currentRoute node
                            Node currentRouteNode = currentRoute.getNodeList().get(currentRouteNodeIndex);


                            BigDecimal oldObjFun = routes.getObjectiveFunction();

                            if (isDebug) System.out.println("Simulating exchange of node " + currentNode.getIndex() + "(" +currentNode.getType() + ") from route " + routes.indexOf(currentNode.getRoute())
                                    + " with node" + currentRouteNode.getIndex() + "(" +currentRouteNode.getType() + ") from route " + routes.indexOf(currentRouteNode.getRoute()));
                            BigDecimal newObjFun = testSwap(currentNode, currentRouteNode);


                            // ex1.compareTo(ex2)
                            // returns -1 if ex2 > ex1
                            // returns 1 if ex1 > ex2
                            // returns 0 if equal
                            if(newObjFun != null && oldObjFun.compareTo(newObjFun) == 1){
                                //Then add current Node and its weight to the map
                                if (isDebug) System.out.println("Simulated situation reduces the objective function! Adding to the candidate map...\n");
                                bestMove.put(newObjFun, currentRouteNode);
                            } else {
                                if (isDebug) System.out.println("Simulated situation does not reduce the objective function! Skip...\n");
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
                            System.out.println("Best move chosen! Exchanged node " + currentNode.getIndex() + " with node  " + bestMove.get(bestNodeToSwap).getIndex());
                            bestMove.clear();
                            isOptimized = false;
                            if (printRoutesInDebug) helper.printRoutes(routes);

                            for (Node node : tsp) {
                                if (node.getType() == Values.nodeType.WAREHOUSE) continue;
                                node.release();
                            }


                        } catch (SwapFailedException e) {}
                    } else {

                        tsp.get(tsp.indexOf(currentNode)).take();

                        for (Node node : tsp) {
                            if (node.isTaken() == false) {
                                isOptimized = false;
                                break;
                            }
                        }

                    }

             
                }

            }
        }

        System.out.println("\nExchange successfully terminated!\nExchange moves done: " + steps + "\nObjective Function: " + routes.getObjectiveFunction().toString() + "\n");

        for (Node node : tsp) {
            if (node.getType() == Values.nodeType.WAREHOUSE) continue;
            node.release();
        }

        return routes;

    }

    @Nullable
    private BigDecimal testSwap(Node currentNode, Node currentRouteNode) {
        BigDecimal newObjFun = null;

        if( canSwap(currentNode, currentRouteNode) && canSwap(currentRouteNode, currentNode) ){

            newObjFun = new BigDecimal(0);

            for (Route inner : routes) {
                if (inner != currentNode.getRoute() && inner != currentRouteNode.getRoute()) {
                    newObjFun = newObjFun.add(inner.getActualDistance());

                } else if (inner == currentNode.getRoute()){

                    newObjFun = newObjFun.add(simulateExchange(currentNode, currentRouteNode));

                } else {

                    newObjFun = newObjFun.add(simulateExchange(currentRouteNode, currentNode));

                }
            }

        }
        return newObjFun;
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

                Node nextIntNode = internal.getRoute().getNodeList().get(internal.getRoute().getNodeList().indexOf(internal) + 1);
                Node prevIntNode = internal.getRoute().getNodeList().get(internal.getRoute().getNodeList().indexOf(internal) - 1);

                // if BACKHAUL -> LINEHAUL, the current internal prev cannot be BACKHAUL
                // if LINEHAUL -> BACKHAUL, the current internal next cannot be LINEHAUL
                if (inType != exType) {

                    if ((inType == Values.nodeType.BACKHAUL && prevIntNode.getType() == Values.nodeType.BACKHAUL || inType == Values.nodeType.LINEHAUL && (nextIntNode.getType() == Values.nodeType.LINEHAUL || prevIntNode.getType() != Values.nodeType.LINEHAUL))) {
                        return false;
                    }

                    int actualInTypeWeight = (inType == Values.nodeType.LINEHAUL ? internal.getRoute().weightLinehaul : internal.getRoute().weightBackhaul) - internal.getWeight();
                    int actualExTypeWeight = (exType == Values.nodeType.LINEHAUL ? internal.getRoute().weightLinehaul : internal.getRoute().weightBackhaul) + external.getWeight();
                    return (actualInTypeWeight <= internal.getRoute().MAX_WEIGHT && actualExTypeWeight <= internal.getRoute().MAX_WEIGHT);

                } else {

                    int actualWeight = (inType == Values.nodeType.LINEHAUL ? internal.getRoute().weightLinehaul : internal.getRoute().weightBackhaul) - internal.getWeight() + external.getWeight();
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

                int firstPosition = first.getRoute().getNodeList().indexOf(first);
                int secondPosition = second.getRoute().getNodeList().indexOf(second);

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

        int firstIndex = firstRoute.getNodeList().indexOf(first);
        BigDecimal actualDistanceFirst = new BigDecimal(0);

        Node actual;
        Node next;

        for (int index = 0; index < firstRoute.getNodeList().size() - 1; index++) {

            actual = firstRoute.getNodeList().get(index);
            next = firstRoute.getNodeList().get(index + 1);

            if (firstIndex == index) actual = second;

            if (firstIndex == index + 1) next = second;

            actualDistanceFirst = actualDistanceFirst.add(distances.getDistance(actual, next));

        }

        return actualDistanceFirst;

    }

    private BigDecimal simulateExchangeSiblings(Node first, Node second) {

        DistanceMatrix distances = DistanceMatrix.getInstance();
        Route route = first.getRoute();

        int firstIndex = route.getNodeList().indexOf(first);
        int secondIndex = route.getNodeList().indexOf(second);
        BigDecimal actualDistanceFirst = new BigDecimal(0);

        Node actual;
        Node next;



        for (int index = 0; index < route.getNodeList().size() - 1; index++) {

            actual = route.getNodeList().get(index);
            next = route.getNodeList().get(index + 1);

            if (index == firstIndex) actual = route.getNodeList().get(secondIndex);
            if (index == secondIndex) actual = route.getNodeList().get(firstIndex);

            if (index + 1 == firstIndex) next = route.getNodeList().get(secondIndex);
            if (index + 1 == secondIndex) next = route.getNodeList().get(firstIndex);

            actualDistanceFirst = actualDistanceFirst.add(distances.getDistance(actual, next));

        }

        return actualDistanceFirst;


    }


}
