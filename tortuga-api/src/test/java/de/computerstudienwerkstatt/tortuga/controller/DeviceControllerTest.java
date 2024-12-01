package de.computerstudienwerkstatt.tortuga.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.computerstudienwerkstatt.tortuga.TestContext;
import de.computerstudienwerkstatt.tortuga.model.device.Device;
import de.computerstudienwerkstatt.tortuga.model.devicecategory.DeviceCategory;
import de.computerstudienwerkstatt.tortuga.repository.device.DeviceCategoryRepository;
import de.computerstudienwerkstatt.tortuga.repository.device.DeviceRepository;
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
import de.computerstudienwerkstatt.tortuga.model.cabinet.Cabinet;

import javax.annotation.Resource;
import java.util.Date;
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
public class DeviceControllerTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Device one;

    private Device two;

    private DeviceCategory deviceCategory;

    private DeviceCategory otherCategory;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceCategoryRepository deviceCategoryRepository;

    @Before
    public void setUp() throws Exception {
        deviceRepository.deleteAllInBatch();
        deviceCategoryRepository.deleteAllInBatch();

        deviceCategory = new DeviceCategory();
        deviceCategory.setName("Category");

        deviceCategory = deviceCategoryRepository.save(deviceCategory);

        otherCategory = new DeviceCategory();
        otherCategory.setName("Other");

        otherCategory = deviceCategoryRepository.save(otherCategory);

        one = new Device();
        one.setName("Device 1");
        one.setAccessories("Accessories");
        one.setAcquisitionDate(Optional.of(new Date()));
        one.setCabinet(Cabinet.CABINET_6);
        one.setCategory(deviceCategory);
        one.setDescription("description");
        one.setInventoryNumber("1234");

        two = new Device();
        two.setName("Device 2");
        two.setAccessories("Accessories");
        two.setAcquisitionDate(Optional.of(new Date()));
        two.setCabinet(Cabinet.CABINET_6);
        two.setCategory(otherCategory);
        two.setDescription("description");
        two.setInventoryNumber("1234");

        one = deviceRepository.save(one);
        two = deviceRepository.save(two);

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @After
    public void tearDown() throws Exception {
        deviceRepository.deleteAllInBatch();
        deviceCategoryRepository.deleteAllInBatch();
    }

    @Test
    public void testFindAll() throws Exception {
        mockMvc.perform(get("/api/v1/devices").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/v1/devices?name=" + one.getName()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", Matchers.is(one.getId())))
                .andExpect(jsonPath("$[0].name", is(one.getName())));
    }

    @Test
    public void testFindOne() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/devices/" + one.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", Matchers.is(one.getId())))
                .andExpect(jsonPath("$.name", is(one.getName())));
    }

    @Test
    public void testIllegalSuggestionQuery() throws Exception {
        mockMvc.perform(get("/api/v1/devices?beginningTime=1234").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testFilterByCategory() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/devices?category=" + one.getCategory().getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", Matchers.is(one.getId())))
                .andExpect(jsonPath("$[0].name", is(one.getName())));
    }

    @Test
    public void testPostDevice() throws Exception {
        Device device3 = new Device();
        device3.setName("Device 3");
        device3.setAccessories("Accessories");
        device3.setAcquisitionDate(Optional.of(new Date()));
        device3.setCabinet(Cabinet.CABINET_6);
        device3.setCategory(deviceCategory);
        device3.setDescription("description");
        device3.setInventoryNumber("1234");
        device3.setId(null);

        String location = mockMvc.perform(post("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(device3))
        )
                .andExpect(jsonPath("$.name", is(device3.getName())))
                .andExpect(header().string("Location", Matchers.notNullValue()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(device3.getName())));
    }

    @Test
    public void testPatchDevice() throws Exception {
        Device devicePatch = new Device();
        devicePatch.setName("Device3");
        devicePatch.setId(null);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/devices/" + one.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(devicePatch))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(one.getId())))
                .andExpect(jsonPath("$.name", is("Device3")));
    }

    @Test
    public void testDeleteDevice() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/devices/" + one.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/devices/" + one.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteDeviceCategoryWithExistingDevices() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/devicecategories/" + deviceCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
