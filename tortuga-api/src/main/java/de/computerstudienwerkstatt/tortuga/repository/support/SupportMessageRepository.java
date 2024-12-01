package de.computerstudienwerkstatt.tortuga.repository.support;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import de.computerstudienwerkstatt.tortuga.model.support.SupportMessage;
import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;

import java.util.List;

/**
 * @author Mischa Holz
 */
@Repository
public interface SupportMessageRepository extends JpaSpecificationRepository<SupportMessage, String> {

    List<SupportMessage> findByEmailId(@Param("emailId") String emailId);

}
