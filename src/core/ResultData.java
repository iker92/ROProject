package core;

import java.math.BigDecimal;

/**
 * Created by loriz on 5/15/17.
 */
public class ResultData {

    public final BigDecimal cost;
    public final int weightLinehaul;
    public final int weightBackhaul;
    public final String route;
    public final BigDecimal totalOF;

    public ResultData(BigDecimal cost, int weightLinehaul, int weightBackhaul, String route, BigDecimal totalOF) {

        this.cost = cost;
        this.weightLinehaul = weightLinehaul;
        this.weightBackhaul = weightBackhaul;
        this.route = route;
        this.totalOF = totalOF;
    }

}