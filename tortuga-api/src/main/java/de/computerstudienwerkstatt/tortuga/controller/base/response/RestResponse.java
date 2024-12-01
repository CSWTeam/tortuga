package de.computerstudienwerkstatt.tortuga.controller.base.response;

import org.springframework.http.HttpStatus;

/**
 * @author Mischa Holz
 */
public class RestResponse extends RuntimeException {

    private HttpStatus status;

    public RestResponse(HttpStatus status) {
        this.status = status;
    }

    public RestResponse(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public RestResponse(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public RestResponse(HttpStatus status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public RestResponse(HttpStatus status, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
