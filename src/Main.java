import org.apache.commons.io.FileUtils;
import utils.MaxWeightException;
import utils.NodeNotFoundException;
import utils.RouteSizeException;
import java.io.File;
import java.io.IOException;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by pippo on 11/04/17.
 */
public class Main {

    public static void main(String [] args) {

        long startTime = System.nanoTime();
        Helper helper = new Helper();

        String fileName = "Instances/B3.txt";
        Instance instance1 = helper.fileToInstance(fileName);

        DistanceMatrix distances = DistanceMatrix.initialize(instance1.nodesList);

        helper.initTSP(instance1);

        RouteList routes =  helper.createRoutesFromInstance(instance1);

        helper.printRoutes(routes);

        BigDecimal oldOF = routes.getObjectiveFunction();



        doBestRelocate(distances, routes, helper);

//        doBestExchange(routes, helper);


        long endTime = System.nanoTime();

        long executiontime= endTime - startTime ;
        System.out.println("Before the Optimization, the Objective function was: " + oldOF.toString());

        helper.printRoutes(routes);

        writeToFile(routes,executiontime);
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

    private static void writeToFile(RouteList routes,long time){
        File file = new File("/home/pippo/Documents/ROProject/test.txt");
        ArrayList<String> data=new ArrayList<>();
        ArrayList<String> routeNodes=new ArrayList<>();

        for (int i = 0; i <routes.size() ; i++) {
            routeNodes.clear();
            for (Node n: routes.get(i).nodeList) {

                if(routeNodes.size()!=0){

                    routeNodes.add(String.join(routeNodes.get(0),String.valueOf(n.index+" ")));}
                    else{
                    routeNodes.add(String.valueOf(n.index+" "));
                }


            }
            String nodes= (String.join("", routeNodes));


            data.add("Route "+i+" cost: "+routes.get(i).getActualDistance()+" \nweigth LINEHAUL :"+routes.get(i).weightLinehaul+" \nweigth BACKHAUL: "+routes.get(i).weightBackhaul+"\nRoute: "+nodes+"\n\n");

        }
        data.add("Objective function: "+routes.getObjectiveFunction()+"\n");
        NumberFormat formatter = new DecimalFormat("#0.00000");
        data.add("Execution time is " + formatter.format((time) / 1000000000d) + " seconds");

        try {

            FileUtils.writeLines(file,data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
