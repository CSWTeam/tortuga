package de.computerstudienwerkstatt.tortuga.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import de.computerstudienwerkstatt.tortuga.model.user.PossessedEntity;

import java.io.Serializable;

/**
 * @author Mischa Holz
 */
@Service
public class PossessedEntityPermissionElevator {

    public <T extends PossessedEntity, ID extends Serializable> boolean checkOwner(JpaRepository<T, ID> repository, ID objectId, String ownerId) {
        T object = repository.findOne(objectId);
        if(object == null) {
            return false;
        }

        return object.getUser() != null && object.getUser().getId().equals(ownerId);
    }
}
