package space.delusive.tversu.exception;

public class NoSuchButtonException extends RuntimeException {
    public NoSuchButtonException() {
        super();
    }

    public NoSuchButtonException(String message) {
        super(message);
    }

    public NoSuchButtonException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchButtonException(Throwable cause) {
        super(cause);
    }

}
