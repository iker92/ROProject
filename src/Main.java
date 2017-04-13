/**
 * Created by pippo on 11/04/17.
 */
public class Main {

    public static void main(String [] args) {

        Helper helper = new Helper();

        // The name of the file to open.
        String fileName = "Instances/A1.txt";

        Instance instance1 = helper.fileToInstance(fileName);

        for (Node node : instance1.nodesList) {
            System.out.println(node.coordinates.x+" "+ node.coordinates.y+ " "+ node.nodeType + " "+ node.weight);
        }

    }
}
