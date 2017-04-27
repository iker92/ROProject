//import java.util.ArrayList;
//
///**
// * Created by andream16 on 13.04.17.
// */
////Input(DistanceMatrix m, RouteList a); Output: RouteList;
//public class Exchange {
//
//    DistanceMatrix distances;
//    RouteList routes;
//
//    public Exchange(DistanceMatrix distances, RouteList routes){
//       this.distances = distances;
//       this.routes    = routes;
//    }
//
//    public RouteList exchange (Node node, Route route){
//        BestExchangeResult best_move = findBestExchange(node, route);
//        int pos_1, pos_2;
//        pos_1 = route.nodeList.indexOf(node);
//        pos_2 = best_move.route.indexOf(best_move.node_2);
//        route.get(pos_1) = best_move.node_2;
//        best_move.route.get(pos_2) = node;
//        RouteList new_routes = routes;
//        new_routes.at(route) = route;
//        new_routes.at(best_move.route) = best_move.route;
//
//        return new_routes;
//    }
//
//    public BestExchangeResult findBestExchange(Node node, Route route){
//
//        BestExchangeResult best_move = null;
//        int current_node_position = route.nodeList.indexOf(node);
//        int current_route_position = routes.at(route);
//        ArrayList<Node> node_list = route.nodeList;
//        int nodeList_size = node_list.size();
//        int routes_number = routes.size();
//        double distance;
//        double tmp_distance = 0.0;
//
//        //Foreach node in current route
//        for (int i = current_node_position+1; i<= nodeList_size; i++){
//           distance = distances.getDistance(node, node_list.get(i));
//           if( i == current_node_position+1 || distance < tmp_distance){
//               if(node.weight + route.weight <= route.maxWeight){
//                   if(node.nodeType == node_list.get(i).nodeType){
//                       best_move = new BestExchangeResult(node, node_list.get(i), route);
//                       tmp_distance = distance;
//                   }
//               }
//           }
//        }
//
//        //Foreach route in routes
//        for(int current_route = current_route_position+1; current_route<=routes_number; current_route++ ){
//            ArrayList<Node> current_nodeList = current_route.nodeList;
//            //For each node in it
//            for(int j=0; j<= current_nodeList.size(); j++){
//                distance = distances.getDistance(node, current_nodeList.get(j));
//                if( distance < tmp_distance){
//                    if(node.weight + current_route.weight <= current_route.maxWeight){
//                        if(node.nodeType == current_nodeList.get(j).nodeType || route.nodeList.size() - 1 == current_node_position){
//                            best_move = new BestExchangeResult(node, node_list.get(j), routes.at(current_route));
//                            tmp_distance = distance;
//                        }
//                    }
//                }
//            }
//        }
//
//        return best_move;
//
//    }
//
//}
//
//class BestExchangeResult {
//    public Node node, node_2;
//    public Route route;
//
//    public BestExchangeResult(Node node, Node node_2, Route route){
//      this.node = node;
//      this.node_2 = node_2;
//      this.route = route;
//    }
//}
