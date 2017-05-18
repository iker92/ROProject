package utils;

import core.*;
import org.apache.commons.io.FileUtils;
import exceptions.MaxWeightException;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * This class contains several helper methods used by different classes to perform the two phases algorithm.
 */
public class Helper {

    private static final String PATH = "Results/";

    private Boolean isDebug = Values.isDebug();

    /**
     * fileToInstance(String fileName) reads the txt file containing the instance and parses it in order to work with them.
     * @param fileName
     * @return Parsed instance
     */
    public Instance fileToInstance(String fileName) {

        Instance instance = new Instance();

        instance.fileName = fileName;

        fileName = "Instances/" + fileName;

        String line = null;
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);
            int i=0;

            while((line = bufferedReader.readLine()) != null) {
                i++;
                String[] splitted = line.split("\\s+");

                switch (i) {
                    case 1:
                        instance.nodeCount = Integer.valueOf(line);
                        break;
                    case 2:
                        break;
                    case 3:
                        instance.routeCount = Integer.valueOf(line);
                        break;
                    case 4: {

                        for (int j = 0; j <=instance.nodeCount; j++) {
                            instance.indexesCompleteTSP.add(Integer.valueOf(splitted[j]));
                        }
                        break;

                    }
                    case 5: {
                        for (int j = 0; j <splitted.length; j++) {
                            instance.indexesLineHaulTSP.add(Integer.valueOf(splitted[j]));
                        }
                        break;
                    }
                    case 6: {
                        for (int j = 0; j <splitted.length; j++) {
                            instance.indexesBackHaulTSP.add(Integer.valueOf(splitted[j]));
                        }
                        break;
                    }
                    default:
                        if (i == 7) {
                            Coordinates coordinates =new Coordinates(Integer.valueOf(splitted[0]),Integer.valueOf(splitted[1]));
                            Node node = new Node(coordinates, Values.nodeType.WAREHOUSE ,0,false,i-7);
                            instance.nodesList.add(node);
                            instance.maxWeight = Integer.valueOf(splitted[3]);
                        }
                        else {
                            Coordinates coordinates = new Coordinates(Integer.valueOf(splitted[0]), Integer.valueOf(splitted[1]));
                            Values.nodeType nodeType;
                            if (Integer.valueOf(splitted[2]).equals(0)) {
                                nodeType = Values.nodeType.BACKHAUL;
                            } else {
                                nodeType = Values.nodeType.LINEHAUL;
                            }
                            if (nodeType == Values.nodeType.BACKHAUL) {
                                Node node = new Node(coordinates, nodeType, Integer.valueOf(splitted[3]), false,i-7);
                                instance.nodesList.add(node);
                            } else {
                                Node node = new Node(coordinates, nodeType, Integer.valueOf(splitted[2]), false,i-7);
                                instance.nodesList.add(node);
                            }
                        }

                        break;
                }



            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }

        return instance;

    }

    /**
     * initTSP(Instance instance) creates 3 different TSP, complete, backHaul only and lineHaul only
     * @param instance
     */
    public void initTSP(Instance instance){
        instance.completeTSP=instance.createTSPFromNodes(instance.nodesList,instance.indexesCompleteTSP);
        instance.backHaulTSP=instance.createTSPFromNodes(instance.nodesList,instance.indexesBackHaulTSP);
        instance.lineHaulTSP=instance.createTSPFromNodes(instance.nodesList,instance.indexesLineHaulTSP);
    }

    /**
     * createRoutesFromInstance(Instance instance) given the parsed instance, returns all the valid initial routes.
     * @param instance
     * @return RouteList which contains all the built routes
     */
    public RouteList createRoutesFromInstance(Instance instance)  {

        System.out.println("Creating routes from an instance...\n");

        DistanceMatrix distances = DistanceMatrix.getInstance();

        RouteList routes = new RouteList();

        Route route = new Route(instance.maxWeight);

        ArrayList<Node> tsp = new ArrayList<Node>(instance.completeTSP);

        Node warehouse = tsp.get(0);
        tsp.remove(0);

        int tspSize = tsp.size();

        //Default size of the routes
        int routeSize = tspSize/instance.routeCount;

        //While we have at least one node in the tsp and tbe number of built routes is less than the given number of routes
        while (tsp.size() - 1 >= 0 && routes.size() < instance.routeCount) {

            //If current route isn't filled yet
            if (route.getNodeList().size() < routeSize) {

                //Try to fill it with another node
                try {
                    route.addNode(tsp.get(0));
                    tsp.remove(0);
                } catch (MaxWeightException e) {
                    //If not possible, try to create a new route
                    try {
                        route = routeBuilder(route, warehouse);
                        routes.add(route);
                    } catch (MaxWeightException e1) {}
                    route = new Route(instance.maxWeight);
                }

            } else {
                //If it's filled indeed, try to create a new route
                try {
                    route = routeBuilder(route, warehouse);
                    routes.add(route);
                } catch (MaxWeightException e) {}
                route = new Route(instance.maxWeight);

            }

        }

        if (routes.size() < instance.routeCount) {
            try {
                route = routeBuilder(route, warehouse);
                routes.add(route);
            } catch (MaxWeightException e) {}
            route = new Route(instance.maxWeight);
        }

        //Recover all remaining nodes from temporal route and tsp
        ArrayList<Node> remainingNodes = new ArrayList<>();
        ArrayList<Node> nodesToCheckInOrder = new ArrayList<>();

        //Let's add all the discarded nodes inside an array of nodes
        if (route.getNodeList().size() != 0) {
            for (Node node : route.getNodeList()) remainingNodes.add(node);
        }

        //While the tsp isn't empty, let's add the current node into remainingNodes
        while (tsp.size()-1 >= 0) {
            remainingNodes.add(tsp.get(0));
            tsp.remove(0);
        }

        //Used to exit from the while. It's decremented each time we are done analyzing a node.
        int steps = remainingNodes.size();

        while (steps > 0) {
            //Let's get the closer nodes to the current one by distance. They are sorted by distance ascending.
            nodesToCheckInOrder = distances.getClosestNodes(remainingNodes.get(0), instance.completeTSP);

            for (Node node : nodesToCheckInOrder) {
                //If the current node and the other remaining nodes are compared, skip
                if (remainingNodes.contains(node)) continue;
                try {
                    //Let's try to relocate the current discarded node into the route of the current closest node
                    if (canPositionate(remainingNodes.get(0), node.getRoute(), node.getRoute().getIndexByNode(node))) {

                        //If it's possible indeed, update all the data
                       node.getRoute().getNodeList().add(node.getRoute().getIndexByNode(node), remainingNodes.get(0));
                        remainingNodes.get(0).setRoute(node.getRoute());
                        if(remainingNodes.get(0).getType() == Values.nodeType.LINEHAUL) {
                            node.getRoute().weightLinehaul += remainingNodes.get(0).getWeight();
                        } else {
                            node.getRoute().weightBackhaul += remainingNodes.get(0).getWeight();
                        }
                        node.getRoute().forceUpdate();

                        //Remove the node from the remaining nodes
                        remainingNodes.remove(0);
                        break;

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Let's go to the next node
            steps--;

        }

        ///////////////////////////////////// SCRAPPED NODES MANAGEMENT //////////////////////////////////////////////

        if (remainingNodes.size() > 0 && routes.size() == instance.routeCount) {
            System.out.println("Now starting the management for the nodes that wouldn't fit the nodes on the first pass... \n");
            System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////\n");


            while (remainingNodes.size() > 0) {

                try {
                    relocateScrapped(remainingNodes.get(0), routes);
                    System.out.println("Relocation of misplaced nodes successful! \n");

                    remainingNodes.remove(0);

                } catch (Exception e) {

                    System.out.println("!!! Couldn't relocate all the scrapped nodes with actual setup... !!!");

                    int lightestRouteIndex = getLightestRoute(routes, Values.nodeType.LINEHAUL);

                    System.out.println("Making space in the lightest route (index = " + lightestRouteIndex + ") and retrying... \n");

                    try {
                        relocateScrapped(routes.get(lightestRouteIndex), routes);
                    } catch (Exception e1) {
                    }


                }
            }
        }

        System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////");

        System.out.println("\nRoutes ready! Now validating... ");


        boolean validationFailed = false;

        for (Route r : routes) {
            if ( r.validate() == false ) {
                validationFailed = true;
                System.out.println("core.Route " + routes.indexOf(r) + " is invalid");
            }
        }

        if (validationFailed) {
            System.out.println("\n!!! Validation terminated with errors! Check them up here! !!!");
        } else {
            System.out.println("\nValidation terminated without errors!");
        }

        return routes;
    }

    /**
     * canPositionate(Node node, Route route, int position) tests if it is possible to relocate a given discarded node
     * inside a given route, given the position of the examinated node inside the latter.
     * @param node discarded node
     * @param route route containing analyzed node.
     * @param position position of the node in the latter route. This node is the closest to node.
     * @return true or false if it's possible or not
     */
    public boolean canPositionate(Node node, Route route, int position) {

        if (isDebug) System.out.println("\nTrying to positionate " + node.getIndex() );

        //if trying to relocate in place of the first warehouse
        if (position == 0) {
            if (isDebug) System.out.println("Positionate is impossible! Trying to put something before first WAREHOUSE\n");
            return false;
        }

        if (node.getType() == Values.nodeType.WAREHOUSE) {
            if (isDebug) System.out.println("Positionate is impossible! Node to relocate is WAREHOUSE\n");
            return false;
        }

        Values.nodeType nodeType = node.getType();

        Values.nodeType previousType = route.getNodeList().get(position - 1).getType();
        Values.nodeType nextType = route.getNodeList().get(position).getType();

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

        //Check actual weight to satisfy the weight constraint
        int actualTypeWeight = (nodeType == Values.nodeType.LINEHAUL ? route.weightLinehaul : route.weightBackhaul) + node.getWeight();

        if (actualTypeWeight <= route.MAX_WEIGHT) {
            if (isDebug) System.out.println("Relocate is possible! Checking if it's worth...\n");
        } else {
            if (isDebug) System.out.println("Relocate is impossible!\n");
        }

        return actualTypeWeight <= route.MAX_WEIGHT;

    }

    /**
     * routeBuilder(Route route, Node warehouse) shuffles a given route in order to randomize the routes.
     * @param route route to shuffle
     * @param warehouse
     * @return shuffled route
     * @throws MaxWeightException
     */
    private Route routeBuilder(Route route, Node warehouse) throws MaxWeightException {

        long seed = System.nanoTime();
        Collections.shuffle(route.getNodeList(), new Random(seed));

        for (int nodeIndex = 0; nodeIndex < route.getNodeList().size(); nodeIndex++) {
            if (route.getNode(nodeIndex).getType() == Values.nodeType.BACKHAUL && route.getNode(nodeIndex).isTaken() == false) {

                route.getNode(nodeIndex).take();
                route.addNode(route.getNode(nodeIndex));

                nodeIndex--;
            }
        }

        for (Node n : route.getNodeList()) n.release();

        route.addNode(0, warehouse);
        route.addNode(warehouse);

        return route;
    }

    /**
     * getRouteIndexByNode(RouteList routes, Node node) returns index of given route in the set of all routes
     * containing the passed node
     * @param routes
     * @param node
     * @return index of the route containing the passed node
     */
    public int getRouteIndexByNode(RouteList routes, Node node) {
        int index = -1;
        for (Route route : routes) {
            index = (route.getNodeList().contains(node) ? routes.indexOf(route) : -1);
            if(index != -1) return index;
        }
        return index;
    }

    /**
     * getLightestRoute(RouteList routes, Values.nodeType type) returns index of lightestRoute
     * @param routes set of all routes
     * @param type type of a given node
     * @return index of the lightest route in the routes set
     */
    public int getLightestRoute(RouteList routes, Values.nodeType type) {

        int oldWeight = -1;
        Route chosenRoute = null;

        for (Route r : routes) {

            int actualWeight = (type == Values.nodeType.LINEHAUL? r.weightLinehaul : r.weightBackhaul);

            if (oldWeight == -1 || actualWeight < oldWeight) {
                oldWeight = actualWeight;
                chosenRoute = r;
            }

        }

        return routes.indexOf(chosenRoute);
    }

    /**
     * relocateScrapped(Route route, RouteList routes) tries to relocate a node inside a given route
     * into another route respecting all the constraints
     * @param route current route
     * @param routes set of all routes
     * @throws Exception
     */
    public void relocateScrapped(Route route, RouteList routes) throws Exception {

        ArrayList<Node> mNodes = new ArrayList<>(route.getNodeList());

        for (Node n : mNodes) {

            if (n.getType() == Values.nodeType.WAREHOUSE) continue;

            try {
                relocateScrapped(n, routes);
            } catch (Exception e) {}
        }

        if (route.getNodeList().size() != 0) {
            throw new Exception("!!! Cannot relocate all the nodes !!!");
        }

    }

    /**
     * relocateScrapped(Node node, RouteList routes) tries to relocate passed node
     * into another route respecting all the constraints
     * @param node
     * @param routes
     * @throws Exception
     */
    public void relocateScrapped(Node node, RouteList routes) throws Exception {

        boolean relocated = false;

        for (Route r : routes) {

            if (r.getNodeList().contains(node)) continue;

            try {
                r.addNode( (node.getType() == Values.nodeType.BACKHAUL? r.getNodeList().size()-1 : 1), node);
                relocated = true;
                break;
            } catch (MaxWeightException e) {}
        }


        if (relocated == false) {
            throw new Exception("!!! Cannot relocate the node !!!");
        }

    }

    /**
     * createSnapshot(RouteList routes) creates string containing the set of routes in order to pretty print them
     * @param routes
     * @return string containing the set of routes
     */
    public String createSnapshot(RouteList routes){

        StringBuilder sb = new StringBuilder();

        sb.append("\n");


        for (Route r : routes) {
            sb.append(routes.indexOf(r) + "  | ");

            for (Node n : r.getNodeList()) {
                sb.append(n.getIndex() + (n.getType().toString().substring(0, 1)) + "\t  ");
            }
            sb.append("\n");

        }

        sb.append("\n");
        sb.append("\n");


        return sb.toString();
    }

    /**
     * createSnapshot(Route route) creates string containing the set of nodes in the nodeList of a route
     * @param route
     * @return string containing the set of nodes in the nodeList of a route
     */
    public String createSnapshot(Route route) {

        StringBuilder sb = new StringBuilder();

        for (Node n : route.getNodeList()) {
            sb.append(n.getIndex() + (n.getType().toString().substring(0, 1)) + "\t");
        }

        sb.append("\n");
        sb.append("\n");

        return sb.toString();

    }

    /**
     * printRoutes(RouteList routes) prints all the routes
     * @param routes
     */
    public void printRoutes(RouteList routes) {

        System.out.print("\n");


        for (Route r : routes) {
            System.out.print(routes.indexOf(r) + "  | ");

            for (Node n : r.getNodeList()) {
                System.out.print(n.getIndex() + (n.getType().toString().substring(0, 1)) + "\t");
            }
            System.out.print("\n");

        }

        System.out.print("\n");
        System.out.print("\n");

    }

    /**
     * printRoute(Route route) prints a route
     * @param route
     */
    public void printRoute(Route route) {

        for (Node n : route.getNodeList()) {
            System.out.print(n.getIndex() + (n.getType().toString().substring(0, 1)) + "\t");
        }

        System.out.print("\n");
        System.out.print("\n");

    }

    /**
     * writeToFile(ArrayList<ResultData> routesData, long time, String fileName) write results on file
     * @param routesData
     * @param time
     * @param fileName
     */
    public void writeToFile(ArrayList<ResultData> routesData, long time, String fileName){
        File file = new File(PATH + fileName);
        ArrayList<String> data=new ArrayList<>();
        ArrayList<String> routeNodes=new ArrayList<>();

        for (int i = 0; i <routesData.size() ; i++) {
            routeNodes.clear();
            data.add("Route "+i+" cost: "+routesData.get(i).getRouteCost()+" \nweight LINEHAUL :"+routesData.get(i).getWeightLH()+" \nweight BACKHAUL: "+routesData.get(i).getWeightBH()+"\nRoute: "+routesData.get(i).getRouteAsString()+"\n\n");
        }

        data.add("Objective function: "+routesData.get(0).getTotalObjectiveFunction()+"\n");
        NumberFormat formatter = new DecimalFormat("#0.00000");
        data.add("Execution time is " + formatter.format((time) / 1000000000d) + " seconds");

        try {

            FileUtils.writeLines(file,data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
