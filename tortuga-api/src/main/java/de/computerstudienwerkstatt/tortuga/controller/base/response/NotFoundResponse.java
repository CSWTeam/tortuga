package de.computerstudienwerkstatt.tortuga.controller.base.response;

import org.springframework.http.HttpStatus;

/**
 * @author Mischa Holz
 */
public class NotFoundResponse extends RestResponse {
    public NotFoundResponse() {
        super(HttpStatus.NOT_FOUND);
    }

    public NotFoundResponse(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public NotFoundResponse(String message, Throwable cause) {
        super(HttpStatus.NOT_FOUND, message, cause);
    }

    public NotFoundResponse(Throwable cause) {
        super(HttpStatus.NOT_FOUND, cause);
    }

    public NotFoundResponse(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(HttpStatus.NOT_FOUND, message, cause, enableSuppression, writableStackTrace);
    }
}
