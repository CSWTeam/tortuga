package de.computerstudienwerkstatt.tortuga.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.repository.user.UserRepository;
import de.computerstudienwerkstatt.tortuga.security.json.LoginRequest;
import de.computerstudienwerkstatt.tortuga.security.token.Token;
import de.computerstudienwerkstatt.tortuga.util.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import de.computerstudienwerkstatt.tortuga.controller.base.advice.RestExceptionHandler;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
public class StatelessLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final static Logger logger = LoggerFactory.getLogger(StatelessLoginFilter.class);

    private UserRepository userRepository;

    private TokenAuthenticationService tokenAuthenticationService;

    private ObjectMapper objectMapper;

    public StatelessLoginFilter(String urlMapping, UserRepository userRepository, TokenAuthenticationService tokenAuthenticationService, AuthenticationManager authManager, ObjectMapper objectMapper) {
        super(new AntPathRequestMatcher(urlMapping));

        this.userRepository = userRepository;
        this.tokenAuthenticationService = tokenAuthenticationService;

        this.objectMapper = objectMapper;

        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        if(!HttpMethod.POST.toString().equals(httpServletRequest.getMethod())) {
            httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            httpServletResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());

            RestExceptionHandler.ErrorResponse errorResponse = new RestExceptionHandler.ErrorResponse(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You need to post login information");
            objectMapper.writeValue(httpServletResponse.getOutputStream(), errorResponse);

            return null;
        }

        try {
            Thread.sleep(400);
        } catch(InterruptedException ignored) {
        }

        LoginRequest loginRequest = objectMapper.readValue(httpServletRequest.getInputStream(), LoginRequest.class);

        UsernamePasswordAuthenticationToken authResult = new UsernamePasswordAuthenticationToken(loginRequest.getLoginName(), loginRequest.getPassword());

        authResult.setDetails(loginRequest);

        return getAuthenticationManager().authenticate(authResult);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        boolean longToken;
        if(authResult.getDetails() instanceof LoginRequest) {
            longToken = (!NetworkUtil.isLocalNetworkRequest(request)) && ((LoginRequest) authResult.getDetails()).getLongToken();
        } else {
            throw new AssertionError("Couldn't get the LoginRequest");
        }

        User user = userRepository.findOneByLoginName(authResult.getName());

        Token token = tokenAuthenticationService.addAuthentication(response, user, longToken, Optional.empty());

        UserAuthentication userAuthentication = new UserAuthentication(user, token);

        SecurityContextHolder.getContext().setAuthentication(userAuthentication);

        logger.info("LOGIN: Login successful");

        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), user);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);

        logger.info("LOGIN: Login failed");
    }
}
