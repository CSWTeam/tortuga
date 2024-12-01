package de.computerstudienwerkstatt.tortuga.model.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.computerstudienwerkstatt.tortuga.model.base.PersistentEntity;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;

/**
 * @author Mischa Holz
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ComplaintTemplate extends PersistentEntity {

    @NotEmpty(message = "Beschwerdevorlagen brauchen einen Text")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
