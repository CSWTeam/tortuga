package de.computerstudienwerkstatt.tortuga.controller.api.statistics;

import de.computerstudienwerkstatt.tortuga.controller.base.AbstractCRUDCtrl;
import de.computerstudienwerkstatt.tortuga.model.statistics.DoorAuthorisationAttempt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Mischa Holz
 */
@RestController
@RequestMapping("/api/v1/" + DoorAuthorisationAttemptController.API_BASE)
public class DoorAuthorisationAttemptController extends AbstractCRUDCtrl<DoorAuthorisationAttempt> {
    public static final String API_BASE = "stats/doorauthorisationattempts";

    @Override
    @RequestMapping
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public List<DoorAuthorisationAttempt> findAll(HttpServletRequest request) {
        return super.findAll(request);
    }

    @Override
    protected String getApiBase() {
        return API_BASE;
    }
}
