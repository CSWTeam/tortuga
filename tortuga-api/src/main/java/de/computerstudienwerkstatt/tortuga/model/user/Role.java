package de.computerstudienwerkstatt.tortuga.model.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Mischa Holz
 */
public enum Role {


    DELETED,
    STUDENT("OP_STUDENT"),
    LECTURER("OP_STUDENT", "OP_LECTURER"),
    CSW_TEAM("OP_STUDENT", "OP_LECTURER", "OP_TEAM"),
    ADMIN("OP_STUDENT", "OP_LECTURER", "OP_TEAM", "OP_ADMIN");

    private String[] privileges;

    Role(String... privileges) {
        this.privileges = privileges;
    }

    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> ret = new HashSet<>();
        ret.add(new SimpleGrantedAuthority("ROLE_" + this.toString()));

        ret.addAll(Arrays.stream(this.privileges).map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

        return ret;
    }
}
