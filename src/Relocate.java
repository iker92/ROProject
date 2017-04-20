import Utils.MaxWeightException;

import java.util.ArrayList;

/**
 * Created by pippo on 13/04/17.
 */
public class Relocate {

    DistanceMatrix distances;
    ArrayList<Route> routes;

    public Relocate(DistanceMatrix distances, ArrayList<Route> routes){
        this.distances = distances;
        this.routes    = routes;
    }

    public ArrayList<Route> relocate (Node node, Route route) throws MaxWeightException {
        BestRelocateResult best_move = findBestRelocate(node, route);
        int pos_1_node = 0, pos_1_route;
        for(Route route1:routes) {

            if (route1.nodeList.indexOf(node) != -1 && route != route1) {
                pos_1_node = route1.nodeList.indexOf(node);
                route1.removeNode(pos_1_node);
            }
        }
        pos_1_route = routes.indexOf(best_move.route);

        //route.addNode(pos_1,best_move.node_2);
        //best_move.route.removeNode(pos_2) ;
        //best_move.route.addNode(pos_2,node);
        ArrayList<Route> new_routes = routes;
        new_routes.remove(pos_1_route);
        //new_routes.add();
        new_routes.add(pos_1_route,best_move.route);
        new_routes.get(pos_1_route).addNode(pos_1_node,best_move.node);


        return new_routes;
    }

    public BestRelocateResult findBestRelocate(Node node, Route route){

        BestRelocateResult best_move = null;
        int current_node_position = route.nodeList.indexOf(node);
        int current_route_position = routes.indexOf(route);
        ArrayList<Node> node_list = route.nodeList;
        int nodeList_size = node_list.size();
        int routes_number = routes.size();
        double distance;
        double tmp_distance = 0.0;

        //Foreach node in current route
        for (int i = current_node_position+1; i< nodeList_size; i++){
            distance = distances.getDistance(node, node_list.get(i));
            if( i == current_node_position+1 || distance < tmp_distance){
                if(node.weight + route.weight <= route.MAX_WEIGHT){
                    if(node.nodeType == node_list.get(i).nodeType){
                        best_move = new BestRelocateResult(node, route);
                        tmp_distance = distance;
                    }
                }
            }
        }
        int current_weight=0;
        //Foreach route in routes
        for(int current_route = current_route_position+1; current_route<routes_number; current_route++ ){
            ArrayList<Node> current_nodeList = route.nodeList;
            current_weight=routes.get(current_route).weight;
            //For each node in it
            for(int j=0; j< current_nodeList.size(); j++){
                distance = distances.getDistance(node, current_nodeList.get(j));
                if( distance < tmp_distance){
                    if(node.weight + current_weight <= route.MAX_WEIGHT){
                        if(node.nodeType == current_nodeList.get(j).nodeType || route.nodeList.size() - 1 == current_node_position){
                            best_move = new BestRelocateResult(node, routes.get(current_route));
                            tmp_distance = distance;

                        }
                    }
                }
            }
        }

        return best_move;

    }

}

class BestRelocateResult {
    public Node node;
    public Route route;

    public BestRelocateResult(Node node, Route route){

        this.node = node;
        this.route = route;
    }
}
