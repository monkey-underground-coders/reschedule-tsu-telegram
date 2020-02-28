package space.delusive.tversu.exception;

/**
 * Is thrown when someone from
 */
public class SoldisWhatTheFuckException extends Exception {
    public SoldisWhatTheFuckException() {
        super();
    }

    public SoldisWhatTheFuckException(String message) {
        super(message);
    }

    public SoldisWhatTheFuckException(String message, Throwable cause) {
        super(message, cause);
    }

    public SoldisWhatTheFuckException(Throwable cause) {
        super(cause);
    }

    protected SoldisWhatTheFuckException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
