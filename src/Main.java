import core.*;
import functions.Exchange;
import functions.Relocate;
import utils.DistanceMatrix;
import utils.Helper;
import exceptions.MaxWeightException;
import exceptions.NodeNotFoundException;
import exceptions.RouteSizeException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


/**
 * Created by pippo on 11/04/17.
 */
public class Main {

    private static final String FILE = "F2";

    private static final String SUFFIX = ".txt";

    public static void main(String [] args) {

        long startTime = System.nanoTime();
        long endTime;

        Helper helper = new Helper();

        String fileName = FILE + SUFFIX;
        Instance instance1 = helper.fileToInstance(fileName);

        Optimization opt = new Optimization(instance1);
        ArrayList<Optimization.OptimizationResult> results = new ArrayList<>();

        results.add(opt.doRelocateExchange());
        results.add(opt.doExchangeRelocate());

        endTime = System.nanoTime();
        long executionTime = endTime - startTime;

        if(results.get(0).exchange.getValue().compareTo(results.get(1).relocate.getValue()) == 1)
        {
            helper.writeToFile(results.get(1).data, executionTime, instance1.fileName);
        } else
        {
            helper.writeToFile(results.get(0).data, executionTime, instance1.fileName);
        }
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.println("\nExecution time is " + formatter.format((executionTime) / 1000000000d) + " seconds");

    }
}
