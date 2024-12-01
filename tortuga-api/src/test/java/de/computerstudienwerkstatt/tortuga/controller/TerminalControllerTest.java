package de.computerstudienwerkstatt.tortuga.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.computerstudienwerkstatt.tortuga.MockLoggedInUserHolder;
import de.computerstudienwerkstatt.tortuga.service.TimedTokenService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import de.computerstudienwerkstatt.tortuga.TestContext;
import de.computerstudienwerkstatt.tortuga.TestPasscodeService;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mischa Holz
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestContext.class)
@WebAppConfiguration
public class TerminalControllerTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockLoggedInUserHolder loggedInUserHolder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestPasscodeService testPasscodeService;

    @Autowired
    private TimedTokenService timedTokenService;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        loggedInUserHolder.setUp();

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @After
    public void tearDown() throws Exception {
        loggedInUserHolder.tearDown();
    }

    @Test
    public void testGenerateAuthCode() throws Exception {
        mockMvc.perform(
                get("/api/v1/terminal/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(jsonPath("$", not(isEmptyString())));
    }

    //TODO Make proper fix
    //@Test
    public void testGenerateAuthCodeFromTheOutside() throws Exception {
        mockMvc.perform(
                get("/api/v1/terminal/code")
                        .with(mockHttpServletRequest -> {
                            mockHttpServletRequest.setRemoteAddr("8.9.8.8");
                            return mockHttpServletRequest;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPatchOpenDoorToFalse() throws Exception {
        mockMvc.perform(
                patch("/api/v1/terminal/door")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"open\":false}")
        )
                .andExpect(status().isNoContent());
    }

    @Test
    public void testOpenDoorWithPasscode() throws Exception {
        String passcode = testPasscodeService.getPasscode();

        mockMvc.perform(
                patch("/api/v1/terminal/door?passcode=" + passcode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"open\":true}")
        )
                .andExpect(status().isNoContent());
    }

    @Test
    public void testOpenDoorWithInvalidPasscode() throws Exception {
        String passcode = "blabla";

        mockMvc.perform(
                patch("/api/v1/terminal/door?passcode=" + passcode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"open\":true}")
        )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testOpenDoorWithTokenWithoutBeingLoggedIn() throws Exception {
        loggedInUserHolder.forgetMe();

        mockMvc.perform(
                patch("/api/v1/terminal/door?token=123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"open\":true}")
        )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testOpenDoorWithInvalidToken() throws Exception {
        mockMvc.perform(
                patch("/api/v1/terminal/door?token=123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"open\":true}")
        )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testOpenDoorWithValidToken() throws Exception {
        Long token = timedTokenService.getCurrentToken();

        mockMvc.perform(
                patch("/api/v1/terminal/door?token=" + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"open\":true}")
        )
                .andExpect(status().isNoContent());
    }

    @Test
    public void testOpenDoorWithoutAnyAuthorization() throws Exception {
        mockMvc.perform(
                patch("/api/v1/terminal/door")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"open\":true}")
        )
                .andExpect(status().isUnauthorized());
    }
}
