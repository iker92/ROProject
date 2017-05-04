import utils.MaxWeightException;
import utils.NodeNotFoundException;
import utils.RouteSizeException;

import java.math.BigDecimal;
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

        helper.printRoutes(routes);

        BigDecimal oldOF = routes.getObjectiveFunction();



        doBestRelocate(distances, routes, helper);

//        doBestExchange(routes, helper);



        System.out.println("Before the Optimization, the Objective function was: " + oldOF.toString());

    }

    private static void doBestRelocate(DistanceMatrix distances, RouteList routes, Helper helper) {

        Relocate relocate = new Relocate(distances,routes,helper);

        try {
            relocate.findBestRelocate();
        } catch (MaxWeightException e) {
            e.printStackTrace();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        } catch (RouteSizeException e) {
            e.printStackTrace();
        }

    }

    private static  void doBestExchange(RouteList routes, Helper helper) {

        Exchange exchange = new Exchange(routes, helper);

        try {
            exchange.findBestExchange();
        } catch (MaxWeightException e) {
            e.printStackTrace();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }


    }

}
