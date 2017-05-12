import javafx.util.Pair;
import utils.*;
import java.lang.reflect.Array;
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
                TreeMap<BigDecimal, Pair<Route, Integer>> bestMove = new TreeMap(Collections.reverseOrder());

                Route route = routes.get(routeIndex);
                ArrayList<Node> currentNodes = route.nodeList;

                //For each node in current route except first and last (WAREHOUSE)
                for (int nodeIndex = 1; nodeIndex < currentNodes.size() - 1; nodeIndex++)
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

                        if (canRelocate(currentNode, currentInnerRoute, innerNodeIndex/*currentInnerRoute.nodeList.indexOf(currentInnerNode)*/)) {
                            //Current Route Actual Distance
                            BigDecimal oldObjFun = routes.getObjectiveFunction();

                            BigDecimal newObjFun = new BigDecimal(0);

                            for (Route inner : routes) {
                                if (inner != currentNode.getRoute() && inner != currentInnerRoute)
                                {
                                    newObjFun = newObjFun.add(inner.getActualDistance());
                                    continue;
                                }

                                if (inner == currentNode.getRoute() && inner == currentInnerRoute) {
                                    System.out.println("Simulate internal relocation of " + currentNode.index + " in position " + innerNodeIndex + " inside " + routes.indexOf(currentNode.getRoute()));
                                    newObjFun = newObjFun.add(simulateInternalRelocation(currentNode, innerNodeIndex));
                                } else if (inner == currentNode.getRoute()) {
                                    System.out.println("Simulate removal of " + currentNode.index + " from route " + routes.indexOf(currentNode.getRoute()));
                                    newObjFun = newObjFun.add(simulateRemovalOfNode(currentNode));
                                } else {
                                    System.out.println("Simulate addition of " + currentNode.index + " in route " + routes.indexOf(currentInnerRoute)+ " with position " + innerNodeIndex/*currentInnerRoute.nodeList.indexOf(currentInnerNode)*/);

                                    newObjFun = newObjFun.add(simulateAdditionOfNode(currentNode, currentInnerRoute, innerNodeIndex/*currentInnerRoute.nodeList.indexOf(currentInnerNode)*/));
                                    // newObjFun = newObjFun.add(simulateAdditionOfNode(currentNode, inner, innerNodeIndex));
                                }
                            }

                            // ex1.compareTo(ex2)
                            // returns -1 if ex2 > ex1
                            // returns 1 if ex1 > ex2
                            // returns 0 if equal
                            if (oldObjFun.compareTo(newObjFun) == 1) {
                                //If so, put currentNode and Its bound value to bestMove map
                                bestMove.put(newObjFun, new Pair<>(currentInnerRoute, innerNodeIndex));

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
                        for(int currentRouteNodeIndex = 1; currentRouteNodeIndex < currentRoute.nodeList.size() - 1; currentRouteNodeIndex++){

                            //Get currentRoute node
                            Node currentRouteNode = currentRoute.nodeList.get(currentRouteNodeIndex);

                            Route currentInnerRoute;

                            if (currentRouteNode.getType() == Values.nodeType.WAREHOUSE) {
                                currentInnerRoute = currentRoute.nodeList.get(currentRouteNodeIndex - 1).getRoute();
                            } else {
                                currentInnerRoute =  currentRouteNode.getRoute();
                            }



                            //If it is possible to relocate
                            if(canRelocate(currentNode, currentInnerRoute, currentRouteNodeIndex /*currentInnerRoute.nodeList.indexOf(currentRouteNode)*/)){

                                BigDecimal oldObjFun = routes.getObjectiveFunction();

                                BigDecimal newObjFun = new BigDecimal(0);

                                for (Route inner : routes) {
                                    if (inner != currentNode.getRoute() && inner != currentInnerRoute)
                                    {
                                        newObjFun = newObjFun.add(inner.getActualDistance());
                                        continue;
                                    }

                                    if (inner == currentNode.getRoute() && inner == currentInnerRoute) {
                                        System.out.println("Simulate internal relocation of " + currentNode.index + " in position " + currentRouteNodeIndex + " inside " + routes.indexOf(currentNode.getRoute()));

                                        newObjFun = newObjFun.add(simulateInternalRelocation(currentNode, currentRouteNodeIndex));
                                    } else if (inner == currentNode.getRoute()) {
                                        System.out.println("Simulate removal of " + currentNode.index + " from route " + routes.indexOf(currentNode.getRoute()));
                                        newObjFun = newObjFun.add(simulateRemovalOfNode(currentNode));
                                    } else {
                                        System.out.println("Simulate addition of " + currentNode.index + " in route " + routes.indexOf(currentInnerRoute) + " with position " + currentRouteNodeIndex/*currentInnerRoute.nodeList.indexOf(currentRouteNode)*/);
                                        newObjFun = newObjFun.add(simulateAdditionOfNode(currentNode, currentInnerRoute, currentRouteNodeIndex/*currentInnerRoute.nodeList.indexOf(currentRouteNode)*/));
                                    }

                                }

                                //If it is worth indeed
                                if(oldObjFun.compareTo(newObjFun) == 1){
                                    //Then add current Node and its weight to the map
                                    System.out.println("Simulated situation reduces the objective function! Adding to the candidate map...\n");

                                    bestMove.put(newObjFun,  new Pair<>(currentInnerRoute, currentRouteNodeIndex));
                                } else {
                                    System.out.println("Simulated situation increases the objective function! Skip...\n");

                                }

                            }
                        }

                    }
                    if(!bestMove.isEmpty()){
                        //Get the best node from it
                        BigDecimal bestMapResult = bestMove.lastKey();
                        try {
                            relocateNode(currentNode, bestMove.get(bestMapResult).getKey(),bestMove.get(bestMapResult).getValue());
                            steps++;
                            System.out.println("Relocated node " + currentNode.index + " inside route " + routes.indexOf(bestMove.get(bestMapResult).getKey()) + " in position " + bestMove.get(bestMapResult).getValue());
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

        // if the nodes are on the same route, use the arraylist directly and force update the obj function
        if (node.getRoute() == route) {
            int oldIndex = route.nodeList.indexOf(node);


            route.nodeList.add(index, node);
            route.nodeList.remove(index < oldIndex ? oldIndex + 1 : oldIndex );

            route.forceUpdate();

        } else {

            //if the nodes are on different routes, just call addNodes
            try {
                route.addNode(index, node);
            } catch (MaxWeightException e) {
                System.err.println("!!! SOMETHING HAS GONE HORRIBLY WRONG !!!");
            }

        }

    }

    private BigDecimal simulateAdditionOfNode(Node node, Route route, int index) {

        ArrayList<Node> listOfNodes = new ArrayList<>(route.nodeList);

        listOfNodes.add(index, node);

        BigDecimal distance = new BigDecimal(0);
        for (int routeIndex = 0; routeIndex < listOfNodes.size()-1; routeIndex++) {
            distance = distance.add(distances.getDistance(listOfNodes.get(routeIndex), listOfNodes.get(routeIndex + 1)));
        }

        return distance;

    }

    private BigDecimal simulateRemovalOfNode(Node node) {
        DistanceMatrix distances = DistanceMatrix.getInstance();


        ArrayList<Integer> listOfNodes = new ArrayList<>();

        for (int nodeIndex = 0; nodeIndex <= node.getRoute().nodeList.size()-1; nodeIndex++) {

            if (node.getRoute().nodeList.get(nodeIndex).index == node.index) continue;

            listOfNodes.add(node.getRoute().nodeList.get(nodeIndex).index);
        }

        BigDecimal distance = new BigDecimal(0);
        for (int routeIndex = 0; routeIndex < listOfNodes.size()-1; routeIndex++) {
            distance = distance.add(distances.getDistance(listOfNodes.get(routeIndex), listOfNodes.get(routeIndex + 1)));
        }

        return distance;
    }

    private BigDecimal simulateInternalRelocation(Node node, int index) {


        Route routeNode = node.getRoute();

        ArrayList<Node> listOfNodes = new ArrayList<>();

        for (Node inNode : routeNode.nodeList) {
            if (inNode == node) continue;
            listOfNodes.add(inNode);
        }

        listOfNodes.add(index, node);


        BigDecimal distance = new BigDecimal(0);
        for (int routeIndex = 0; routeIndex < listOfNodes.size()-1; routeIndex++) {
            distance = distance.add(distances.getDistance(listOfNodes.get(routeIndex), listOfNodes.get(routeIndex + 1)));
        }

        return distance;

    }


/* commented for now, all is handled in simulaterelocate
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
*/

    public boolean canRelocate(Node node, Route route, int position) {

        System.out.println("\nTrying to relocate " + node.index + " from Route" + routes.indexOf(node.getRoute()) + " to Route " + routes.indexOf(route) + " in position " + position);

        //if trying to relocate a node with itself
        if (node.getRoute() == route &&  (route.nodeList.get(position).index == node.index || position == (route.nodeList.indexOf(node)+1))) {
            System.out.println("Relocate is impossible! Trying to relocate in the same position (position or position+1)!\n");
            return false;
        }


        if(node.getRoute() == route && node.getType() != route.nodeList.get(position).getType()){

            System.out.println("Relocate is impossible! Trying to relocate node of different types on same route!\n");
            return false;

        }

        //if trying to relocate in place of the first warehouse
        if (position == 0) {
            // System.out.println("Relocate is impossible! Trying to put something before first WAREHOUSE\n");
            return false;
        }

        if (node.getType() == Values.nodeType.WAREHOUSE) {
            //System.out.println("Relocate is impossible! Node to relocate is WAREHOUSE\n");
            return false;
        }

        Values.nodeType nodeType = node.getType();

        Values.nodeType previousTypeExtRoute = Values.nodeType.LINEHAUL;
        Values.nodeType nextTypeExtRoute = Values.nodeType.LINEHAUL;

        previousTypeExtRoute = node.getRoute().nodeList.get(node.getRoute().nodeList.indexOf(node) - 1).getType();
        nextTypeExtRoute = node.getRoute().nodeList.get(node.getRoute().nodeList.indexOf(node) + 1).getType();


        Values.nodeType previousType = route.nodeList.get(position - 1).getType();
        Values.nodeType nextType = route.nodeList.get(position).getType();

        //System.out.println("Node Type: " + nodeType.toString() + " | Previous Type: " + previousType.toString() + " | Next Type: " + nextType.toString());

        //if trying to relocate the only node in a route
        if (node.getRoute().nodeList.size() == 3 || (nodeType == Values.nodeType.LINEHAUL && nextTypeExtRoute != Values.nodeType.LINEHAUL && previousTypeExtRoute == Values.nodeType.WAREHOUSE)) {
            System.out.println("Relocate is impossible! Trying to relocate the only node in the route (or the only LINEHAUL)\n");
            return false;
        }

        //if nodes have different types
        if (nodeType != route.nodeList.get(position).getType()) {
            if (nodeType == Values.nodeType.LINEHAUL && previousType == Values.nodeType.BACKHAUL /*&& nextType != Values.nodeType.LINEHAUL*/) {
                System.out.println("Relocate is impossible! Trying to put a LINEHAUL in an invalid position\n");
                return false;
            }
            if (nodeType == Values.nodeType.BACKHAUL && /*previousType != Values.nodeType.BACKHAUL &&*/ nextType == Values.nodeType.LINEHAUL) {
                System.out.println("Relocate is impossible! Trying to put a BACKHAUL in an invalid position\n");
                return false;
            }
        }



        int actualTypeWeight = (nodeType == Values.nodeType.LINEHAUL ? route.weightLinehaul : route.weightBackhaul) + node.weight;

        if (actualTypeWeight <= route.MAX_WEIGHT) {
            System.out.println("Relocate is possible! Checking if it's worth...\n");
        } else {
            System.out.println("Relocate is impossible!\n");
        }

        return actualTypeWeight <= route.MAX_WEIGHT;

    }


}
