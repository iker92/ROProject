import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by loriz on 4/13/17.
 */
public class Helper {

    public Instance fileToInstance(String fileName) {

        Instance instance = new Instance();

        String line = null;
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);
            int i=0;

            while((line = bufferedReader.readLine()) != null) {
                i++;
                String[] splitted = line.split("\\s+");

                switch (i) {
                    case 1:
                        instance.nodeCount = Integer.valueOf(line);
                        break;
                    case 2:
                        break;
                    case 3:
                        instance.routeCount = Integer.valueOf(line);
                        break;
                    case 4: {

                        for (int j = 0; j <=instance.nodeCount; j++) {
                            instance.indexesCompleteTSP.add(Integer.valueOf(splitted[j]));
                        }
                        break;

                    }
                    case 5: {
                        for (int j = 0; j <splitted.length; j++) {
                            instance.indexesLineHaulTSP.add(Integer.valueOf(splitted[j]));
                        }
                        break;
                    }
                    case 6: {
                        for (int j = 0; j <splitted.length; j++) {
                            instance.indexesBackHaulTSP.add(Integer.valueOf(splitted[j]));
                        }
                        break;
                    }
                    default:
                        if (i == 7) {
                            Coordinates coordinates =new Coordinates(Integer.valueOf(splitted[0]),Integer.valueOf(splitted[1]));
                            Node node = new Node(coordinates,Values.nodeType.WAREHOUSE ,Integer.valueOf(splitted[3]),false,i-7);
                            instance.nodesList.add(node);
                        }
                        else {
                            Coordinates coordinates = new Coordinates(Integer.valueOf(splitted[0]), Integer.valueOf(splitted[1]));
                            Values.nodeType nodeType;
                            if (Integer.valueOf(splitted[2]).equals(0)) {
                                nodeType = Values.nodeType.BACKHAUL;
                            } else {
                                nodeType = Values.nodeType.LINEHAUL;
                            }
                            if (nodeType == Values.nodeType.BACKHAUL) {
                                Node node = new Node(coordinates, nodeType, Integer.valueOf(splitted[3]), false,i-7);
                                instance.nodesList.add(node);
                            } else {
                                Node node = new Node(coordinates, nodeType, Integer.valueOf(splitted[2]), false,i-7);
                                instance.nodesList.add(node);
                            }
                        }

                        break;
                }



            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }



        return instance;

    }

    public void initTSP(Instance instance){
        instance.completeTSP=instance.createTSPFromNodes(instance.nodesList,instance.indexesCompleteTSP);
        instance.backHaulTSP=instance.createTSPFromNodes(instance.nodesList,instance.indexesBackHaulTSP);
        instance.lineHaulTSP=instance.createTSPFromNodes(instance.nodesList,instance.indexesLineHaulTSP);
    }
}
