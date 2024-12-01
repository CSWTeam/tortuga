package de.computerstudienwerkstatt.tortuga.repository.user;

import de.computerstudienwerkstatt.tortuga.model.major.Major;
import org.springframework.stereotype.Repository;
import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;

/**
 * Created by hannes on 24.11.15.
 */
@Repository
public interface MajorRepository extends JpaSpecificationRepository<Major, String> {

}
