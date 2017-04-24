package utils;

/**
 * Created by loriz on 4/24/17.
 */
public class ImproperUsageException extends Exception {

    public ImproperUsageException() {
        super();
    }

    public ImproperUsageException(String error) {
        super(error);
    }

}