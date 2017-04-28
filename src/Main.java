import utils.MaxWeightException;
import utils.NodeNotFoundException;
import utils.RouteSizeException;

import java.util.ArrayList;

/**
 * Created by pippo on 11/04/17.
 */
public class Main {

    public static void main(String [] args) {

        Helper helper = new Helper();

        String fileName = "Instances/A1.txt";
        Instance instance1 = helper.fileToInstance(fileName);

        DistanceMatrix distances = DistanceMatrix.initialize(instance1.nodesList);

        helper.initTSP(instance1);

        RouteList routes =  helper.createRoutesFromInstance(instance1);

        printRoutes(routes);


        RouteList exchangedRoutes = new RouteList();
        RouteList relocatedRoutes = new RouteList();
        Relocate relocate = new Relocate(distances,routes,helper);

        try {
            relocatedRoutes = relocate.findBestRelocate(instance1.completeTSP);
            printRoutes(routes);
            relocatedRoutes.size();
        } catch (MaxWeightException e) {
            e.printStackTrace();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        } catch (RouteSizeException e) {
            e.printStackTrace();
        }

        Exchange exchange = new Exchange(routes, helper);

        try {
            exchangedRoutes = exchange.findBestExchange();
            printRoutes(routes);
            exchangedRoutes.size();
        } catch (MaxWeightException e) {
            e.printStackTrace();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }


    }

    private static void printRoutes(RouteList routes) {

        for (Route r : routes) {
            System.out.print(routes.indexOf(r) + "  | ");

            for (Node n : r.nodeList) {
                System.out.print(n.index + (n.getType().toString().substring(0, 1)) + "\t");
            }
            System.out.print("\n");

        }

        System.out.print("\n");
        System.out.print("\n");

    }
}
