package de.computerstudienwerkstatt.tortuga.config;

import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Mischa Holz
 */
@Configuration
public class DataSourceConfig {


    @Autowired
    private Environment environment;

    @Bean
    public SimpleDriverDataSource dataSource() throws URISyntaxException {
        String url = System.getenv("DATABASE_URL");
        if(url == null) {
            // default. if you need a different configuration for development just
            // set the environment variable in your runner using the same format as
            // below
            if(environment.acceptsProfiles("TEST")) {
                url = "postgres://postgres:password@localhost:5432/rms4csw_test";
            } else {
                url = "postgres://postgres:password@localhost:5432/rms4csw";
            }
        }

        URI dbUri = new URI(url);

        String username = dbUri.getUserInfo().split(":")[0];
        String password = null;
        if(dbUri.getUserInfo().split(":").length == 2) {
            password = dbUri.getUserInfo().split(":")[1];
        }
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

        SimpleDriverDataSource driverDataSource = new SimpleDriverDataSource();
        driverDataSource.setUrl(dbUrl);
        driverDataSource.setUsername(username);
        driverDataSource.setPassword(password);
        driverDataSource.setDriverClass(Driver.class);

        return driverDataSource;
    }

}
