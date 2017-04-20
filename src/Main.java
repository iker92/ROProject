import utils.MaxWeightException;

import java.util.ArrayList;

/**
 * Created by pippo on 11/04/17.
 */
public class Main {

    public static void main(String [] args) {

        Helper helper = new Helper();

        String fileName = "Instances/A1.txt";
        Instance instance1 = helper.fileToInstance(fileName);

        DistanceMatrix distances= DistanceMatrix.initialize(instance1.nodesList);

        helper.initTSP(instance1);

        for (Node node: instance1.completeTSP){
            System.out.println(node.index);
        }

        ArrayList<Route> routes = helper.createRoutesFromInstance(instance1);
        ArrayList<Route> new_routes=new ArrayList<>();


        Relocate relocate=new Relocate(distances,routes);
        try {
         new_routes= relocate.relocate(routes.get(0).nodeList.get(0),routes.get(1));
        } catch (MaxWeightException e) {
            e.printStackTrace();
        }

        boolean bo=false;


    }

}
