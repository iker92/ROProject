package core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * RouteList is a special implementation of ArrayList focused only towards keeping track of all the Routes.
 * As an enhanced container, some methods are extended (such as the .add() and .remove()) and some new functions added
 * (such as automatically updating the global objective function when any route gets modified) by implementing the RouteListener interface.
 */
public class RouteList extends ArrayList<Route> implements Route.RouteListener{

    private BigDecimal objectiveFunction = new BigDecimal(0);


    ///////////////////////////////////////// INITIALIZATION METHODS ///////////////////////////////////////////////////

    @Override
    public boolean addAll(Collection<? extends Route> collection) {

        for (Route route : collection) {
            this.add(route);
        }
        return true;
    }


    /**
     * add(Route) method was Overridden to add our OnRouteChangeListener to every added route, thus letting the program keep
     * track automatically of every variation of the added route.
     * @param route is the route to add to the RouteList
     * @return continues the normal add(T) behaviour
     */
    @Override
    public boolean add(Route route) {
        route.setOnRouteChangeListener(this);
        objectiveFunction = objectiveFunction.add(route.getActualDistance());
        return super.add(route);
    }


    /**
     * .remove(Route) method was Overridden to remove any attached OnRouteChengeListener from the Route upon its removal
     * from the RouteList.
     * @param o is the Object to be cast to Route to remove from the RouteList
     * @return continues with the normal remove(T) behaviour
     */
    @Override
    public boolean remove(Object o) {
        ((Route) o).setOnRouteChangeListener(null);
        objectiveFunction = objectiveFunction.subtract(((Route) o).getActualDistance());
        return super.remove(o);
    }

    
    ////////////////////////////////////////////////// METHODS /////////////////////////////////////////////////////////


    /**
     * OnRouteChange(...) is Overridden from the implementation of the same method from Route.RouteListener.
     * This method bonds together the instance of Route to the instance of RouteList. By doing this, the program automatically
     * recalculates and updates the global objective function upon variation of the instance of Route.
     * @param route is the Route to bind to this RouteList
     * @param oldDistance is the cost (or distance/objective function) of this Route before any variation (used to update the RouteList's)
     */
    @Override
    public void OnRouteChange(Route route, BigDecimal oldDistance) {
        objectiveFunction = objectiveFunction.subtract(oldDistance).add(route.getActualDistance());

    }

    public BigDecimal getObjectiveFunction(){
        return objectiveFunction;
    }
}
