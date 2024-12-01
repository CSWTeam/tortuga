package de.computerstudienwerkstatt.tortuga.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mischa Holz
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Module jdk8Module() {
        return new Jdk8Module();
    }

}
