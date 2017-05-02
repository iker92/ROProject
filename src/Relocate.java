import utils.MaxWeightException;
import utils.NodeNotFoundException;
import utils.RouteSizeException;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by pippo on 13/04/17.
 */
public class Relocate {

    DistanceMatrix distances;
    RouteList routes;

    Helper helper;

    public Relocate(DistanceMatrix distances, RouteList routes, Helper helper){
        this.distances = distances;
        this.routes = routes;
        this.helper = helper;
    }

    public RouteList findBestRelocate(ArrayList<Node> completeTSP) throws MaxWeightException, NodeNotFoundException, RouteSizeException {


        /** the external for is useful for analyze all nodes only one time per node ***/
        for (int i = 1; i < completeTSP.size(); i++) {

            Node currentNode = completeTSP.get(i);
            Route currentRoute = currentNode.getRoute().getCopyOfRoute();
            int routeIndex = helper.getRouteIndexByNode(routes, currentNode);

            //For every route
            for (int currentInternalRoute = 0; currentInternalRoute < routes.size(); currentInternalRoute++)
            {
                //For every node
                for (int currentInternalNode = 1; currentInternalNode < routes.get(currentInternalRoute).nodeList.size() -1; currentInternalNode++)
                {
                    //control node type
                    if(currentNode.getType().equals(Values.nodeType.LINEHAUL))
                    {
                        //control if current_node is different from the passed node
                        if (currentNode != routes.get(currentInternalRoute).getNode(currentInternalNode))
                        {
                            //index of the current node in original position
                            int index = currentNode.getRoute().nodeList.indexOf(currentNode);

                            //if the route of examined node is different from the actual route
                            if (currentNode.getRoute() != routes.get(currentInternalRoute))
                            {
                                //if don't exceed maximum weight
                                if (routes.get(currentInternalRoute).canAdd(currentNode))
                                {
                                    if(routes.get(currentInternalRoute).nodeList.get(currentInternalNode).getType().equals(Values.nodeType.LINEHAUL))
                                    {
                                        //remove examined node from its route
                                        currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                        routeIndex=helper.getRouteIndexByNode(routes, currentNode);
                                    } else{
                                        currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                        routeIndex=helper.getRouteIndexByNode(routes, currentNode);
                                        currentInternalNode++;
                                    }
                                }
                            } else {
                                if(routes.get(currentInternalRoute).nodeList.get(currentInternalNode).getType().equals(Values.nodeType.LINEHAUL)){
                                    //remove examined node from its route
                                    currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                    routeIndex=helper.getRouteIndexByNode(routes, currentNode);
                                } else{
                                    currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                    routeIndex=helper.getRouteIndexByNode(routes, currentNode);
                                    //currentInternalRoute++;
                                }

                            }
                        }
                    } else if(currentNode.getType().equals(Values.nodeType.BACKHAUL)) {
                        //control if current_node is different from the passed node
                        if (currentNode != routes.get(currentInternalRoute).getNode(currentInternalNode))
                        {

                            //index of the current node in original position
                            int index = currentNode.getRoute().nodeList.indexOf(currentNode);

                            //if the route of examined node is different from the actual route
                            if (currentNode.getRoute() != routes.get(currentInternalRoute)) {
                                //if don't exceed maximum weight
                                if (routes.get(currentInternalRoute).canAdd(currentNode)) {
                                    if (routes.get(currentInternalRoute).nodeList.get(currentInternalNode).getType().equals(Values.nodeType.LINEHAUL)) {
                                        if (currentInternalNode == routes.get(currentInternalRoute).nodeList.size() - 2) { //handling warehouses

                                            currentInternalNode = routes.get(currentInternalRoute).nodeList.size() -1; //handling warehouses
                                            currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                            routeIndex = helper.getRouteIndexByNode(routes, currentNode);
                                        }
                                    } else {
                                        if (currentInternalNode == routes.get(currentInternalRoute).nodeList.size() - 2) { //handling warehouses

                                            currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                            routeIndex = helper.getRouteIndexByNode(routes, currentNode);
                                            currentInternalNode = routes.get(currentInternalRoute).nodeList.size() -1; //handling warehouses
                                            currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                            routeIndex = helper.getRouteIndexByNode(routes, currentNode);
                                        } else {
                                            currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                            routeIndex = helper.getRouteIndexByNode(routes, currentNode);
                                        }
                                    }
                                }
                            } else {
                                if (routes.get(currentInternalRoute).nodeList.get(currentInternalNode).getType().equals(Values.nodeType.LINEHAUL)) {
                                    if (currentInternalNode >= routes.get(currentInternalRoute).nodeList.size() - 2) { //handling warehouses
                                        currentInternalNode = routes.get(currentInternalRoute).nodeList.size() -1; //handling warehouses
                                        currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                        routeIndex = helper.getRouteIndexByNode(routes, currentNode);
                                    }

                                } else {
                                    if (currentInternalNode == routes.get(currentInternalRoute).nodeList.size() - 2) { //handling warehouses
                                        currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                        routeIndex = helper.getRouteIndexByNode(routes, currentNode);
                                        currentInternalNode = routes.get(currentInternalRoute).nodeList.size() -1; //handling warehouses
                                        currentRoute = moveNodeandCheck(currentNode, currentRoute, currentInternalNode, currentInternalRoute, index, routeIndex);
                                        routeIndex = helper.getRouteIndexByNode(routes, currentNode);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return routes;
    }

    //TODO: due to heavy changes in some core functionalities, this method was commented. isItMinimized must be correctly reimplemented and canSwap must be replaced with canRelocate (to be implemented)

    private Route moveNodeandCheck(Node currentNode, Route currentRoute, int currentInternalNode, int currentInternalRoute,int index,int routeIndex) throws MaxWeightException, RouteSizeException {

     /*
     BigDecimal obj=routes.getObjectiveFunction();
       Node tempNode=routes.get(currentInternalRoute).getNode(currentInternalNode);

       if(routes.get(currentInternalRoute).canSwap(tempNode,currentNode)) {
           routes.get(currentInternalRoute).addNode(currentInternalNode, currentNode);

           if (routes.isItMinimized(obj)) {
               currentRoute = currentNode.getRoute().getCopyOfRoute();
           } else {
               routes.get(currentInternalRoute).removeNode(currentNode);
               if (index >= currentRoute.nodeList.size()) {
                   routes.get(routeIndex).addNode(currentNode);
               } else {
                   routes.get(routeIndex).addNode(index, currentNode);
               }
           }


       }
       */

     //TODO: from here to the bottom was already commented


        //calculate new total cost


       /* for (int j = 0; j < routes.size(); j++) {
            actual_cost += routes.get(j).getActualDistance();
        }

        //if the new total cost is greater than the old, undo the swap
        if (actual_cost >= old_cost)
        {
            routes.get(currentInternalRoute).removeNode(currentNode);
            if (index >= currentRoute.nodeList.size())
            {
                routes.get(routeIndex).addNode(currentNode);
            } else {
                routes.get(routeIndex).addNode(index, currentNode);
            }
        } //otherwise old cost became actual cost (in order to store best result) and update the current node route
        else {
            old_cost = actual_cost;
            currentRoute = currentNode.getRoute().getCopyOfRoute();
        }

        return currentRoute;
    }*/
        return currentRoute;
    }
}

