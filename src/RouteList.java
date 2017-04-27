import java.util.ArrayList;

/**
 * Created by loriz on 4/27/17.
 */
public class RouteList extends ArrayList<Route> implements Route.RouteListener{

    private double objectiveFunction = 0.0;

    public RouteList(ArrayList<Route> routes) {
        for (Route r  : routes){
            add(r);
        }
    }


    ///////////////////////////////////////// INITIALIZATION METHODS ///////////////////////////////////////////////////

    @Override
    public boolean add(Route route) {
        route.setOnRouteChangeListener(this);
        objectiveFunction += route.getActualDistance();
        return super.add(route);
    }

    @Override
    public boolean remove(Object o) {
        ((Route) o).setOnRouteChangeListener(null);
        objectiveFunction -= ((Route) o).getActualDistance();
        return super.remove(o);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////// METHODS /////////////////////////////////////////////////////////

    public double getObjectiveFunction(){
        return objectiveFunction;
    }


    //You can use this method to check whether a new calculated objective function is minimized.
    public boolean isItMinimized(double objectFun){
        return objectFun > objectiveFunction;
    }


    @Override
    public void OnRouteChange(Route route, double oldDistance) {
        objectiveFunction = (objectiveFunction - oldDistance) + route.getActualDistance();
    }
}
