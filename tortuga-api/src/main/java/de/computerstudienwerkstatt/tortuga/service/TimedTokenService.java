package de.computerstudienwerkstatt.tortuga.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Mischa Holz
 */
@Service
public class TimedTokenService {

    private final long seed = new SecureRandom().nextLong();

    public long getCurrentToken() {
        return calculateToken(System.currentTimeMillis());
    }

    public long getPreviousToken() {
        return calculateToken(System.currentTimeMillis() - (30 * 1000));
    }

    private long calculateToken(long time) {
        long currentInterval = time / (30 * 1000);

        Random random = new Random();
        random.setSeed(seed ^ currentInterval);

        return random.nextInt(999_999 - 100_000) + 100_000;
    }

    public boolean isValidToken(long token) {
        return token == getCurrentToken() || token == getPreviousToken();
    }

}
