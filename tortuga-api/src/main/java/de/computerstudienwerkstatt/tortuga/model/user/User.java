package de.computerstudienwerkstatt.tortuga.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.computerstudienwerkstatt.tortuga.model.major.Major;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import de.computerstudienwerkstatt.tortuga.controller.base.response.BadRequestResponse;
import de.computerstudienwerkstatt.tortuga.model.base.PersistentEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Entity(name = "rms_user")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@StudentsHaveToHaveAMajor
@StudentsHaveToHaveAStudentId
@UsersHaveUniqueEmails
@UsersHaveUniqueLoginNames
public class User extends PersistentEntity {

    @Column(unique = true)
    @NotEmpty(message = "Benutzer brauchen einen Anmeldenamen")
    private String loginName;

    @NotEmpty(message = "Benutzer brauchen einen Vornamen")
    private String firstName;

    @NotEmpty(message = "Benutzer brauchen einen Nachnamen")
    private String lastName;

    @NotEmpty(message = "Benutzer brauchen eine Email")
    @Email
    @Column(unique = true)
    private String email;

    @Access(AccessType.FIELD)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToOne
    @Access(AccessType.FIELD)
    private Major major;

    @Access(AccessType.FIELD)
    private String studentId;

    @NotEmpty(message = "Benutzer brauchen eine Telefonnummer")
    private String phoneNumber;

    @NotNull(message = "Benutzer brauchen ein Passwort")
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Jeder Benutzer braucht eine Rolle")
    private Role role;

    @Access(AccessType.FIELD)
    private Date expirationDate;

    @NotNull
    private Boolean enabled;

    @Access(AccessType.FIELD)
    @Column(unique = true)
    private String passcode;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName.toLowerCase();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Optional<Gender> getGender() {
        if(this.gender == null) {
            return Optional.empty();
        }

        return Optional.of(this.gender);
    }

    public void setGender(Optional<Gender> gender) {
        if(gender.isPresent()) {
            this.gender = gender.get();
        } else {
            this.gender = null;
        }
    }

    public Optional<Major> getMajor() {
        return Optional.ofNullable(this.major);
    }

    public void setMajor(Optional<Major> major) {
        if(major.isPresent()) {
            this.major = major.get();
        } else {
            this.major = null;
        }
    }

    public Optional<String> getStudentId() {
        return Optional.ofNullable(this.studentId);
    }

    public void setStudentId(Optional<String> studentId) {
        this.studentId = studentId.orElse(null);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        if(password.length() < 6) {
            throw new BadRequestResponse("Passwörter müssen mindestens 6 Zeichen lang sein");
        }

        this.password = new BCryptPasswordEncoder().encode(password);
    }

    @JsonIgnore
    public Optional<String> getPasscode() {
        return Optional.ofNullable(passcode);
    }

    @JsonProperty
    public void setPasscode(Optional<String> passcode) {
        this.passcode = passcode.map(p -> new BCryptPasswordEncoder().encode(p)).orElse(null);
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Optional<Date> getExpirationDate() {
        if(expirationDate == null) {
            return Optional.empty();
        }

        return Optional.of(expirationDate);
    }

    public void setExpirationDate(Optional<Date> expirationDate) {
        if(expirationDate.isPresent()) {
            this.expirationDate = expirationDate.get();
        } else {
            this.expirationDate = null;
        }
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @JsonIgnore
    public boolean isActiveUser() {
        boolean date = true;
        if(getExpirationDate().isPresent()) {
            date = getExpirationDate().get().after(new Date());
        }

        return isEnabled() && date;
    }

    public static Date calculateNextSemesterEnd(Date from) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(from);

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        if(calendar.get(Calendar.MONTH) < Calendar.APRIL) {
            calendar.set(Calendar.MONTH, Calendar.APRIL);
        } else if(calendar.get(Calendar.MONTH) < Calendar.OCTOBER) {
            calendar.set(Calendar.MONTH, Calendar.OCTOBER);
        } else {
            int year = calendar.get(Calendar.YEAR);
            year++;
            calendar.set(Calendar.YEAR, year);

            calendar.set(Calendar.MONTH, Calendar.APRIL);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
