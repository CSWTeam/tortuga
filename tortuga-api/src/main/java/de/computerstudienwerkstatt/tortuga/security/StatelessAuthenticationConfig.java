package de.computerstudienwerkstatt.tortuga.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.computerstudienwerkstatt.tortuga.controller.base.advice.RestExceptionHandler;
import de.computerstudienwerkstatt.tortuga.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.ExceptionTranslationFilter;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Mischa Holz
 */
@EnableWebSecurity
@Configuration
@Order(1)
public class StatelessAuthenticationConfig extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(StatelessAuthenticationConfig.class);

    private TokenAuthenticationService tokenAuthenticationService;

    private UserDetailsService userDetailsService;

    private UserRepository userRepository;

    private ObjectMapper objectMapper;

    public StatelessAuthenticationConfig() {
        super(true);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .exceptionHandling().and()
                .anonymous().and()
                .servletApi().and()
                .headers().cacheControl().and().and()
                .authorizeRequests()

                //allow anonymous resource requests
                .antMatchers("/").permitAll()
                .antMatchers("/favicon.ico").permitAll()

                .antMatchers(HttpMethod.POST, "/api/v1/terminal/authenticate").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/terminal/code").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/health/health").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/roomreservations").permitAll()

                .and()

                .addFilterAfter(new StatelessLoginFilter("/api/v1/login", userRepository, tokenAuthenticationService, authenticationManager(), objectMapper), ExceptionTranslationFilter.class)

                .addFilterAfter(new StatelessAuthenticationFilter(tokenAuthenticationService), ExceptionTranslationFilter.class)

                .exceptionHandling().authenticationEntryPoint((request, response, e) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                    RestExceptionHandler.ErrorResponse resp = new RestExceptionHandler.ErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token or no token at all");

                    new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(response.getOutputStream(), resp);
                });
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder()).and().authenticationProvider(new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return authentication;
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return authentication.equals(UserAuthentication.class);
            }
        });
    }

    @Override
    protected UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setTokenAuthenticationService(TokenAuthenticationService tokenAuthenticationService) {
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
