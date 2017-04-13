import java.util.ArrayList;

/**
 * Created by pippo on 11/04/17.
 */
public class Main {

    public static void main(String [] args) {

        Helper helper = new Helper();

        // The name of the file to open.
        String fileName = "Instances/A1.txt";

        Instance instance1 = helper.fileToInstance(fileName);
        helper.initTSP(instance1);

        for (Node node: instance1.completeTSP){
            System.out.println(node.index);
        }


    }

}
