import utils.MaxWeightException;
import utils.NodeNotFoundException;

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
        ArrayList<Route> relocatedRoutes = new ArrayList<>();


        Relocate relocate = new Relocate(distances,routes, helper);
        try
        {
         relocatedRoutes = relocate.findBestRelocate(instance1.completeTSP);
        } catch (MaxWeightException e) {
            e.printStackTrace();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }

        boolean bo = false;


    }

}
