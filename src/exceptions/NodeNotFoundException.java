package exceptions;

public class NodeNotFoundException extends Exception {

    public NodeNotFoundException() {
        super();
    }

    public NodeNotFoundException(String error) {
        super(error);
    }

}