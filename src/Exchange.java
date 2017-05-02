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

        //For each route
        for(Route route : routes){

            //Initializing map to contain the current best move to do.
            TreeMap<Double, Node> bestMove = new TreeMap(Collections.reverseOrder());
            double currentRouteWeight;

            ArrayList<Node> currentNodes = route.nodeList;
            int currentRouteSize = currentNodes.size();
            double routeWeight = route.getActualDistance();

            //For each node in current route
            for(Node node : currentNodes){

                /** Swaps on the same Route **/
                //If currentNode is a Warehouse node indeed we skip it
                if(node.getType().equals(Values.nodeType.WAREHOUSE)) continue;

                //Get current node index
                int currNodeIndex = currentNodes.indexOf(node);

                //Starting from the next node
                for( int i=currNodeIndex+1; i<currentRouteSize; i++ ){
                    //Get next's node index
                    Node currentNode = currentNodes.get(i);

                    //If the next node is a warehouse skip
                    if(currentNode.getType().equals(Values.nodeType.WAREHOUSE)) continue;

                    if(route.canSwap(node, currentNode)){
                        //Current Route Actual Distance
                        currentRouteWeight = 2 * helper.simulateExchange(node, currentNode);

                        //Check if it is possible to do such move and also if it is worth to do it
                        if((2 * routeWeight) > currentRouteWeight){
                            //If so, put currentNode and Its bound value to bestMove map
                            bestMove.put(currentRouteWeight, currentNode);
                        }
                    }

                }

                /** Swaps on Other Routes **/
                for(Route currentRoute : routes){

                    //Skip if we are on the very same route
                    if(currentRoute.equals(route)) continue;

                    int routeIndex = routes.indexOf(currentRoute);
                    Route otherRoute = routes.get(routeIndex);

                    ArrayList<Node> otherRouteNodes = otherRoute.nodeList;

                    //For each node in otherRoute
                    for(Node otherRouteNode : otherRouteNodes){

                        if(otherRouteNode.getType().equals(Values.nodeType.WAREHOUSE)) continue;

                        if(route.canSwap(node, otherRouteNode) && otherRoute.canSwap(otherRouteNode, node)){
                            double otherRouteWeight = otherRoute.getActualDistance();
                            //Current Route Actual Distance
                            currentRouteWeight = helper.simulateExchange(node, otherRouteNode) + helper.swapAndCalculateDistance(otherRouteNode, node);

                            //Check if it is possible to do such move and also if it is worth to do it
                            if(routeWeight + otherRouteWeight > currentRouteWeight){
                                //If so, put currentNode and Its bound value to bestMove map
                                bestMove.put(currentRouteWeight, otherRouteNode);
                            }
                        }

                    }

                }

                //If the map has at least one candidate
                if(!bestMove.isEmpty()){
                    //Get the best node from it
                    Double bestNodeToSwap = bestMove.firstKey();
                    try {
                        //Swap
                        helper.swapNodes(node, bestMove.get(bestNodeToSwap));
                    } catch (SwapFailedException e) {}
                }

            }

        }

        return routes;

    }

}
