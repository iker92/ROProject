import utils.MaxWeightException;
import utils.NodeNotFoundException;
import utils.RouteSizeException;

import java.math.BigDecimal;


/**
 * Created by pippo on 11/04/17.
 */
public class Main {

    private static final String FILE = "A1.txt";

    public static void main(String [] args) {

        long startTime = System.nanoTime();
        Helper helper = new Helper();

        String fileName = "Instances/" + FILE;
        Instance instance1 = helper.fileToInstance(fileName);

        DistanceMatrix distances = DistanceMatrix.initialize(instance1.nodesList);

        helper.initTSP(instance1);

        RouteList routes =  helper.createRoutesFromInstance(instance1);

        helper.printRoutes(routes);

        BigDecimal oldOF = routes.getObjectiveFunction();


        System.out.println("Debug mode: " + Values.isDebug().toString());



        doBestRelocate(distances, routes, helper);

//        doBestExchange(routes, helper);


        long endTime = System.nanoTime();

        long executiontime= endTime - startTime ;
        System.out.println("Before the Optimization, the Objective function was: " + oldOF.toString());

        helper.printRoutes(routes);

        helper.writeToFile(routes,executiontime, FILE);
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
