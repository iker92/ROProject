import utils.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Created by pippo on 13/04/17.
 */
public class Relocate {

    DistanceMatrix distances;
    RouteList routes;

    Helper helper;

    public Relocate(DistanceMatrix distances, RouteList routes, Helper helper) {
        this.distances = distances;
        this.routes = routes;
        this.helper = helper;
    }

    public RouteList findBestRelocate() throws MaxWeightException, NodeNotFoundException, RouteSizeException {
        boolean isOptimized = false;
        int steps = 0;

        while (isOptimized == false)
        {
            isOptimized = true;
            //For each route
            for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
                //Initializing map to contain the current best move to do.
                TreeMap<BigDecimal, Node> bestMove = new TreeMap(Collections.reverseOrder());

                Route route = routes.get(routeIndex);
                ArrayList<Node> currentNodes = route.nodeList;

                //For each node in current route except first and last (WAREHOUSE)
                for (int nodeIndex = 1; nodeIndex <= currentNodes.size() - 1; nodeIndex++)
                {
                    //Get Current Node
                    Node currentNode = currentNodes.get(nodeIndex);

                    /** Relocate on the same Route **/
                    for (int innerNodeIndex = 1; innerNodeIndex <= currentNodes.size() - 1; innerNodeIndex++)
                    {
                        //Get Current Inner Node
                        Node currentInnerNode = currentNodes.get(innerNodeIndex);

                        //If we are analyzing the same node, or one or both of them are WAREHOUSE nodes, skip
                        if (currentNode.equals(currentInnerNode) || currentNode.getType().equals(Values.nodeType.WAREHOUSE))
                            continue;

                        Route currentInnerRoute;

                        if (currentInnerNode.getType() == Values.nodeType.WAREHOUSE) {
                            currentInnerRoute = currentNodes.get(innerNodeIndex - 1).getRoute();
                        } else {
                            currentInnerRoute =  currentInnerNode.getRoute();
                        }

                        if (canRelocate(currentNode, currentInnerRoute, currentInnerRoute.nodeList.indexOf(currentInnerNode))) {
                            //Current Route Actual Distance
                            BigDecimal oldObjFun = routes.getObjectiveFunction();

                            BigDecimal newObjFun = new BigDecimal(0);

                            for (Route inner : routes) {
                                if (inner != currentNode.getRoute() && inner != currentInnerRoute)
                                {
                                    newObjFun = newObjFun.add(inner.getActualDistance());
                                }
                                if (inner == currentNode.getRoute())
                                {
                                    if(currentNode.getRoute() == currentInnerRoute){
                                        newObjFun = newObjFun.add(simulateRelocateSiblings(currentNode, currentInnerRoute, currentInnerRoute.nodeList.indexOf(currentInnerNode)));
                                    }
                                    else{
                                        newObjFun = newObjFun.add(simulateRelocate(currentNode, currentInnerRoute, currentInnerRoute.nodeList.indexOf(currentInnerNode)));
                                    }
                                }
                                else
                                {
                                    newObjFun = newObjFun.add(simulateRelocate(currentNode, currentInnerRoute, currentInnerRoute.nodeList.indexOf(currentInnerNode)));
                                }
                            }

                            // ex1.compareTo(ex2)
                            // returns -1 if ex2 > ex1
                            // returns 1 if ex1 > ex2
                            // returns 0 if equal
                            if (oldObjFun.compareTo(newObjFun) == 1) {
                                //If so, put currentNode and Its bound value to bestMove map
                                bestMove.put(newObjFun, currentInnerNode);

                            }
                        }
                    }

                    /** Relocate on Other Routes **/
                    for(int currentRouteIndex = 0; currentRouteIndex < routes.size(); currentRouteIndex++){

                        //Get Current Route
                        Route currentRoute = routes.get(currentRouteIndex);

                        //If we are analyzing same routes, skip
                        if(currentRoute.equals(route)) continue;

                        //For each node inside currentRoute
                        for(int currentRouteNodeIndex = 1; currentRouteNodeIndex <= currentRoute.nodeList.size() - 1; currentRouteNodeIndex++){

                            //Get currentRoute node
                            Node currentRouteNode = currentRoute.nodeList.get(currentRouteNodeIndex);

                            Route currentInnerRoute;

                            if (currentRouteNode.getType() == Values.nodeType.WAREHOUSE) {
                                currentInnerRoute = currentRoute.nodeList.get(currentRouteNodeIndex - 1).getRoute();
                            } else {
                                currentInnerRoute =  currentRouteNode.getRoute();
                            }

                            //If it is possible to relocate
                            if(canRelocate(currentNode, currentInnerRoute, currentInnerRoute.nodeList.indexOf(currentRouteNode))){

                                BigDecimal oldObjFun = routes.getObjectiveFunction();

                                BigDecimal newObjFun = new BigDecimal(0);

                                for (Route inner : routes) {
                                    if (inner != currentNode.getRoute() && inner != currentInnerRoute)
                                    {
                                        newObjFun = newObjFun.add(inner.getActualDistance());
                                    }
                                    if (inner == currentNode.getRoute())
                                    {
                                        if(currentNode.getRoute() == currentInnerRoute){
                                            newObjFun = newObjFun.add(simulateRelocateSiblings(currentNode, currentInnerRoute, currentRouteNodeIndex));
                                        }
                                        else{
                                            newObjFun = newObjFun.add(simulateRelocate(currentNode, currentInnerRoute, currentRouteNodeIndex));
                                        }
                                    }

                                    if (inner == currentInnerRoute)
                                    {
                                        newObjFun = newObjFun.add(simulateRelocate(currentNode, currentInnerRoute, currentRouteNodeIndex));
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
                    if(!bestMove.isEmpty()){
                        //Get the best node from it
                        BigDecimal bestMapResult = bestMove.lastKey();
                        try {
                            relocateNode(currentNode, bestMove.get(bestMapResult).getRoute(),bestMove.get(bestMapResult).getRoute().getIndexByNode(bestMove.get(bestMapResult)));
                            steps++;
                            System.out.println("Relocated node " + currentNode.index + " inside route of node  " + bestMove.get(bestMapResult).index);
                            bestMove.clear();
                            isOptimized = false;
                            helper.printRoutes(routes);
                        } catch (SwapFailedException e) { }
                    }
                }
            }
        }

        System.out.println("\nRelocate successfully terminated!\nRelocate moves done: " + steps + "\nObjective Function: " + routes.getObjectiveFunction().toString());

        return routes;
    }


    public void relocateNode(Node node, Route route, int index) throws SwapFailedException
    {

        try {
            node.getRoute().removeNode(node);
            route.addNode(index, node);
        } catch (MaxWeightException e) {

            System.err.println("!!! SOMETHING HAS GONE HORRIBLY WRONG !!!");

        }

    }

    private BigDecimal simulateRelocate(Node node, Route route, int index) {


        DistanceMatrix distances = DistanceMatrix.getInstance();
        Route firstRoute = node.getRoute();

        int nodeIndex = firstRoute.nodeList.indexOf(node);
        BigDecimal actualDistanceFirst = new BigDecimal(0);

        Node actual;
        Node next;
        int offset = 0;
        int nodeListSize = route.nodeList.size();

        for (int innerIndex = 0; innerIndex < nodeListSize - 1 ; innerIndex++) {

            actual = route.nodeList.get(innerIndex + offset);
            next = route.nodeList.get(innerIndex + 1 + offset);

            if (node.getRoute() == route) {

                // simulate skip of node
                if (nodeIndex == innerIndex + 1) {
                    next = route.nodeList.get(innerIndex + 2);
                    nodeListSize--;
                    offset = 1;
                }
                //if (nodeIndex == innerIndex) actual = route.nodeList.get(innerIndex - 1);

            } else {

                // simulate insertion of node
                if (index == innerIndex + 1) {
                    next = node;
                    offset = -1;
                    nodeListSize++;
                }
                if (index == innerIndex) actual = node;

            }

            actualDistanceFirst = actualDistanceFirst.add(distances.getDistance(actual, next));

        }

        return actualDistanceFirst;

    }

    private BigDecimal simulateRelocateSiblings(Node node, Route route, int index) {

        DistanceMatrix distances = DistanceMatrix.getInstance();
        Route firstRoute = node.getRoute();

        int nodeIndex = firstRoute.nodeList.indexOf(node);
        BigDecimal actualDistanceFirst = new BigDecimal(0);

        Node actual;
        Node next;

        for (int innerIndex = 0; innerIndex < route.nodeList.size() - 1; innerIndex++) {

            actual = route.nodeList.get(innerIndex);
            next = route.nodeList.get(innerIndex + 1);

            //TODO probably all wrong, check!

            // simulate skip of node
            if (nodeIndex == innerIndex + 1) next = route.nodeList.get(innerIndex + 2);
            if (nodeIndex == innerIndex) actual = route.nodeList.get(innerIndex - 1);

            // simulate insertion of node
            if (index == innerIndex + 1) next = node;
            if (index == innerIndex) actual = node;

            actualDistanceFirst = actualDistanceFirst.add(distances.getDistance(actual, next));

        }
        return actualDistanceFirst;

    }


    public boolean canRelocate(Node node, Route route, int position) {

        //if trying to relocate a node with itself
        if (node.getRoute() == route && route.nodeList.indexOf(node) == position) {
            return false;
        }

        //if trying to relocate in place of the first warehouse
        if (position == 0 || node.getType() == Values.nodeType.WAREHOUSE) {
            return false;
        }


        Values.nodeType nodeType = node.getType();

        Values.nodeType previousTypeExtRoute = node.getRoute().nodeList.get(node.getRoute().nodeList.indexOf(node) - 1).getType();
        Values.nodeType nextTypeExtRoute = node.getRoute().nodeList.get(node.getRoute().nodeList.indexOf(node) + 1).getType();

        Values.nodeType previousType = route.nodeList.get(position - 1).getType();
        Values.nodeType nextType = route.nodeList.get(position).getType();

        //System.out.println("Node Type: " + nodeType.toString() + " | Previous Type: " + previousType.toString() + " | Next Type: " + nextType.toString());

        //if trying to relocate the only node in a route
        if (node.getRoute().nodeList.size() == 3 || (nodeType == Values.nodeType.LINEHAUL && nextTypeExtRoute != Values.nodeType.LINEHAUL && previousTypeExtRoute == Values.nodeType.WAREHOUSE)) {
            return false;
        }

        //if nodes have different types
        if (nodeType != route.nodeList.get(position).getType()) {
            if (nodeType == Values.nodeType.LINEHAUL && previousType == Values.nodeType.BACKHAUL && nextType != Values.nodeType.LINEHAUL) {
                return false;
            }
            if (nodeType == Values.nodeType.BACKHAUL && previousType != Values.nodeType.BACKHAUL && nextType == Values.nodeType.LINEHAUL) {
                return false;
            }
        }

        int actualTypeWeight = (nodeType == Values.nodeType.LINEHAUL ? route.weightLinehaul : route.weightBackhaul) + node.weight;

        return actualTypeWeight <= route.MAX_WEIGHT;

    }


}
