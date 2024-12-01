package de.computerstudienwerkstatt.tortuga.security.json;

/**
 * @author Mischa Holz
 */
public class LoginRequest {

    private String loginName;

    private String password;

    private Boolean longToken = false;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName.toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getLongToken() {
        return longToken;
    }

    public void setLongToken(Boolean longToken) {
        this.longToken = longToken;
    }
}
