package de.computerstudienwerkstatt.tortuga.repository.support;

import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;
import org.springframework.stereotype.Repository;
import de.computerstudienwerkstatt.tortuga.model.support.ComplaintTemplate;

/**
 * @author Mischa Holz
 */
@Repository
public interface ComplaintTemplateRepository extends JpaSpecificationRepository<ComplaintTemplate, String> {
}
