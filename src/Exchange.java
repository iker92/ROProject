import utils.MaxWeightException;
import utils.NodeNotFoundException;
import utils.SwapFailedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Created by andream16 on 25.04.17.
 */
public class Exchange {

    RouteList routes;
    Helper helper;

    public Exchange(RouteList routes, Helper helper){
        this.routes = routes;
        this.helper = helper;
    }

    public RouteList findBestExchange() throws MaxWeightException, NodeNotFoundException {

        boolean isDone = false;

        while (!isDone){

            isDone = true;
            //For each route
            for(int routeIndex = 0; routeIndex < routes.size(); routeIndex++){
                //Initializing map to contain the current best move to do.
                TreeMap<Double, Node> bestMove = new TreeMap(Collections.reverseOrder());
                double currentRouteWeight;

                Route route = routes.get(routeIndex);
                ArrayList<Node> currentNodes = route.nodeList;
                int currentRouteSize = currentNodes.size();
                double routeWeight = route.getActualDistance();

                boolean paperino = true;

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

                        if (route.canSwap(currentNode, currentInnerNode)) {
                            //Current Route Actual Distance
                            currentRouteWeight = helper.simulateExchange(currentNode, currentInnerNode);

                            //Check if it is possible to do such move and also if it is worth to do it
                            if (routeWeight > currentRouteWeight) {
                                //If so, put currentNode and Its bound value to bestMove map
                                bestMove.put(currentRouteWeight*2, currentInnerNode);
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
                        double currentRouteActualDistance = currentRoute.getActualDistance();

                        //Get Route's Actual Distance + Original Route's one
                        double currentActualDistance = currentRouteActualDistance + routeWeight;

                        //For each node inside currentRoute
                        for(int currentRouteNodeIndex = 1; currentRouteNodeIndex < currentRoute.nodeList.size()-2; currentRouteNodeIndex++){

                            //Get currentRoute node
                            Node currentRouteNode = currentRoute.nodeList.get(currentRouteNodeIndex);

                            //If it is possible to swap
                            if( route.canSwap(currentNode, currentRouteNode) && currentRoute.canSwap(currentRouteNode, currentNode) ){

                                //If it is worth indeed
                                if(currentActualDistance > helper.simulateExchange(currentNode, currentRouteNode) + helper.simulateExchange(currentRouteNode, currentNode)){
                                    //Then add current Node and its weight to the map
                                    bestMove.put(currentRouteActualDistance, currentRouteNode);
                                }

                            }
                        }

                    }

                    //If the map has at least one candidate
                    if(!bestMove.isEmpty()){
                        //Get the best node from it
                        Double bestNodeToSwap = bestMove.lastKey();
                        try {
                            //Swap
                            helper.swapNodes(currentNode, bestMove.get(bestNodeToSwap));
                            //Decrease in order to analyze the last exchanged node
                            //nodeIndex--;
                            bestMove.clear();
                            isDone = false;
                            //System.out.println("routeIndex: " + routeIndex + " nodeIndex: " + nodeIndex);
                            //helper.printRoutes(routes);
                        } catch (SwapFailedException e) {}
                    }

                }

            }
        }



        return routes;

    }

}
