package de.computerstudienwerkstatt.tortuga.security.token;

/**
 * @author Mischa Holz
 */
public class TokenFormatException extends TokenException {
    public TokenFormatException() {
    }

    public TokenFormatException(String message) {
        super(message);
    }

    public TokenFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenFormatException(Throwable cause) {
        super(cause);
    }

    public TokenFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
