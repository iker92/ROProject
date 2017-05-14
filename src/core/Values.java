package core;

/**
 * Created by loriz on 4/13/17.
 */
public class Values {

    private static final Boolean ROUTES_IN_DEBUG = true;

    public enum nodeType {
        BACKHAUL, LINEHAUL, WAREHOUSE
    }

    public static Boolean isDebug() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");

    }

    public static Boolean printRoutesInDebug() {
        return ROUTES_IN_DEBUG && isDebug();
    }


}
