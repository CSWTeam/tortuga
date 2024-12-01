package de.computerstudienwerkstatt.tortuga.controller.api.support;

import de.computerstudienwerkstatt.tortuga.controller.base.AbstractCRUDCtrl;
import de.computerstudienwerkstatt.tortuga.controller.base.ChangeSet;
import de.computerstudienwerkstatt.tortuga.controller.base.response.BadRequestResponse;
import de.computerstudienwerkstatt.tortuga.model.support.SupportMessage;
import de.computerstudienwerkstatt.tortuga.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author Mischa Holz
 */
@RestController
@RequestMapping("/api/v1/" + SupportMessageController.API_BASE)
public class SupportMessageController extends AbstractCRUDCtrl<SupportMessage> {
    public static final String API_BASE = "supportmessages";

    @Autowired
    private EmailService emailService;

    @Override
    @RequestMapping(method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public List<SupportMessage> findAll(HttpServletRequest request) {
        return super.findAll(request);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public SupportMessage findOne(@PathVariable("id") String id) {
        return super.findOne(id);
    }

    @Override
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<SupportMessage> post(@RequestBody SupportMessage newEntity, HttpServletResponse response) {
        if(newEntity.getOpenedAt() == null) {
            newEntity.setOpenedAt(new Date());
        }
        return super.post(newEntity, response);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public ResponseEntity delete(@PathVariable("id") String id) {
        return super.delete(id);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public SupportMessage patch(@PathVariable("id") String id, @RequestBody ChangeSet<SupportMessage> entity) {
        if(entity.getPatch().getAnswer().isPresent()) {
            SupportMessage supportMessage = repository.findOne(id);

            emailService.sendEmail(
                    supportMessage.getEmail().orElseThrow(() -> new BadRequestResponse("Diese Nachricht hat keine Email Adresse, der man antworten könnte.")),
                    supportMessage.getSubject(),
                    entity.getPatch().getAnswer().get());
        }

        return super.patch(id, entity);
    }

    @Override
    public String getApiBase() {
        return API_BASE;
    }
}
