package de.computerstudienwerkstatt.tortuga.model.device;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.NotEmpty;
import de.computerstudienwerkstatt.tortuga.model.base.PersistentEntity;
import de.computerstudienwerkstatt.tortuga.model.cabinet.Cabinet;
import de.computerstudienwerkstatt.tortuga.model.devicecategory.DeviceCategory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class Device extends PersistentEntity {

    @NotEmpty(message = "Der Gerätename darf nicht leer sein.")
    private String name;

    @OneToOne
    @NotNull(message = "Geräte brauchen eine Gerätekategorie")
    private DeviceCategory category;

    private String description;

    private String accessories;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Geräte müssen in einem der Schränke gelagert sein")
    private Cabinet cabinet;

    @NotEmpty(message = "Jedes Gerät braucht eine Inventarnummer")
    @NotNull(message = "Jedes Gerät braucht eine Inventarnummer")
    private String inventoryNumber;

    @Access(AccessType.FIELD)
    private Date acquisitionDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceCategory getCategory() {
        return category;
    }

    public void setCategory(DeviceCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccessories() {
        return accessories;
    }

    public void setAccessories(String accessories) {
        this.accessories = accessories;
    }

    public Cabinet getCabinet() {
        return cabinet;
    }

    public void setCabinet(Cabinet cabinet) {
        this.cabinet = cabinet;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public Optional<Date> getAcquisitionDate() {
        return Optional.ofNullable(acquisitionDate);
    }

    public void setAcquisitionDate(Optional<Date> acquisitionDate) {
        this.acquisitionDate = acquisitionDate.orElse(null);
    }
}
