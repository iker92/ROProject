package exceptions;

/**
 * Created by loriz on 4/24/17.
 */
public class SwapFailedException extends Exception {

    public SwapFailedException() {
            super();
        }

    public SwapFailedException(String error) {
            super(error);
        }
}
