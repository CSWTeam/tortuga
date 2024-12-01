package de.computerstudienwerkstatt.tortuga.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.computerstudienwerkstatt.tortuga.model.config.ConfigurationProperty;
import de.computerstudienwerkstatt.tortuga.repository.config.ConfigurationPropertyRepository;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Service
public class ConfigurationService {

    private ConfigurationPropertyRepository configurationPropertyRepository;


    public Optional<List<String>> getValues(@NotNull String label) {
        ConfigurationProperty property = configurationPropertyRepository.findOneByLabel(label);
        if(property == null) {
            return Optional.empty();
        }

        return Optional.of(property.getValues());
    }

    public Optional<String> getValue(@NotNull String label) {
        return getValues(label).map(ret -> ret.get(0));
    }

    public void persistOption(@NotNull String label, @NotNull List<String> values) {
        ConfigurationProperty property = configurationPropertyRepository.findOneByLabel(label);
        if(property != null) {
            if(values.size() == 0) {
                configurationPropertyRepository.delete(property);
                return;
            }

            property.setValues(values);

            configurationPropertyRepository.save(property);
            return;
        }

        if(values.size() == 0) {
            return;
        }

        property = new ConfigurationProperty();
        property.setLabel(label);
        property.setValues(values);

        configurationPropertyRepository.save(property);
    }

    public void persistOption(@NotNull String label, @NotNull String value) {
        persistOption(label, Collections.singletonList(value));
    }



    @Autowired
    public void setConfigurationPropertyRepository(ConfigurationPropertyRepository configurationPropertyRepository) {
        this.configurationPropertyRepository = configurationPropertyRepository;
    }
}
