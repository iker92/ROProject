package functions;

import com.sun.istack.internal.Nullable;
import core.*;
import exceptions.MovementFailedException;
import utils.DistanceMatrix;
import utils.Helper;
import javafx.util.Pair;
import exceptions.MaxWeightException;
import exceptions.NodeNotFoundException;
import exceptions.RouteSizeException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Class Relocate implements relocate algorithm. For each route, for each node in current route, we check first
 * if is possibile to perform the move respecting constraints, than we simulate the move checking current objective function and the simulated one
 * with the virtual movement. If it is worth, the relocate move is made and the objective function is updated
 */

public class Relocate {

    private static Boolean isDebug = Values.isDebug();
    private static Boolean printRoutesInDebug = Values.printRoutesInDebug();


    DistanceMatrix distances;
    RouteList routes;

    Helper helper;
    ArrayList<Node> tsp;

    public Relocate(DistanceMatrix distances, RouteList routes, Helper helper, ArrayList<Node> tsp) {
        this.distances = distances;
        this.routes = routes;
        this.helper = helper;
        this.tsp = tsp;
    }

    /**
     *
     * @return RouteList final list of routes after the best Relocate move
     * @throws MaxWeightException
     * @throws NodeNotFoundException
     * @throws RouteSizeException
     */
    public RouteList findBestRelocate() throws MaxWeightException, NodeNotFoundException, RouteSizeException {
        boolean isOptimized = false;
        int steps = 0;

        System.out.println("//////////////////////////////////////////////////////////////////\nStarting Relocate...\n");

        //ALWAYS take the warehouse
        tsp.get(0).take();

        while (isOptimized == false)
        {
            isOptimized = true;

            //For each route
            for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
                //Initializing map to contain the current best move to do.
                TreeMap<BigDecimal, Pair<Route, Integer>> bestMove = new TreeMap(Collections.reverseOrder());

                Route route = routes.get(routeIndex);
                ArrayList<Node> currentNodes = route.getNodeList();

                //For each node in current route except first and last (WAREHOUSE)
                for (int nodeIndex = 1; nodeIndex < currentNodes.size() - 1; nodeIndex++)
                {
                    //Get Current Node
                    Node currentNode = currentNodes.get(nodeIndex);

                    for(int currentRouteIndex = 0; currentRouteIndex < routes.size(); currentRouteIndex++){

                        //Get Current Route
                        Route currentRoute = routes.get(currentRouteIndex);

                        //For each node inside currentRoute
                        for(int currentRouteNodeIndex = 1; currentRouteNodeIndex < currentRoute.getNodeList().size() - 1; currentRouteNodeIndex++){

                            //Get currentRoute node
                            Node currentRouteNode = currentRoute.getNodeList().get(currentRouteNodeIndex);

                            Route currentInnerRoute;

                            if (currentRouteNode.getType() == Values.nodeType.WAREHOUSE) {
                                currentInnerRoute = currentRoute.getNodeList().get(currentRouteNodeIndex - 1).getRoute();
                            } else {
                                currentInnerRoute =  currentRouteNode.getRoute();
                            }


                            BigDecimal oldObjFun = routes.getObjectiveFunction();

                            // simulate relocate move
                            BigDecimal newObjFun = testRelocate(currentNode, currentInnerRoute, currentRouteNodeIndex);



                            // ex1.compareTo(ex2)
                            // returns -1 if ex2 > ex1
                            // returns 1 if ex1 > ex2
                            // returns 0 if equal
                            if (newObjFun != null && oldObjFun.compareTo(newObjFun) == 1) {
                                //Then add current Node and its weight to the map
                                if (isDebug) System.out.println("Simulated situation reduces the objective function! Adding to the candidate map...\n");

                                bestMove.put(newObjFun,  new Pair<>(currentInnerRoute, currentRouteNodeIndex));
                            } else {
                                if (isDebug) System.out.println("Simulated situation does not reduce the objective function! Skip...\n");
                            }
                        }

                    }

                    if(!bestMove.isEmpty()){
                        //Get the best node from it
                        BigDecimal bestMapResult = bestMove.lastKey();
                        try {
                            //do the move with the best node in the map, according to smallest objective functions based on simulation moves
                            relocateNode(currentNode, bestMove.get(bestMapResult).getKey(),bestMove.get(bestMapResult).getValue());
                            steps++;
                            System.out.println("Best move chosen! Relocated node " + currentNode.getIndex() + " inside route " + routes.indexOf(bestMove.get(bestMapResult).getKey()));
                            bestMove.clear();
                            isOptimized = false;
                            if (printRoutesInDebug) helper.printRoutes(routes);

                            //since we've made a move, we have to check again all other nodes, then, we release them
                            for (Node node : tsp) {
                                if (node.getType() == Values.nodeType.WAREHOUSE) continue;
                                node.release();
                            }

                        } catch (MovementFailedException e) { }
                    } else {
                        //the node is optimized, so we take him
                        tsp.get(tsp.indexOf(currentNode)).take();

                        //if at least one of the nodes of complete tsp haven't been taken, we haven't optimized yet. Else, we are done.
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

        System.out.println("\nRelocate successfully terminated!\nRelocate moves done: " + steps + "\nObjective Function: " + routes.getObjectiveFunction().toString() + "\n");
        //we release all the nodes for further use by Exchange algorithm
        for (Node node : tsp) {
            if (node.getType() == Values.nodeType.WAREHOUSE) continue;
            node.release();
        }

        return routes;
    }

    /**
     * TestRelocate check and simultae relocate move.
     * @param currentNode node we want to relocate
     * @param currentInnerRoute route we want to put currentNode
     * @param innerNodeIndex position where currentNode will be placed
     * @return
     */
    @Nullable
    private BigDecimal testRelocate(Node currentNode, Route currentInnerRoute, int innerNodeIndex) {

        BigDecimal newObjFun = null;

        //check if all the parameters satisfy constraints
        if (canRelocate(currentNode, currentInnerRoute, innerNodeIndex)) {

            newObjFun = new BigDecimal(0);
            //for all routes
            for (Route inner : routes) {
                //if the current route is not the node's route or the currentRoute we simply add to objective function the cost of the route
                if (inner != currentNode.getRoute() && inner != currentInnerRoute)
                {
                    newObjFun = newObjFun.add(inner.getActualDistance());
                    continue;
                }
                //depends on situation, we simulate the node move on its route, on different route
                //and the removal of the node from its route before the movement
                if (inner == currentNode.getRoute() && inner == currentInnerRoute) {
                    if (isDebug) System.out.println("Simulate internal relocation of " + currentNode.getIndex() + " in position " + innerNodeIndex + " inside " + routes.indexOf(currentNode.getRoute()));
                    newObjFun = newObjFun.add(simulateInternalRelocation(currentNode, innerNodeIndex));
                } else if (inner == currentNode.getRoute()) {
                    if (isDebug) if (isDebug) System.out.println("Simulate removal of " + currentNode.getIndex() + " from route " + routes.indexOf(currentNode.getRoute()));
                    newObjFun = newObjFun.add(simulateRemovalOfNode(currentNode));
                } else {
                    if (isDebug) System.out.println("Simulate addition of " + currentNode.getIndex() + " in route " + routes.indexOf(currentInnerRoute)+ " with position " + innerNodeIndex/*currentInnerRoute.getNodeList().indexOf(currentInnerNode)*/);

                    newObjFun = newObjFun.add(simulateAdditionOfNode(currentNode, currentInnerRoute, innerNodeIndex));

                }
            }
        }
        return newObjFun;

    }

    /**
     * relocateNode performs the relocate move.
     * @param node node we want to relocate
     * @param route route we want to put node
     * @param index position where node will be placed
     * @throws MovementFailedException
     */
    public void relocateNode(Node node, Route route, int index) throws MovementFailedException
    {

        // if the nodes are on the same route, use the arraylist directly and force update the obj function
        if (node.getRoute() == route) {
            int oldIndex = route.getNodeList().indexOf(node);
            route.getNodeList().add(index, node);
            route.getNodeList().remove(index < oldIndex ? oldIndex + 1 : oldIndex );

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

    /**
     * simulateAdditionOfNode simulate relocate move of the node in a route different from its own
     * @param node node we want to relocate
     * @param route route we want to put node
     * @param index position where node will be placed
     * @return cost of the route after the simulation
     */
    private BigDecimal simulateAdditionOfNode(Node node, Route route, int index) {

        //we take route's nodeList and we add the node to its
        ArrayList<Node> listOfNodes = new ArrayList<>(route.getNodeList());

        listOfNodes.add(index, node);

        BigDecimal distance = new BigDecimal(0);
        //update the cost of the route
        for (int routeIndex = 0; routeIndex < listOfNodes.size()-1; routeIndex++) {
            distance = distance.add(distances.getDistance(listOfNodes.get(routeIndex), listOfNodes.get(routeIndex + 1)));
        }

        return distance;

    }

    /**
     * simulateRemovalOfNode simulate the removal of the node from its route, before performing the movement
     * @param node node to remove
     * @return updated cost of the route
     */
    private BigDecimal simulateRemovalOfNode(Node node) {
        DistanceMatrix distances = DistanceMatrix.getInstance();


        ArrayList<Integer> listOfNodes = new ArrayList<>();

        //we put all the nodes in nodeList but node in the ArrayList
        for (int nodeIndex = 0; nodeIndex <= node.getRoute().getNodeList().size()-1; nodeIndex++) {

            if (node.getRoute().getNodeList().get(nodeIndex).getIndex() == node.getIndex()) continue;

            listOfNodes.add(node.getRoute().getNodeList().get(nodeIndex).getIndex());
        }
        // we calculate the cost of the ArrayList created above
        BigDecimal distance = new BigDecimal(0);
        for (int routeIndex = 0; routeIndex < listOfNodes.size()-1; routeIndex++) {
            distance = distance.add(distances.getDistance(listOfNodes.get(routeIndex), listOfNodes.get(routeIndex + 1)));
        }

        return distance;
    }

    /**
     * simulateInternalRelocation simulate the relocate move of the node on its route
     * @param node node we want to relocate
     * @param index position where node will be placed
     * @return updated cost of the route after the simulation
     */
    private BigDecimal simulateInternalRelocation(Node node, int index) {

        int oldIndex = node.getRoute().getNodeList().indexOf(node);

        Route routeNode = node.getRoute();

        ArrayList<Node> listOfNodes = new ArrayList<>();

        for (Node inNode : routeNode.getNodeList()) {
            if (inNode == node) continue;
            listOfNodes.add(inNode);
        }

        listOfNodes.add(oldIndex < index ? index - 1 : index, node);

        //when the internal move is made, we calculate the new cost of the route
        BigDecimal distance = new BigDecimal(0);
        for (int routeIndex = 0; routeIndex < listOfNodes.size()-1; routeIndex++) {
            distance = distance.add(distances.getDistance(listOfNodes.get(routeIndex), listOfNodes.get(routeIndex + 1)));
        }

        return distance;

    }

    /**
     * canRelocate check if the relocate move of the node to the position in ruote satisfies constraints of the route
     * @param node node we want to relocate
     * @param route route we want to put node
     * @param position position where node will be placed
     * @return true if move is possible, false otherwise
    */

    public boolean canRelocate(Node node, Route route, int position) {

        if (isDebug) System.out.println("\nTrying to relocate " + node.getIndex() + " from Route" + routes.indexOf(node.getRoute()) + " to Route " + routes.indexOf(route) + " in position " + position);

        //if trying to relocate a node with itself
        if (node.getRoute() == route &&  (route.getNodeList().get(position).getIndex() == node.getIndex() || position == (route.getNodeList().indexOf(node)+1))) {
            if (isDebug) System.out.println("Relocate is impossible! Trying to relocate in the same position (position or position+1)!\n");
            return false;
        }
        //if trying to relocate the node in the position and there relies a node with different type of the node to move, we can't do the movement
        if(node.getRoute() == route && node.getType() != route.getNodeList().get(position).getType()){

            if (isDebug) System.out.println("Relocate is impossible! Trying to relocate node of different types on same route!\n");
            return false;

        }

        //if trying to relocate in place of the first warehouse
        if (position == 0) {
            if (isDebug) System.out.println("Relocate is impossible! Trying to put something before first WAREHOUSE\n");
            return false;
        }

        if (node.getType() == Values.nodeType.WAREHOUSE) {
            if (isDebug) System.out.println("Relocate is impossible! Node to relocate is WAREHOUSE\n");
            return false;
        }

        Values.nodeType nodeType = node.getType();

        Values.nodeType previousTypeExtRoute = Values.nodeType.LINEHAUL;
        Values.nodeType nextTypeExtRoute = Values.nodeType.LINEHAUL;

        previousTypeExtRoute = node.getRoute().getNodeList().get(node.getRoute().getNodeList().indexOf(node) - 1).getType();
        nextTypeExtRoute = node.getRoute().getNodeList().get(node.getRoute().getNodeList().indexOf(node) + 1).getType();


        Values.nodeType previousType = route.getNodeList().get(position - 1).getType();
        Values.nodeType nextType = route.getNodeList().get(position).getType();

        //if trying to relocate the only node in a route
        if (node.getRoute().getNodeList().size() == 3 || (nodeType == Values.nodeType.LINEHAUL && nextTypeExtRoute != Values.nodeType.LINEHAUL && previousTypeExtRoute == Values.nodeType.WAREHOUSE)) {
            if (isDebug) System.out.println("Relocate is impossible! Trying to relocate the only node in the route (or the only LINEHAUL)\n");
            return false;
        }

        //if nodes have different types
        if (nodeType != route.getNodeList().get(position).getType()) {
            if (nodeType == Values.nodeType.LINEHAUL && previousType == Values.nodeType.BACKHAUL /*&& nextType != Values.nodeType.LINEHAUL*/) {
                if (isDebug) System.out.println("Relocate is impossible! Trying to put a LINEHAUL in an invalid position\n");
                return false;
            }
            if (nodeType == Values.nodeType.BACKHAUL && /*previousType != Values.nodeType.BACKHAUL &&*/ nextType == Values.nodeType.LINEHAUL) {
                if (isDebug) System.out.println("Relocate is impossible! Trying to put a BACKHAUL in an invalid position\n");
                return false;
            }
        }



        int actualTypeWeight = (nodeType == Values.nodeType.LINEHAUL ? route.weightLinehaul : route.weightBackhaul) + node.getWeight();

        //check if the weight after the movement is always less than MAXWEIGHT
        if (actualTypeWeight <= route.MAX_WEIGHT) {
            if (isDebug) System.out.println("Relocate is possible! Checking if it's worth...\n");
        } else {
            if (isDebug) System.out.println("Relocate is impossible!\n");
        }

        return actualTypeWeight <= route.MAX_WEIGHT;

    }


}
