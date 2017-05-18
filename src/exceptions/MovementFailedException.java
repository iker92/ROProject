package exceptions;

public class MovementFailedException extends Exception {

    public MovementFailedException() {
            super();
        }

    public MovementFailedException(String error) {
            super(error);
        }
}
