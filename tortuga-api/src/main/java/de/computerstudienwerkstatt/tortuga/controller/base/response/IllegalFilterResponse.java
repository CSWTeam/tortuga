package de.computerstudienwerkstatt.tortuga.controller.base.response;

import org.springframework.http.HttpStatus;

/**
 * @author Mischa Holz
 */
public class IllegalFilterResponse extends RestResponse {

    private String type;

    private String field;

    public IllegalFilterResponse(String type, String field) {
        super(HttpStatus.BAD_REQUEST, type + " does not have a field named " + field);

        this.type = type;
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public String getField() {
        return field;
    }
}
