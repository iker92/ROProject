/*
import javafx.util.Pair;

*/
/**
 * Created by andream16 on 13.04.17.
 *//*

public class Algorithm {

    Distances distances;
    core.RouteList routes;

    public Algorithm(Distances distances, core.RouteList routes){
        this.routes = routes;
        this.distances = distances;
    }

    public Pair<core.RouteList, core.RouteList> findBestRelocateAndExchange(Distances distances, core.RouteList routes){

        core.RouteList exchange_then_relocate;
        core.RouteList relocate_then_exchange;

        functions.Exchange _exchange = new functions.Exchange(distances, routes);
        functions.Relocate _relocate = new functions.Relocate(distances, routes);

        exchange_then_relocate = _exchange.exchange(routes);
        exchange_then_relocate = _relocate.relocate(exchange_then_relocate);

        relocate_then_exchange = _relocate.relocate(routes);
        relocate_then_exchange = _exchange.exchange(relocate_then_exchange);

        Pair<core.RouteList, core.RouteList> best_exchanges_and_relocations = new Pair<>(exchange_then_relocate, relocate_then_exchange);

        return best_exchanges_and_relocations;

    }

}
*/
