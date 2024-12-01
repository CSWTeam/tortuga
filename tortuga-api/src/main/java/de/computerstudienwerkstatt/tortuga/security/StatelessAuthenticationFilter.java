package de.computerstudienwerkstatt.tortuga.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import de.computerstudienwerkstatt.tortuga.model.user.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
public class StatelessAuthenticationFilter extends GenericFilterBean {

    private TokenAuthenticationService tokenAuthenticationService;

    public StatelessAuthenticationFilter(TokenAuthenticationService tokenAuthenticationService) {
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Optional<UserAuthentication> authentication = tokenAuthenticationService.getAuthentication((HttpServletRequest) servletRequest);
        if(authentication.isPresent()) {
            tokenAuthenticationService.addAuthentication(
                    (HttpServletResponse) servletResponse,
                    (User) authentication.get().getDetails(),
                    false,
                    authentication.map(UserAuthentication::getToken)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication.get());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
