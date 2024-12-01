package de.computerstudienwerkstatt.tortuga.security.token;

/**
 * @author Mischa Holz
 */
public class TokenNotPresentException extends TokenException {
    public TokenNotPresentException() {
    }

    public TokenNotPresentException(String message) {
        super(message);
    }

    public TokenNotPresentException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenNotPresentException(Throwable cause) {
        super(cause);
    }

    public TokenNotPresentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
