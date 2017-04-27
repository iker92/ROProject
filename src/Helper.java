import core.Coordinates;
import utils.MaxWeightException;
import utils.SwapFailedException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by loriz on 4/13/17.
 */
public class Helper {

    public Instance fileToInstance(String fileName) {

        Instance instance = new Instance();

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
                            Node node = new Node(coordinates,Values.nodeType.WAREHOUSE ,0,false,i-7);
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
            // Or we could just do this:
            // ex.printStackTrace();
        }

        return instance;

    }



    public void initTSP(Instance instance){
        instance.completeTSP=instance.createTSPFromNodes(instance.nodesList,instance.indexesCompleteTSP);
        instance.backHaulTSP=instance.createTSPFromNodes(instance.nodesList,instance.indexesBackHaulTSP);
        instance.lineHaulTSP=instance.createTSPFromNodes(instance.nodesList,instance.indexesLineHaulTSP);
    }



    public RouteList createRoutesFromInstance(Instance instance)  {

        System.out.println("Creating routes from an instance...\n");


        RouteList routes = new RouteList();

        Route route = new Route(instance.maxWeight);

        ArrayList<Node> tsp = new ArrayList<Node>(instance.completeTSP);

        Node warehouse = tsp.get(0);
        tsp.remove(0);

        int tspSize = tsp.size();

        int routeSize = tspSize/instance.routeCount;


        while (tsp.size() - 1 >= 0) {

            if (route.nodeList.size() < routeSize) {

                try {
                    route.addNode(tsp.get(0));
                    tsp.remove(0);
                } catch (MaxWeightException e) {
                    try {
                        route = routeBuilder(route, warehouse);
                        routes.add(route);
                    } catch (MaxWeightException e1) {}
                    route = new Route(instance.maxWeight);
                }

            } else {

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

        ///////////////////////////////////// SCRAPPED NODES MANAGEMENT //////////////////////////////////////////////

        Route scrapped = route;

        if (scrapped.nodeList.size() != 0 && routes.size() == instance.routeCount) {
            System.out.println("Now starting the management for the nodes that wouldn't fit the nodes on the first pass... \n");
            System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////\n");


            while (scrapped.nodeList.size() != 0) {

                try {
                    relocateScrapped(scrapped, routes);
                    System.out.println("Relocation of misplaced nodes successful! \n");

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
                System.out.println("Route " + routes.indexOf(r) + " is invalid");
            }
        }

        if (validationFailed) {
            System.out.println("\n!!! Validation terminated with errors! Check them up here! !!!");
        } else {
            System.out.println("\nValidation terminated without errors!");
        }

        return routes;
    }



    private Route routeBuilder(Route route, Node warehouse) throws MaxWeightException {

        long seed = System.nanoTime();
        Collections.shuffle(route.nodeList, new Random(seed));

        for (int nodeIndex = 0; nodeIndex < route.nodeList.size(); nodeIndex++) {
            if (route.getNode(nodeIndex).getType() == Values.nodeType.BACKHAUL && route.getNode(nodeIndex).taken == false) {

                route.getNode(nodeIndex).take();
                route.addNode(route.getNode(nodeIndex));

                nodeIndex--;
            }
        }

        for (Node n : route.nodeList) n.release();

        route.addNode(0, warehouse);
        route.addNode(warehouse);

        return route;
    }



    public int getRouteIndexByNode(RouteList routes, Node node) {
        int index = -1;
        for (Route route : routes) {
            index = (route.nodeList.contains(node) ? routes.indexOf(route) : -1);
            if(index != -1) return index;
        }
        return index;
    }



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



    public void relocateScrapped(Route route, RouteList routes) throws Exception {

        ArrayList<Node> mNodes = new ArrayList<>(route.nodeList);

        for (Node n : mNodes) {

            if (n.getType() == Values.nodeType.WAREHOUSE) continue;

            try {
                relocateScrapped(n, routes);
            } catch (Exception e) {}
        }

        if (route.nodeList.size() != 0) {
            throw new Exception("!!! Cannot relocate all the nodes !!!");
        }

    }



    public void relocateScrapped(Node node, RouteList routes) throws Exception {

        boolean relocated = false;

        for (Route r : routes) {

            if (r.nodeList.contains(node)) continue;

            try {
                r.addNode( (node.getType() == Values.nodeType.BACKHAUL? r.nodeList.size()-1 : 1), node);
                relocated = true;
                break;
            } catch (MaxWeightException e) {}
        }


        if (relocated == false) {
            throw new Exception("!!! Cannot relocate the node !!!");
        }

    }



    public void swapNodes(Node first, Node second) throws SwapFailedException {
        // if called with nodes of the same route, call the appropriate function
        if (first.getRoute() == second.getRoute()) {
            first.getRoute().swap(first,second);
        } else {

            // check if swapping the nodes (second in place of the first and vice versa) would cause trouble
            if (first.getRoute().canSwap(first, second) && second.getRoute().canSwap(second, first)) {

                int firstPosition = first.getRoute().nodeList.indexOf(first);
                int secondPosition = second.getRoute().nodeList.indexOf(second);

                Route firstRoute = first.getRoute();
                Route secondRoute = second.getRoute();

                firstRoute.removeNode(first);
                secondRoute.removeNode(second);

                try {
                    firstRoute.addNode(firstPosition, second);
                    secondRoute.addNode(secondPosition, first);
                } catch (MaxWeightException e) {
                    throw new SwapFailedException("!!! Swap failed! !!!");
                }

            }
        }
    }



    public void swapNodesIfWorth(Node first, Node second) throws SwapFailedException {

        Route firstRoute = first.getRoute();
        Route secondRoute = second.getRoute();
        double firstDistance = firstRoute.getActualDistance();
        double secondDistance = secondRoute.getActualDistance();

        double distanceSum = firstDistance + secondDistance;

        if (distanceSum > (swapAndCalculateDistance(first, second) + swapAndCalculateDistance(second, first))) {

            swapNodes(first, second);

        } else {
            throw new SwapFailedException("!!! Swapping these nodes would increase the objective function! !!!");
        }
        
    }



    private double swapAndCalculateDistance(Node first, Node second) {

        DistanceMatrix distances = DistanceMatrix.getInstance();
        Route firstRoute = first.getRoute();

        int firstIndex = firstRoute.nodeList.indexOf(first);
        double actualDistanceFirst = 0.0;

        Node actual;
        Node next;

        for (int index = 0; index < firstRoute.nodeList.size() - 1; index++) {

            if (firstIndex == index) {
                actual = second;
                next = firstRoute.nodeList.get(index + 1);
            } else if (firstIndex == index + 1) {
                actual = firstRoute.nodeList.get(index);
                next = second;
            } else {
                actual = firstRoute.nodeList.get(index);
                next = firstRoute.nodeList.get(index + 1);
            }

            actualDistanceFirst += distances.getDistance(actual, next);

        }

        return actualDistanceFirst;

    }


}
