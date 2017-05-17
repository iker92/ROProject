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

    private static final String FILE = "B1";
    private static final String SUFFIX = ".txt";

    public static void main(String [] args) {

        Helper helper = new Helper();

        String fileName = FILE + SUFFIX;
        Instance instance1 = helper.fileToInstance(fileName);

        Optimization opt = new Optimization(instance1);

        opt.doRelocateExchange();
        boolean sborr = false;
    }

}
