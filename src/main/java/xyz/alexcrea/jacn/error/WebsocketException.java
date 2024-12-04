package xyz.alexcrea.jacn.error;

public class WebsocketException extends Exception {

    public WebsocketException() {
    }

    public WebsocketException(String message) {
        super(message);
    }

    public WebsocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebsocketException(Throwable cause) {
        super(cause);
    }

    public WebsocketException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
