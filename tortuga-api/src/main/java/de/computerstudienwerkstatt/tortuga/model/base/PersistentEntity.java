package de.computerstudienwerkstatt.tortuga.model.base;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author Mischa Holz
 */
@MappedSuperclass
public abstract class PersistentEntity implements Serializable {

    private static final long serialVersionUID = -9103267971270130529L;

    @Id
    @Access(AccessType.FIELD)
    @Column(length = 32)
    @Pattern(regexp = "[0-9a-f]{32}")

    private String id;

    public PersistentEntity() {
        this.id = IdGenerator.generate();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentEntity)) return false;

        PersistentEntity that = (PersistentEntity) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(this.getClass().getSimpleName()).append("(");

        String delim = "";
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            ret.append(delim);
            ret.append(field.getName());
            ret.append("=");
            try {
                Object fieldValue = field.get(this);
                if(fieldValue == null) {
                    ret.append("null");
                } else {
                    ret.append(fieldValue.toString());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            delim = ", ";
        }

        ret.append(")");

        return ret.toString();
    }
}
