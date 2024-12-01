package de.computerstudienwerkstatt.tortuga.security;

import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.repository.user.UserRepository;
import de.computerstudienwerkstatt.tortuga.security.token.Token;
import de.computerstudienwerkstatt.tortuga.security.token.TokenHandler;
import de.computerstudienwerkstatt.tortuga.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Service
public class TokenAuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationService.class);

    private static final String COOKIE_NAME = "auth_token";

    private static final String SECRET_KEY_LABEL = "st.ilu.rms4csw.security.token";

    private TokenHandler tokenHandler;

    private long longValidFor;

    private long shortValidFor;

    private UserRepository userRepository;

    @Autowired
    public TokenAuthenticationService(ConfigurationService configurationService, @Value("${token.longValidFor}") Long longValidFor, @Value("${token.shortValidFor}") Long shortValidFor) {
        Optional<String> secretKey = configurationService.getValue(SECRET_KEY_LABEL);
        if(secretKey.isPresent()) {
            logger.info("Using saved secret");

            byte[] secret = Base64.getDecoder().decode(secretKey.get());

            this.tokenHandler = new TokenHandler(secret);
        } else {
            logger.info("Generating new secret");

            SecureRandom secureRandom = new SecureRandom();
            byte[] secretBytes = new byte[4096];

            secureRandom.nextBytes(secretBytes);

            String persistKey = Base64.getEncoder().encodeToString(secretBytes);

            configurationService.persistOption(SECRET_KEY_LABEL, persistKey);

            this.tokenHandler = new TokenHandler(secretBytes);
        }

        this.longValidFor = longValidFor;
        this.shortValidFor = shortValidFor;
    }

    public Token addAuthentication(HttpServletResponse response, User user, boolean longToken, Optional<Token> oldToken) {
        Token token;

        if(oldToken.isPresent()) {
            token = tokenHandler.createTokenForUser(user, oldToken.get().getValidFor());
        } else {
            if(longToken) {
                token = tokenHandler.createTokenForUser(user, longValidFor);
            } else {
                token = tokenHandler.createTokenForUser(user, shortValidFor);
            }
        }

        String strToken = tokenHandler.signToken(token);
        try {
            strToken = URLEncoder.encode(strToken, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }

        Cookie cookie = new Cookie(COOKIE_NAME, strToken);
        cookie.setPath("/");
        cookie.setMaxAge(14 * 24 * 60 * 60);

        response.addCookie(cookie);

        response.setHeader("X-Next-Auth-Token", strToken);

        return token;
    }

    public Optional<UserAuthentication> getAuthentication(HttpServletRequest request) {
        String strToken = Arrays
                .stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .map(v -> {
                    try {
                        return URLDecoder.decode(v, "UTF-8");
                    } catch(UnsupportedEncodingException e) {
                        throw new AssertionError(e);
                    }
                })
                .findAny()
                .orElseGet(() -> {
                    String value = request.getHeader(HttpHeaders.AUTHORIZATION);
                    if(value == null || value.isEmpty()) {
                        return null;
                    }
                    return value;
                });
        if(strToken == null) {
            return Optional.empty();
        }

        Token token = tokenHandler.validateToken(strToken);

        User user = userRepository.findOne(token.getId());
        if(user == null) {
            return Optional.empty();
        }

        if(user.isActiveUser()) {
            return Optional.of(new UserAuthentication(user, token));
        }

        return Optional.empty();
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
