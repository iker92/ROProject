import utils.MaxWeightException;
import utils.NodeNotFoundException;
import utils.RouteSizeException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


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

        BigDecimal relocateResult = new BigDecimal(0);
        BigDecimal exchangeResult = new BigDecimal(0);

        routes = doBestRelocate(distances, routes, helper, instance1.completeTSP);
        relocateResult = routes.getObjectiveFunction();
        routes = doBestExchange(routes, helper, instance1.completeTSP);
        exchangeResult = routes.getObjectiveFunction();


        long endTime = System.nanoTime();

        long executionTime= endTime - startTime ;

        NumberFormat formatter = new DecimalFormat("#0.00000");

        System.out.println("\n\nDone! Before the Optimization, the Objective function was: " + oldOF.toString()
                + "\nAfter the Relocation, the Objective function was: " + relocateResult.toString()
                + "\nAfter the Exchange, the Objective function was: " + exchangeResult.toString()
                +  "\nExecution time is " + formatter.format((executionTime) / 1000000000d) + " seconds");

        helper.printRoutes(routes);

        helper.writeToFile(routes, executionTime, FILE);
    }

    private static RouteList doBestRelocate(DistanceMatrix distances, RouteList routes, Helper helper, ArrayList<Node> tsp) {

        Relocate relocate = new Relocate(distances,routes,helper, tsp);
        RouteList optRoutes = new RouteList();
        try {
            optRoutes = relocate.findBestRelocate();
        } catch (MaxWeightException e) {
            e.printStackTrace();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        } catch (RouteSizeException e) {
            e.printStackTrace();
        }
        return optRoutes;

    }

    private static RouteList doBestExchange(RouteList routes, Helper helper, ArrayList<Node> completeTSP) {

        Exchange exchange = new Exchange(routes, helper, completeTSP);
        RouteList optRoutes = new RouteList();

        try {
            optRoutes = exchange.findBestExchange();
        } catch (MaxWeightException e) {
            e.printStackTrace();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }

        return optRoutes;

    }



}
