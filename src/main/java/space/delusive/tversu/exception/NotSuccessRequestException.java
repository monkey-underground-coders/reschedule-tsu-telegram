package space.delusive.tversu.exception;

public class NotSuccessRequestException extends RuntimeException {
    public NotSuccessRequestException() {
        super();
    }

    public NotSuccessRequestException(String message) {
        super(message);
    }

    public NotSuccessRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSuccessRequestException(Throwable cause) {
        super(cause);
    }

    protected NotSuccessRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
