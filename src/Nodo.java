/**
 * Created by pippo on 11/04/17.
 */
public class Nodo {

    public Coordinate coordinate;
    public String tipoNodo;
    public int peso;
    public boolean preso;

    public Nodo(Coordinate coordinate,String tipoNodo, int peso,boolean preso){
        this.coordinate=coordinate;
        this.tipoNodo=tipoNodo;
        this.peso=peso;
        this.preso=preso;
    }

}
