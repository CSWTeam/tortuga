package de.computerstudienwerkstatt.tortuga.security.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.computerstudienwerkstatt.tortuga.model.user.User;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

/**
 * @author Mischa Holz
 */
public class TokenHandler {

    private static final Logger logger = LoggerFactory.getLogger(TokenHandler.class);

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String SEPARATOR = ".";
    private static final String SEPARATOR_SPLITTER = "\\.";

    private ObjectMapper objectMapper;


    private Mac mac;

    public TokenHandler(byte[] secret) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new Jdk8Module());

        try {
            mac = Mac.getInstance(HMAC_ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            mac.init(new SecretKeySpec(secret, HMAC_ALGO));
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public Token createTokenForUser(User user, long validFor) {
        Token token = new Token();
        token.setLoginName(user.getLoginName());
        token.setId(user.getId());
        token.setRole(user.getRole());
        token.setUser(user);

        token.setIssuedAt(new Date());
        token.setValidFor(validFor);

        return token;
    }

    public String signToken(Token token) {
        byte[] userBytes = toJson(token);
        byte[] hash = createHmac(userBytes);

        return toBase64(userBytes) + SEPARATOR + toBase64(hash);
    }

    public Token validateToken(String strToken) {
        String[] parts = strToken.split(SEPARATOR_SPLITTER);
        if(parts.length != 2 || parts[0].length() <= 0 || parts[1].length() <= 0) {
            logger.info("Invalid token");
            throw new TokenFormatException("Could not parse the token. It consists of 2 parts separated by " + SEPARATOR);
        }

        byte[] userBytes = fromBase64(parts[0]);
        byte[] hash = fromBase64(parts[1]);

        if(validateHash(userBytes, hash)) {
            try {
                Token token = objectMapper.readValue(userBytes, Token.class);

                if(new Date().before(new Date(token.getIssuedAt().getTime() + token.getValidFor()))) {
                    return token;
                }

                logger.info("Expired token! Was issued at {} and valid for {}", token.getIssuedAt(), token.getValidFor());
                throw new TokenExpiredException("This token is already expired");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        logger.info("Invalid token hash");
        throw new TokenHashException("Invalid hash");
    }

    private byte[] toJson(Token token) {
        try {
            return objectMapper.writeValueAsBytes(token);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized byte[] createHmac(byte[] content) {
        return mac.doFinal(content);
    }

    private boolean validateHash(byte[] userBytes, byte[] hash) {
        return Arrays.equals(createHmac(userBytes), hash);
    }

    private String toBase64(byte[] content) {
        return Base64.getUrlEncoder().encodeToString(content);
    }

    private byte[] fromBase64(String content) {
        return Base64.getUrlDecoder().decode(content);
    }

}
