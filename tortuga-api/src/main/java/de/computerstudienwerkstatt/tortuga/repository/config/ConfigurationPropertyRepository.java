package de.computerstudienwerkstatt.tortuga.repository.config;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import de.computerstudienwerkstatt.tortuga.model.config.ConfigurationProperty;
import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;

/**
 * @author Mischa Holz
 */
@Repository
public interface ConfigurationPropertyRepository extends JpaSpecificationRepository<ConfigurationProperty, String> {

    ConfigurationProperty findOneByLabel(@Param("label") String label);

}
