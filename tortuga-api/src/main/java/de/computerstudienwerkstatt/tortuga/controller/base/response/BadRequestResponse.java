package de.computerstudienwerkstatt.tortuga.controller.base.response;

import org.springframework.http.HttpStatus;

/**
 * @author Mischa Holz
 */
public class BadRequestResponse extends RestResponse {
    public BadRequestResponse() {
        this("Bad Request");
    }

    public BadRequestResponse(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
