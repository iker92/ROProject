package exceptions;

/**
 * Created by loriz on 4/20/17.
 */
public class NodeNotFoundException extends Exception {

    public NodeNotFoundException() {
        super();
    }

    public NodeNotFoundException(String error) {
        super(error);
    }

}