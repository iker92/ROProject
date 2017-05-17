package core;

/**
 * Values is a central class which holds some informations which are part of the core of all the program and,
 * by consequence, used everywhere.
 */
public class Values {

    /**
     * ROUTES_IN_DEBUG toggles the printing of the updated Routes for every successful relocation/exchange when in Debug Mode
     */
    private static final Boolean ROUTES_IN_DEBUG = true;


    /**
     * nodeType is an enum used to statically assign a Type to every Node, giving a user a pool of three choices to choose from
     */
    public enum nodeType {
        BACKHAUL, LINEHAUL, WAREHOUSE
    }


    /**
     * isDebug(), as the name says, is a little method which is able to tell if the program was launched in Debug mode
     * or Release mode. Each mode presents a different verbosity in console but the same output.
     * @return
     */
    public static Boolean isDebug() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
    }


    /**
     * printRoutesInDebug() let the program show the Routes for every step if desired
     * @return
     */
    public static Boolean printRoutesInDebug() {
        return ROUTES_IN_DEBUG && isDebug();
    }


}
