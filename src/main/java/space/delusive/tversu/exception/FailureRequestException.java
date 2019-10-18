package space.delusive.tversu.exception;

public class FailureRequestException extends RuntimeException {
    public FailureRequestException() {
        super();
    }

    public FailureRequestException(String message) {
        super(message);
    }

    public FailureRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailureRequestException(Throwable cause) {
        super(cause);
    }

    protected FailureRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
