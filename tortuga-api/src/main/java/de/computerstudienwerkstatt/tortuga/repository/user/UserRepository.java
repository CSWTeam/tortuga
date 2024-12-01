package de.computerstudienwerkstatt.tortuga.repository.user;

import de.computerstudienwerkstatt.tortuga.model.user.User;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;

import java.util.List;

/**
 * @author Mischa Holz
 */
@Repository
public interface UserRepository extends JpaSpecificationRepository<User, String> {

    User findOneByLoginName(@Param("loginName") String loginName);

    User findOneByEmail(@Param("email") String email);

    List<User> findAllByMajorId(String majorId);

}
