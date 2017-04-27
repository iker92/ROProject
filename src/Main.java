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

        DistanceMatrix distances = DistanceMatrix.initialize(instance1.nodesList);

        helper.initTSP(instance1);

        for (Node node: instance1.completeTSP){
            System.out.println(node.index);
        }

        RouteList routes =  helper.createRoutesFromInstance(instance1);

        RouteList exchangedRoutes = new RouteList();

        Exchange exchange = new Exchange(routes, helper);

        try {
            exchangedRoutes = exchange.findBestExchange();
            exchangedRoutes.size();
        } catch (MaxWeightException e) {
            e.printStackTrace();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }

    }
}
