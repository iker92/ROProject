import core.Coordinates;
import utils.MaxWeightException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
                            Node node = new Node(coordinates,Values.nodeType.WAREHOUSE ,Integer.valueOf(splitted[3]),false,i-7);
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


    /**
     * createRoutesFromInstance takes an instance and generates an arraylist of routes. The routes are popolated
     *                          by filling up each route with as many nodes taken from the linehaulTSP as possible
     *                          (until the weight limit is reached). Then it ensures that all routes are popolated
     *                          and the backhaul are inserted.
     * @param instance1 input instance
     * @return the corresponding ArrayList<Route>
     */
    public ArrayList<Route> createRoutesFromInstance(Instance instance1) {

        ArrayList<Route> routes = new ArrayList<>();

        Route route = new Route(instance1.maxWeight);

        ArrayList<Node> lineHaulTSP = instance1.lineHaulTSP;

        /////////////////////// ROUTES GENERATION (LINEHAUL ONLY, ROUTES NUMBER NOT VERIFIED) ////////////////////////////

        // TODO: this is not tested for cases where the linehauls form a number of routes bigger than the limit

        while (lineHaulTSP.size()-1 >= 0) {

            try {
                route.addNode(lineHaulTSP.get(0));
                lineHaulTSP.remove(0);
            } catch (MaxWeightException e) {
                routes.add(route);
                route = new Route(instance1.maxWeight);
            }

        }

        if (route.nodeList.size() != 0) {
            routes.add(route);
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        ////////////////////////////////// ROUTES NUMBER VERIFICATION AND REARRANGEMENT //////////////////////////////////

        int oldRouteSize = routes.size();
        while (routes.size() < instance1.routeCount) {

            route = new Route(instance1.maxWeight);

            for (int i=0; i < oldRouteSize; i++) {

                if (routes.get(i).nodeList.size() > 1) {
                    try {
                        Node node = routes.get(i).getNode(0);
                        route.addNode(node);
                        routes.get(i).removeNode(node);
                        routes.add(route);
                        break;
                    } catch (MaxWeightException e) {}
                }
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        ///////////////////////////////////////// ADDING BACKHAULS TO ROUTES /////////////////////////////////////////////

        ArrayList<Node> backHaulTSP = instance1.backHaulTSP;

        //TODO: this is untested for cases where you have more backhaul than routes, it SHOULD work
        for (int i=0; i < backHaulTSP.size(); i++) {

            try {
                // this exception can never be triggered, this try/catch is useless but needed because Java likes o' piesc
                routes.get(i % (routes.size()-1 )).addNode(backHaulTSP.get(i));
            } catch (MaxWeightException e) {}

        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        return routes;
    }

}
