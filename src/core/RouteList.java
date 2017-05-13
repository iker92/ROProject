package core;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by loriz on 4/27/17.
 */
public class RouteList extends ArrayList<Route> implements Route.RouteListener{

    private BigDecimal objectiveFunction = new BigDecimal(0);

    public RouteList(ArrayList<Route> routes) {
        this.addAll(routes);
    }

    public RouteList() {

    }


    ///////////////////////////////////////// INITIALIZATION METHODS ///////////////////////////////////////////////////

    @Override
    public boolean add(Route route) {
        route.setOnRouteChangeListener(this);
        objectiveFunction = objectiveFunction.add(route.getActualDistance());
        return super.add(route);
    }

    @Override
    public boolean remove(Object o) {
        ((Route) o).setOnRouteChangeListener(null);
        objectiveFunction = objectiveFunction.subtract(((Route) o).getActualDistance());
        return super.remove(o);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////// METHODS /////////////////////////////////////////////////////////

    public BigDecimal getObjectiveFunction(){
        return objectiveFunction;
    }


    @Override
    public void OnRouteChange(Route route, BigDecimal oldDistance) {
        objectiveFunction = objectiveFunction.subtract(oldDistance).add(route.getActualDistance());

        boolean fasullo = false;

    }
}
