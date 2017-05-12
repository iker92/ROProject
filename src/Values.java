/**
 * Created by loriz on 4/13/17.
 */
public class Values {

    public enum nodeType {
        BACKHAUL, LINEHAUL, WAREHOUSE
    }

    public static Boolean isDebug() {

        return java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");

    }


}
