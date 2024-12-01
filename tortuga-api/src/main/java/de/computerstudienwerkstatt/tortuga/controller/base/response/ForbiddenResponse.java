package de.computerstudienwerkstatt.tortuga.controller.base.response;

import org.springframework.http.HttpStatus;

/**
 * @author Mischa Holz
 */
public class ForbiddenResponse extends RestResponse {
    public ForbiddenResponse() {
        this("Forbidden");
    }

    public ForbiddenResponse(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
