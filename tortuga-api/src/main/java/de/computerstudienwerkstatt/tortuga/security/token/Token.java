package de.computerstudienwerkstatt.tortuga.security.token;

import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.model.user.Role;

import java.util.Date;

/**
 * @author Mischa Holz
 */
public class Token {

    private String loginName;

    private String id;

    private Role role;

    private User user;

    private Date issuedAt;

    private long validFor;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public long getValidFor() {
        return validFor;
    }

    public void setValidFor(long validFor) {
        this.validFor = validFor;
    }
}
