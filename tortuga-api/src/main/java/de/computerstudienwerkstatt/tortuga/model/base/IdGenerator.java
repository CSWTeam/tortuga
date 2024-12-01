package de.computerstudienwerkstatt.tortuga.model.base;

import java.util.UUID;

/**
 * @author Mischa Holz
 */
public class IdGenerator {

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
