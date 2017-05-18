package exceptions;

public class ImproperUsageException extends Exception {

    public ImproperUsageException() {
        super();
    }

    public ImproperUsageException(String error) {
        super(error);
    }

}