package de.computerstudienwerkstatt.tortuga.security.token;

import org.springframework.security.core.AuthenticationException;

/**
 * @author Mischa Holz
 */
public class TokenException extends AuthenticationException {

    public TokenException() {
        super("");
    }

    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenException(Throwable cause) {
        super("", cause);
    }

    public TokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super("");
    }
}
