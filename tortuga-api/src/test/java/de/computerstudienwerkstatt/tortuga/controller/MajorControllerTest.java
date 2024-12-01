package de.computerstudienwerkstatt.tortuga.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.computerstudienwerkstatt.tortuga.TestContext;
import de.computerstudienwerkstatt.tortuga.model.major.Major;
import de.computerstudienwerkstatt.tortuga.repository.statistics.DoorAuthorisationAttemptRepository;
import de.computerstudienwerkstatt.tortuga.repository.user.MajorRepository;
import de.computerstudienwerkstatt.tortuga.repository.user.UserRepository;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Arrays;

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
public class MajorControllerTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Major major1;

    private Major major2;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoorAuthorisationAttemptRepository doorAuthorisationAttemptRepository;

    @Before
    public void setUp() throws Exception {
        doorAuthorisationAttemptRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        majorRepository.deleteAllInBatch();

        major1 = new Major();
        major1.setName("Major 1");

        major2 = new Major();
        major2.setName("Major 2");

        majorRepository.save(Arrays.asList(major1, major2));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @After
    public void tearDown() throws Exception {
        doorAuthorisationAttemptRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        majorRepository.deleteAllInBatch();
    }

    @Test
    public void testFindAll() throws Exception {
        mockMvc.perform(get("/api/v1/majors").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", Matchers.is(major1.getId())))
                .andExpect(jsonPath("$[0].name", is(major1.getName())))
                .andExpect(jsonPath("$[1].id", Matchers.is(major2.getId())));
    }

    @Test
    public void testFindOne() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/majors/" + major1.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", Matchers.is(major1.getId())))
                .andExpect(jsonPath("$.name", is(major1.getName())));
    }

    @Test
    public void testPostMajor() throws Exception {
        Major major3 = new Major();
        major3.setName("Major 3");

        String location = mockMvc.perform(post("/api/v1/majors")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(major3))
        ).andExpect(jsonPath("$.id", Matchers.is(major3.getId())))
         .andExpect(jsonPath("$.name", is(major3.getName())))
         .andExpect(header().string("Location", Matchers.notNullValue()))
         .andExpect(status().isCreated())
         .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", Matchers.is(major3.getId())))
                .andExpect(jsonPath("$.name", is(major3.getName())));
    }

    @Test
    public void testPatchMajor() throws Exception {
        Major major = new Major();
        major.setName("bla");
        major.setId(null);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/majors/" + major2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(major)))
                .andExpect(jsonPath("$.id", Matchers.is(major2.getId())))
                .andExpect(jsonPath("$.name", is(major.getName())));
    }

    @Test
    public void testDelete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/majors/" + major1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/majors/" + major1.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
