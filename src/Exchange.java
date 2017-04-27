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

    public RouteList findBestExchange() throws MaxWeightException, NodeNotFoundException, SwapFailedException {

        //Routes clone
        RouteList fakeRoutes = new RouteList(routes);

        //For each route
        for(Route route : fakeRoutes){

            Route fakeRoute = route.getCopyOfRoute();
            ArrayList<Node> currentNodes = fakeRoute.nodeList;
            int currentRouteSize = currentNodes.size();
            double routeActualWeight = fakeRoute.getActualDistance();
            int currentRouteIndex = fakeRoutes.indexOf(route);

            //For each node in current route
            for(Node node : currentNodes){

                Node fakeNode = new Node(node);

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

                    //If it is possible to swap them
                    if(fakeRoute.canSwap(node, currentNode)){
                        //Swap them indeed
                        fakeRoute.swap(node, currentNode);
                        //Let's check if the exchange is worth
                        double newActualWeight = fakeRoute.getActualDistance();
                        //If it's worth indeed
                        if(newActualWeight < routeActualWeight){
                           //Let's apply the changes into the real route
                           route.swap(node, currentNode);
                        }
                    }
                }

                /** Swaps on Other Routes **/
                for(int j=currentRouteIndex+1; j<fakeRoutes.size(); j++){

                    Route otherRoute = fakeRoutes.get(j);

                    //Else let's get a clone of otherRoute
                    Route otherFakeRoute = otherRoute.getCopyOfRoute();
                    ArrayList<Node> otherRouteNodes = otherFakeRoute.nodeList;

                    //For each node in otherFakeRoute
                    for(Node otherRouteNode : otherRouteNodes){
                        //If we can swap
                        if(fakeRoute.canSwap(node, otherRouteNode)){
                            
                            double oldObjectiveFunction = fakeRoutes.getObjectiveFunction();
                            
                            //Then lets do the swap between the clones
                            helper.swapNodes(node, otherRouteNode);

                            //If it minimizes the objective function
                            if( fakeRoutes.isItMinimized(oldObjectiveFunction) ){
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
