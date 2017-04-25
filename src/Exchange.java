import utils.MaxWeightException;
import utils.NodeNotFoundException;
import utils.SwapFailedException;

import java.util.ArrayList;

/**
 * Created by andream16 on 25.04.17.
 */
public class Exchange {

    ArrayList<Route> routes;
    Helper helper;

    public Exchange(ArrayList<Route> routes, Helper helper){
        this.routes = routes;
        this.helper = helper;
    }

    public ArrayList<Route> findBestExchange() throws MaxWeightException, NodeNotFoundException, SwapFailedException {

        //Routes clone
        ArrayList<Route> routesClone = new ArrayList<>(routes);

        //For each route
        for(Route route : routesClone){

            Route routeClone = route.getCopyOfRoute();
            ArrayList<Node> currentNodes = routeClone.nodeList;
            int currentRouteSize = currentNodes.size();
            double routeActualWeight = routeClone.getActualDistance();

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

                    //If it is possible to swap them
                    if(routeClone.canSwap(node, currentNode)){
                        //Swap them indeed
                        routeClone.swap(node, currentNode);
                        //Let's check if the exchange is worth
                        double newActualWeight = routeClone.getActualDistance();
                        //If it's worth indeed
                        if(newActualWeight < routeActualWeight){
                           //Let's apply the changes into the real route
                           route.swap(node, currentNode);
                        }
                    }
                }

                /** Swaps on Other Routes **/
                for(Route otherRoute : routes){

                    //If we get the same route skip
                    if(otherRoute.equals(route)) continue;
                    //Else let's get a clone of otherRoute
                    Route otherRouteClone = otherRoute.getCopyOfRoute();
                    ArrayList<Node> otherRouteNodes = otherRouteClone.nodeList;

                    //For each node in otherRouteClone
                    for(Node otherRouteNode : otherRouteNodes){
                        //If we can swap
                        if(routeClone.canSwap(node, otherRouteNode)){
                            //Then lets do the swap between the clones
                            helper.swapNodes(node, otherRouteNode);
                            //Let's check if it's worth
                            double new_obj_fun_value = helper.calculateObjectiveFunction(routesClone);
                            //If it minimizes the objective function
                            if( helper.isItMinimized(new_obj_fun_value) ){
                               //Let's swap also the real one
                               helper.swapNodes(node, otherRouteNode);
                               //Let's overwrite the objective function value
                               helper.setObjectiveFunction(new_obj_fun_value);
                            } else {
                                //If it is not worth, let's swap back
                                helper.swapNodes(otherRouteNode, node);
                            }
                        }
                    }

                }

            }

        }

        return routes;

    }

}
