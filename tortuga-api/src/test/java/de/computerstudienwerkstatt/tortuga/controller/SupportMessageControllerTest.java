package de.computerstudienwerkstatt.tortuga.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.computerstudienwerkstatt.tortuga.model.support.SupportMessage;
import de.computerstudienwerkstatt.tortuga.repository.support.SupportMessageRepository;
import org.hamcrest.Matchers;
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

import javax.annotation.Resource;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Mischa Holz
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestContext.class)
@WebAppConfiguration
public class SupportMessageControllerTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private SupportMessage one;

    private SupportMessage two;

    private MockMvc mockMvc;

    @Autowired
    private SupportMessageRepository supportMessageRepository;

    @Before
    public void setUp() throws Exception {
        supportMessageRepository.deleteAllInBatch();

        one = new SupportMessage();
        one.setBody("body");
        one.setDone(false);
        one.setEmail(Optional.of("mai@mail.de"));
        one.setName(Optional.empty());
        one.setSubject("subject");

        one = supportMessageRepository.save(one);

        two = new SupportMessage();
        two.setBody("body");
        two.setDone(true);
        two.setEmail(Optional.empty());
        two.setName(Optional.empty());
        two.setSubject("subject");

        two = supportMessageRepository.save(two);

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @After
    public void tearDown() throws Exception {
        supportMessageRepository.deleteAllInBatch();
    }

    @Test
    public void testFindAll() throws Exception {
        mockMvc.perform(get("/api/v1/supportmessages").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(one.getId())))
                .andExpect(jsonPath("$[1].id", is(two.getId())));
    }

    @Test
    public void testFindAllNotDone() throws Exception {
        mockMvc.perform(get("/api/v1/supportmessages?done=false").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(one.getId())));
    }

    @Test
    public void testFindOne() throws Exception {
        mockMvc.perform(get("/api/v1/supportmessages/" + one.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(one.getId())));
    }

    @Test
    public void testPostSupportMessage() throws Exception {
        SupportMessage three = new SupportMessage();
        three.setBody("body");
        three.setDone(true);
        three.setEmail(Optional.empty());
        three.setName(Optional.of("name"));
        three.setSubject("subject");
        three.setId(null);


        String location = mockMvc.perform(post("/api/v1/supportmessages")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(three))
        )
                .andExpect(header().string("Location", Matchers.notNullValue()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(three.getName().get())));
    }

    @Test
    public void testDeleteRoomReservation() throws Exception {
        mockMvc.perform(delete("/api/v1/supportmessages/" + one.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/supportmessages/" + one.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPatchSupportMessage() throws Exception {

        String patchJson = "{\"done\": true, \"answer\": \"answer\"}";

        mockMvc.perform(patch("/api/v1/supportmessages/" + one.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(patchJson)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(one.getId())))
                .andExpect(jsonPath("$.answer", is("answer")))
                .andExpect(jsonPath("$.done", is(true)));
    }
}
