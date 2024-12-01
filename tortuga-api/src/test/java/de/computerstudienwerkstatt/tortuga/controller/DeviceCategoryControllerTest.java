package de.computerstudienwerkstatt.tortuga.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import de.computerstudienwerkstatt.tortuga.model.devicecategory.DeviceCategory;
import de.computerstudienwerkstatt.tortuga.repository.device.DeviceCategoryRepository;
import de.computerstudienwerkstatt.tortuga.repository.device.DeviceRepository;

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
public class DeviceCategoryControllerTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private DeviceCategory deviceCategory1;

    private DeviceCategory deviceCategory2;

    @Autowired
    private DeviceCategoryRepository deviceCategoryRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Before
    public void setUp() throws Exception {
        deviceRepository.deleteAllInBatch();
        deviceCategoryRepository.deleteAllInBatch();

        deviceCategory1 = new DeviceCategory();
        deviceCategory1.setName("Device 1");
        deviceCategory1.setActive(true);

        deviceCategory2 = new DeviceCategory();
        deviceCategory2.setName("Device 2");
        deviceCategory1.setActive(true);

        deviceCategoryRepository.save(Arrays.asList(deviceCategory1, deviceCategory2));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @After
    public void tearDown() throws Exception {
        deviceRepository.deleteAllInBatch();
        deviceCategoryRepository.deleteAllInBatch();
    }

    @Test
    public void testFindAll() throws Exception {
        mockMvc.perform(get("/api/v1/devicecategories").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(deviceCategory1.getId())))
                .andExpect(jsonPath("$[0].name", is(deviceCategory1.getName())))
                .andExpect(jsonPath("$[1].id", is(deviceCategory2.getId())));
    }

    @Test
    public void testFindOne() throws Exception {
        mockMvc.perform(get("/api/v1/devicecategories/" + deviceCategory1.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(deviceCategory1.getId())))
                .andExpect(jsonPath("$.name", is(deviceCategory1.getName())));
    }

    @Test
    public void testPostDeviceCategory() throws Exception {
        DeviceCategory deviceCategory = new DeviceCategory();
        deviceCategory.setName("DeviceCategory 3");
        deviceCategory.setId(null);

        String location = mockMvc.perform(post("/api/v1/devicecategories")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(deviceCategory))
        )
         .andExpect(jsonPath("$.name", is(deviceCategory.getName())))
         .andExpect(header().string("Location", Matchers.notNullValue()))
         .andExpect(status().isCreated())
         .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(deviceCategory.getName())));
    }

    @Test
    public void testPatchDeviceCategory() throws Exception {
        DeviceCategory deviceCategory = new DeviceCategory();
        deviceCategory.setName("bla");
        deviceCategory.setId(null);

        mockMvc.perform(patch("/api/v1/devicecategories/" + deviceCategory2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deviceCategory)))
                .andExpect(jsonPath("$.id", is(deviceCategory2.getId())))
                .andExpect(jsonPath("$.name", is(deviceCategory.getName())));
    }

    @Test
    public void testDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/devicecategories/" + deviceCategory1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/devicecategories/" + deviceCategory1.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
