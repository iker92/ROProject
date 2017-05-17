package core;

import java.math.BigDecimal;

/**
 * ResultData is essentially the POJO which helps to model the data calculated for each iteration into the final result.
 * In fact, this class holds all the information to be shown for each route inside the result output file
 */
public class ResultData {

    private final BigDecimal cost;
    private final int weightLinehaul;
    private final int weightBackhaul;
    private final String route;
    private final BigDecimal totalOF;

    public ResultData(BigDecimal cost, int weightLinehaul, int weightBackhaul, String route, BigDecimal totalOF) {

        this.cost = cost;
        this.weightLinehaul = weightLinehaul;
        this.weightBackhaul = weightBackhaul;
        this.route = route;
        this.totalOF = totalOF;
    }

    public BigDecimal getRouteCost() { return cost; }

    public int getWeightLH() { return weightLinehaul; }

    public int getWeightBH() { return weightBackhaul; }

    public String getRouteAsString() { return route; }

    public BigDecimal getTotalObjectiveFunction() { return totalOF; }
}