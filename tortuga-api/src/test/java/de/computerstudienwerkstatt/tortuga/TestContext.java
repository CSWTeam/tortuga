package de.computerstudienwerkstatt.tortuga;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import de.computerstudienwerkstatt.tortuga.service.SpringSecurityLoggedInUserHolder;

/**
 * @author Mischa Holz
 */
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SpringSecurityLoggedInUserHolder.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Main.class)
})
public class TestContext {
}
