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
    Node temp;
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


        /***
         * external counters are useful for remove pointed node
         *    internal counters are useful for insert external node in internal route after internal node position
         */

        for (int i = 1; i < completeTSP.size(); i++) {

            Node currentNode = completeTSP.get(i);
            Route currentRoute = new Route(currentNode.getRoute());
            int routeIndex = helper.getRouteIndexByNode(routes, currentNode);

            //Control every route
            for (int currentInternalRoute = 0; currentInternalRoute < routes.size(); currentInternalRoute++)
            {
                //Control every node
                for (int currentInternalNode = 0; currentInternalNode < routes.get(currentInternalRoute).nodeList.size(); currentInternalNode++)
                {
                    //control node type
                    if(currentNode.nodeType == routes.get(currentInternalRoute).nodeList.get(currentInternalNode).nodeType)
                    {
                        //control if current_node is different from the passed node
                        if (currentNode != routes.get(currentInternalRoute).getNode(currentInternalNode))
                        {
                            int index = currentNode.getRoute().nodeList.indexOf(currentNode);

                            //if don't exceed maximum weight
                            if (currentNode.getRoute() != routes.get(currentInternalRoute))
                            {
                                if (routes.get(currentInternalRoute).canAdd(currentNode))
                                {
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
                                        if (index == currentRoute.nodeList.size())
                                        {
                                            routes.get(routeIndex).addNode(currentNode);
                                        } else {
                                            routes.get(routeIndex).addNode(index, currentNode);
                                        }
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

