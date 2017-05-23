import core.*;
import exceptions.MaxWeightException;
import exceptions.NodeNotFoundException;
import exceptions.RouteSizeException;
import functions.Exchange;
import functions.Relocate;
import utils.DistanceMatrix;
import utils.Helper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.util.Pair;


/**
 * Created by loriz on 14/05/17.
 */
public class Optimization {

    private final Instance instance;
    private final Helper helper;
    private final int NUMBER_OF_TIMES = 10;

    public Optimization(Instance instance) {
        this.instance = instance;
        this.helper = new Helper();
    }

    public OptimizationResult doRelocateExchange() {

        System.out.println("********************************************************");
        System.out.println("********************************************************");
        System.out.println("*************RELOCATE EXCHANGE*********************");

        ArrayList<OptimizationResult> resultList = new ArrayList<>();
        DistanceMatrix distances = DistanceMatrix.initialize(instance.nodesList);
        helper.initTSP(instance);

        for (int i=0; i < NUMBER_OF_TIMES; i++) {

            ArrayList<ResultData> finalSnap = new ArrayList<>();

            //initial route
            RouteList routes = helper.createRoutesFromInstance(instance,i);
            Pair initialSnap = new Pair(helper.createSnapshot(routes), routes.getObjectiveFunction());


            BigDecimal oldOF = routes.getObjectiveFunction();

            System.out.println("Debug mode: " + Values.isDebug().toString());


            doBestRelocate(distances, routes, helper, instance.completeTSP);
            Pair relocateSnap = new Pair(helper.createSnapshot(routes), routes.getObjectiveFunction());

            doBestExchange(routes, helper, instance.completeTSP);
            Pair exchangeSnap = new Pair(helper.createSnapshot(routes), routes.getObjectiveFunction());

            for (Route route : routes) {
                finalSnap.add(new ResultData(route.getActualDistance(), route.weightLinehaul, route.weightBackhaul, helper.createSnapshot(route), routes.getObjectiveFunction()));
            }

            HashMap routeMap = new HashMap();

            for (int pair = 0; pair < finalSnap.size(); pair++) {
                routeMap.put(finalSnap, routes.get(0).getActualDistance());
            }


            resultList.add(new OptimizationResult(oldOF, initialSnap, relocateSnap, exchangeSnap, finalSnap));
        }


        OptimizationResult best = resultList.get(0);

        for (OptimizationResult result : resultList) {

            if (best == null) best = result;

            if (result.exchange.getValue().compareTo(best.exchange.getValue()) == -1) {
                best = result;
            }
        }

        System.out.print("//////////////////////////////////////////////////////////////////////////////\n");
        System.out.print("//////////////////////////////////////////////////////////////////////////////\n");
        System.out.print("//////////////////////////////////////////////////////////////////////////////\n\n");
        System.out.println("RelocateExchange cycle finished! Presenting results...\n");

        System.out.println("Initial Routes (Objective Function = " + best.initial.getValue() + ")");
        System.out.print(best.initial.getKey());

        System.out.println("Relocated Routes (Objective Function = " + best.relocate.getValue() + ")");
        System.out.print(best.relocate.getKey());

        System.out.println("Exchanged Routes (Objective Function = " + best.exchange.getValue() + ")");
        System.out.print(best.exchange.getKey());

        return best;
    }

    public OptimizationResult doExchangeRelocate() {

        System.out.println("********************************************************");
        System.out.println("********************************************************");
        System.out.println("************* EXCHANGE RELOCATE *********************");

        ArrayList<OptimizationResult> resultList = new ArrayList<>();
        DistanceMatrix distances = DistanceMatrix.initialize(instance.nodesList);
        helper.initTSP(instance);

        for (int i=0; i < NUMBER_OF_TIMES; i++) {

            ArrayList<ResultData> finalSnap = new ArrayList<>();

            //initial route
            RouteList routes = helper.createRoutesFromInstance(instance,i);
            Pair initialSnap = new Pair(helper.createSnapshot(routes), routes.getObjectiveFunction());


            BigDecimal oldOF = routes.getObjectiveFunction();

            System.out.println("Debug mode: " + Values.isDebug().toString());

            doBestExchange(routes, helper, instance.completeTSP);
            Pair exchangeSnap = new Pair(helper.createSnapshot(routes), routes.getObjectiveFunction());

            doBestRelocate(distances, routes, helper, instance.completeTSP);
            Pair relocateSnap = new Pair(helper.createSnapshot(routes), routes.getObjectiveFunction());

            for (Route route : routes) {
                finalSnap.add(new ResultData(route.getActualDistance(), route.weightLinehaul, route.weightBackhaul, helper.createSnapshot(route), routes.getObjectiveFunction()));
            }

            HashMap routeMap = new HashMap();

            for (int pair = 0; pair < finalSnap.size(); pair++) {
                routeMap.put(finalSnap, routes.get(0).getActualDistance());
            }

            resultList.add(new OptimizationResult(oldOF, initialSnap, relocateSnap, exchangeSnap, finalSnap));
        }


        OptimizationResult best = resultList.get(0);

        for (OptimizationResult result : resultList) {

            if (best == null) best = result;

            if (result.relocate.getValue().compareTo(best.relocate.getValue()) == -1) {
                best = result;
            }
        }

        System.out.print("//////////////////////////////////////////////////////////////////////////////\n");
        System.out.print("//////////////////////////////////////////////////////////////////////////////\n");
        System.out.print("//////////////////////////////////////////////////////////////////////////////\n\n");
        System.out.println("ExchangeRelocate cycle finished! Presenting results...\n");

        System.out.println("Initial Routes (Objective Function = " + best.initial.getValue() + ")");
        System.out.print(best.initial.getKey());

        System.out.println("Exchanged Routes (Objective Function = " + best.exchange.getValue() + ")");
        System.out.print(best.exchange.getKey());

        System.out.println("Relocated Routes (Objective Function = " + best.relocate.getValue() + ")");
        System.out.print(best.relocate.getKey());

        return best;
    }

    
    private RouteList doBestRelocate(DistanceMatrix distances, RouteList routes, Helper helper, ArrayList<Node> tsp) {

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


    private RouteList doBestExchange(RouteList routes, Helper helper, ArrayList<Node> completeTSP) {

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


    public class OptimizationResult {


        public final BigDecimal oldOF;
        public final Pair<String, BigDecimal> initial;
        public final Pair<String, BigDecimal> relocate;
        public final Pair<String, BigDecimal> exchange;
        public final ArrayList<ResultData> data;

        public OptimizationResult(BigDecimal oldOF, Pair<String, BigDecimal> initial, Pair<String, BigDecimal> relocate, Pair<String, BigDecimal> exchange, ArrayList<ResultData> data) {

            this.oldOF = oldOF;
            this.initial = initial;
            this.relocate = relocate;
            this.exchange = exchange;
            this.data = data;
        }
    }



}
