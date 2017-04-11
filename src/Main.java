import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by pippo on 11/04/17.
 */
public class Main {

    public static void main(String [] args) {

        int numeroNodi=0;
        int numeroRotte=0;
        ArrayList<Integer> tspCompleto=new ArrayList<>();
        ArrayList<Integer> tspLineHaul=new ArrayList<>();
        ArrayList<Integer> tspBackHaul=new ArrayList<>();
        ArrayList<Nodo> nodi=new ArrayList<>();
        // The name of the file to open.
        String fileName = "Instances/A1.txt";

        // This will reference one line at a time
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
                String[] splited = line.split("\\s+");

                switch (i) {
                    case 1:
                        numeroNodi = Integer.valueOf(line);
                        break;
                    case 2:
                        break;
                    case 3:
                        numeroRotte = Integer.valueOf(line);
                        break;
                    case 4: {

                        for (int j = 0; j <=numeroNodi; j++) {
                            tspCompleto.add(Integer.valueOf(splited[j]));
                        }
                        break;

                    }
                    case 5: {
                        for (int j = 0; j <splited.length; j++) {
                            tspLineHaul.add(Integer.valueOf(splited[j]));
                        }
                        break;
                    }
                    case 6: {
                        for (int j = 0; j <splited.length; j++) {
                            tspBackHaul.add(Integer.valueOf(splited[j]));
                        }
                        break;
                    }
                    default:
                        if (i == 7) {
                            Coordinate coordinate=new Coordinate(Integer.valueOf(splited[0]),Integer.valueOf(splited[1]));
                            Nodo nodo= new Nodo(coordinate,"Deposito",Integer.valueOf(splited[3]),false);
                            nodi.add(nodo);
                        }
                        else {
                            Coordinate coordinate = new Coordinate(Integer.valueOf(splited[0]), Integer.valueOf(splited[1]));
                            String tipo = "";
                            if (Integer.valueOf(splited[2]).equals(0)) {
                                tipo = "BackHaul";
                            } else {
                                tipo = "LineHaul";
                            }
                            if (tipo.equals("BackHaul")) {
                                Nodo nodo = new Nodo(coordinate, tipo, Integer.valueOf(splited[3]), false);
                                nodi.add(nodo);
                            } else {
                                Nodo nodo = new Nodo(coordinate, tipo, Integer.valueOf(splited[2]), false);
                                nodi.add(nodo);
                            }
                        }

                        break;
                }



            }
            //System.out.println(numeroNodi + " "+ numeroRotte);
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

        for (Nodo nodo :nodi) {
            System.out.println(nodo.coordinate.x+" "+nodo.coordinate.y+ " "+ nodo.tipoNodo + " "+nodo.peso);
        }

    }
}
