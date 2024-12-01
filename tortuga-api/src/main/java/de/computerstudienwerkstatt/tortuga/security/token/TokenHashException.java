package de.computerstudienwerkstatt.tortuga.security.token;

/**
 * @author Mischa Holz
 */
public class TokenHashException extends TokenException {

    public TokenHashException() {
    }

    public TokenHashException(String message) {
        super(message);
    }

    public TokenHashException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenHashException(Throwable cause) {
        super(cause);
    }

    public TokenHashException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
