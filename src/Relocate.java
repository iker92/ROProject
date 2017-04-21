import utils.MaxWeightException;
import utils.NodeNotFoundException;

import java.util.ArrayList;

/**
 * Created by pippo on 13/04/17.
 */
public class Relocate {

    DistanceMatrix distances;
    ArrayList<Route> routes;
    public double old_cost = 0;
    public double actual_cost = 0;
    Helper helper;

    public Relocate(DistanceMatrix distances, ArrayList<Route> routes, Helper helper){
        this.distances = distances;
        this.routes = routes;
        this.helper = helper;
    }

    public ArrayList<Route> findBestRelocate(ArrayList<Node> completeTSP) throws MaxWeightException, NodeNotFoundException {

        for (int i = 0; i < routes.size(); i++) {
            old_cost += routes.get(i).getActualDistance();
        }

        /** the external for is useful for analyze all nodes only one time per node ***/
        for (int i = 1; i < completeTSP.size(); i++) {

            Node currentNode = completeTSP.get(i);
            Route currentRoute = currentNode.getRoute().getCopyOfRoute(currentNode.getRoute());
            int routeIndex = helper.getRouteIndexByNode(routes, currentNode);

            //For every route
            for (int currentInternalRoute = 0; currentInternalRoute < routes.size(); currentInternalRoute++)
            {
                //For every node
                for (int currentInternalNode = 0; currentInternalNode < routes.get(currentInternalRoute).nodeList.size(); currentInternalNode++)
                {
                    //control node type
                    if(currentNode.nodeType == routes.get(currentInternalRoute).nodeList.get(currentInternalNode).nodeType)
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
                                    //remove examined node from its route
                                    currentNode.getRoute().removeNode(currentNode);
                                    //add the node in new route
                                    routes.get(currentInternalRoute).addNode(currentInternalNode, currentNode);

                                    //calculate new total cost
                                    actual_cost = 0;

                                    for (int j = 0; j < routes.size(); j++) {
                                        actual_cost += routes.get(j).getActualDistance();
                                    }

                                    //if the new total cost is greater than the old, undo the swap
                                    if (actual_cost >= old_cost)
                                    {
                                        routes.get(currentInternalRoute).removeNode(currentNode);
                                        if (index == currentRoute.nodeList.size())
                                        {
                                            routes.get(routeIndex).addNode(currentNode);
                                        } else {
                                            routes.get(routeIndex).addNode(index, currentNode);
                                        }
                                    } else {
                                        old_cost = actual_cost;
                                    }
                                }
                            } else {
                                currentNode.getRoute().removeNode(currentNode);
                                routes.get(currentInternalRoute).addNode(currentInternalNode, currentNode);

                                actual_cost = 0;
                                for (int j = 0; j < routes.size(); j++) {
                                    actual_cost += routes.get(j).getActualDistance();
                                }

                                //if the total cost is greater than the old, undo the swap
                                if (actual_cost >= old_cost)
                                {
                                    routes.get(currentInternalRoute).removeNode(currentNode);
                                    if (index == currentRoute.nodeList.size()) {
                                        routes.get(routeIndex).addNode(currentNode);
                                    } else {
                                        routes.get(routeIndex).addNode(index, currentNode);
                                    }
                                } else {
                                    old_cost = actual_cost;
                                }
                            }
                        }
                    }
                }
            }
        }

        return routes;
    }
}

