package de.computerstudienwerkstatt.tortuga.model.cabinet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import de.computerstudienwerkstatt.tortuga.controller.base.response.BadRequestResponse;

/**
 * @author Mischa Holz
 */
public enum Cabinet {

    CABINET_6("Schrank 6"),
    CABINET_7("Schrank 7");

    private String displayName;

    Cabinet(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String displayName() {
        return displayName;
    }

    @JsonCreator
    public static Cabinet fromValue(String val) {
        for (Cabinet cabinet : Cabinet.values()) {
            if(cabinet.displayName().equals(val)) {
                return cabinet;
            }
        }

        throw new BadRequestResponse("Could not map '" + val + "' to any Cabinet");
    }
}
