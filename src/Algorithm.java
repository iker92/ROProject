import javafx.util.Pair;

/**
 * Created by andream16 on 13.04.17.
 */
public class Algorithm {

    Distances distances;
    ArrayList<Route> routes;

    public Algorithm(Distances distances, ArrayList<Route> routes){
        this.routes = routes;
        this.distances = distances;
    }

    public Pair<ArrayList<Route>, ArrayList<Route>> findBestRelocateAndExchange(Distances distances, ArrayList<Route> routes){

        ArrayList<Route> exchange_then_relocate;
        ArrayList<Route> relocate_then_exchange;

        Exchange _exchange = new Exchange(distances, routes);
        Relocate _relocate = new Relocate(distances, routes);

        exchange_then_relocate = _exchange.exchange(routes);
        exchange_then_relocate = _relocate.relocate(exchange_then_relocate);

        relocate_then_exchange = _relocate.relocate(routes);
        relocate_then_exchange = _exchange.exchange(relocate_then_exchange);

        Pair<ArrayList<Route>, ArrayList<Route>> best_exchanges_and_relocations = new Pair<>(exchange_then_relocate, relocate_then_exchange);

        return best_exchanges_and_relocations;

    }

}
