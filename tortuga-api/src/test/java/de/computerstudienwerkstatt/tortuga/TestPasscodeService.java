package de.computerstudienwerkstatt.tortuga;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mischa Holz
 */
@Service
public class TestPasscodeService {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockLoggedInUserHolder loggedInUserHolder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @PostConstruct
    public void postInit() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    public String getPasscode() throws Exception {
        String json = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/" + loggedInUserHolder.getLoggedInUser().get().getId() + "/passcode")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> response = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });

        assertNotNull(response.get("passcode"));

        @SuppressWarnings("unchecked")
        List<String> code = (List<String>) response.get("passcode");
        assertTrue(code.size() == 5);

        return code.stream().reduce("", (a, b) -> a + b);
    }

}
