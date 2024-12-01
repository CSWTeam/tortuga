package de.computerstudienwerkstatt.tortuga.model.major;

import org.hibernate.validator.constraints.NotEmpty;
import de.computerstudienwerkstatt.tortuga.model.base.PersistentEntity;

import javax.persistence.Entity;

/**
 * @author Mischa Holz
 */
@Entity
public class Major extends PersistentEntity {

    @NotEmpty(message = "Der Name des Studiengangs darf nicht leer sein")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
