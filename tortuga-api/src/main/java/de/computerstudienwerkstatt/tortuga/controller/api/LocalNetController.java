package de.computerstudienwerkstatt.tortuga.controller.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Hannes Güdelhöfer
 */
@RestController
@RequestMapping("/api/v1/localnet")
public class LocalNetController {
    @RequestMapping
    public Boolean localNet(HttpServletRequest request) {
        //TODO MAKE PROBER FIX,
        return true;
        // return NetworkUtil.isLocalNetworkRequest(request);
    }
}
