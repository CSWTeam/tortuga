package de.computerstudienwerkstatt.tortuga.model.config;

import de.computerstudienwerkstatt.tortuga.model.base.PersistentEntity;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mischa Holz
 */
@Entity
public class ConfigurationProperty extends PersistentEntity {

    @Column(unique = true)
    private String label;

    @Size(min = 1)
    @NotNull
    @ElementCollection
    @Column(columnDefinition = "TEXT")
    private List<String> values = new ArrayList<>();

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
