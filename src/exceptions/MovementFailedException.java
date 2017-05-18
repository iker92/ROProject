package exceptions;

/**
 * Created by loriz on 4/24/17.
 */
public class MovementFailedException extends Exception {

    public MovementFailedException() {
            super();
        }

    public MovementFailedException(String error) {
            super(error);
        }
}
