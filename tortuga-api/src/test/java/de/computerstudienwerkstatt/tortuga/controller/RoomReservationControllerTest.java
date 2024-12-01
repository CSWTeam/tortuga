package de.computerstudienwerkstatt.tortuga.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.computerstudienwerkstatt.tortuga.MockLoggedInUserHolder;
import de.computerstudienwerkstatt.tortuga.TestContext;
import de.computerstudienwerkstatt.tortuga.TestHelper;
import de.computerstudienwerkstatt.tortuga.controller.base.advice.RestExceptionHandler;
import de.computerstudienwerkstatt.tortuga.model.reservation.RoomReservation;
import de.computerstudienwerkstatt.tortuga.model.reservation.TimeSpan;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.repository.reservation.RoomReservationRepository;
import de.computerstudienwerkstatt.tortuga.repository.statistics.DoorAuthorisationAttemptRepository;
import de.computerstudienwerkstatt.tortuga.repository.user.UserRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import de.computerstudienwerkstatt.tortuga.model.reservation.RepeatOption;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Mischa Holz
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestContext.class)
@WebAppConfiguration
public class RoomReservationControllerTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private RoomReservation one;

    private RoomReservation two;

    @Autowired
    private RoomReservationRepository roomReservationRepository;

    @Autowired
    private MockLoggedInUserHolder mockLoggedInUserHolder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoorAuthorisationAttemptRepository doorAuthorisationAttemptRepository;

    @Before
    public void setUp() throws Exception {
        roomReservationRepository.deleteAllInBatch();
        doorAuthorisationAttemptRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        mockLoggedInUserHolder.setUp();

        User other = TestHelper.createUser();
        other = userRepository.save(other);

        one = new RoomReservation();
        one.setTimeSpan(new TimeSpan(TestHelper.getDate(100), TestHelper.getDate(200)));
        one.setApproved(true);
        one.setTitle("beschreibung");
        one.setOpen(false);
        one.setUser(mockLoggedInUserHolder.getLoggedInUser().get());

        two = new RoomReservation();
        two.setTimeSpan(new TimeSpan(TestHelper.getDate(201), TestHelper.getDate(300)));
        two.setTitle("beschreibung");
        two.setApproved(false);
        two.setOpen(false);
        two.setUser(other);

        roomReservationRepository.save(Arrays.asList(one, two));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @After
    public void tearDown() throws Exception {
        roomReservationRepository.deleteAllInBatch();
        doorAuthorisationAttemptRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    public void testFindAll() throws Exception {
        mockMvc.perform(get("/api/v1/roomreservations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].id", Matchers.containsInAnyOrder(one.getId(), two.getId())));

        Assert.assertNotEquals(one.getUser().getId(), two.getUser().getId());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/roomreservations?user=" + one.getUser().getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", Matchers.is(one.getId())));

        mockMvc.perform(get("/api/v1/roomreservations?user.DOESNOTEXIST.EVENLESS=bla").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testFindOne() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/roomreservations/" + one.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", Matchers.is(one.getId())))
                .andExpect(jsonPath("$.user.id", Matchers.is(one.getUser().getId())));
    }

    @Test
    public void testOverlappingRoomReservations() throws Exception {
        RoomReservation three = new RoomReservation();
        three.setTimeSpan(new TimeSpan(TestHelper.getDate(150), TestHelper.getDate(500)));
        three.setOpen(true);
        three.setApproved(true);
        three.setTitle("titel");

        String json = mockMvc.perform(post("/api/v1/roomreservations")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(three)))
                .andExpect(status().is4xxClientError())
                .andReturn().getResponse().getContentAsString();

        RestExceptionHandler.ValidationError error = objectMapper.readValue(json, RestExceptionHandler.ValidationError.class);
        Assert.assertFalse("RoomtReservations overlapping has to be a global error", error.getErrors().get(RestExceptionHandler.ValidationError.GLOBAL_ERROR_KEY).isEmpty());
    }

    @Test
    public void testRepeatRoomReservations() throws Exception {
        roomReservationRepository.deleteAllInBatch();

        RoomReservation three = new RoomReservation();
        three.setTimeSpan(new TimeSpan(TestHelper.getDate(0), TestHelper.getDate(500)));
        three.setTitle("beschreibung");
        three.setId(null);
        three.setRepeatOption(Optional.of(RepeatOption.WEEKLY));
        three.setRepeatUntil(Optional.of(TestHelper.getDate(3 * 7 * 24 * 60 * 60 * 1000 + 5)));

        String json = mockMvc.perform(post("/api/v1/roomreservations")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(three)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        RoomReservation master = objectMapper.readValue(json, RoomReservation.class);
        String sharedId = master.getSharedId().orElseThrow(() -> new AssertionError("Shared Id needs to be present"));

        assertTrue(master.getSharedId().isPresent());

        //noinspection PointlessArithmeticExpression
        mockMvc.perform(get("/api/v1/roomreservations?approved=false&sort=timeSpan.beginning&direction=ASC").contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].sharedId", is(sharedId)))
                .andExpect(jsonPath("$[1].sharedId", is(sharedId)))
                .andExpect(jsonPath("$[2].sharedId", is(sharedId)))
                .andExpect(jsonPath("$[3].sharedId", is(sharedId)))
                .andExpect(jsonPath("$[0].timeSpan.beginning", anyOf(
                        is(TestHelper.getDate().getTime()),
                        is(TestHelper.getDate(- 3_600_000).getTime()),
                        is(TestHelper.getDate(3_600_000).getTime())
                )))
                .andExpect(jsonPath("$[1].timeSpan.beginning", anyOf(
                        is(TestHelper.getDate(1 * 7 * 24 * 60 * 60 * 1000).getTime()),
                        is(TestHelper.getDate(1 * 7 * 24 * 60 * 60 * 1000 - 3_600_000).getTime()),
                        is(TestHelper.getDate(1 * 7 * 24 * 60 * 60 * 1000 + 3_600_000).getTime())
                )))
                .andExpect(jsonPath("$[2].timeSpan.beginning", anyOf(
                        is(TestHelper.getDate(2 * 7 * 24 * 60 * 60 * 1000).getTime()),
                        is(TestHelper.getDate(2 * 7 * 24 * 60 * 60 * 1000 - 3_600_000).getTime()),
                        is(TestHelper.getDate(2 * 7 * 24 * 60 * 60 * 1000 + 3_600_000).getTime())
                )))
                .andExpect(jsonPath("$[3].timeSpan.beginning", anyOf(
                        is(TestHelper.getDate(3 * 7 * 24 * 60 * 60 * 1000).getTime()),
                        is(TestHelper.getDate(3 * 7 * 24 * 60 * 60 * 1000 - 3_600_000).getTime()),
                        is(TestHelper.getDate(3 * 7 * 24 * 60 * 60 * 1000 + 3_600_000).getTime())
                )));
    }

    @Test
    public void testPatchRoomReservation() throws Exception {
        RoomReservation three = new RoomReservation();
        three.setApproved(true);
        three.setId(null);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/roomreservations/" + two.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(three)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved", is(true)));
    }

    @Test
    public void testPostRoomReservation() throws Exception {
        RoomReservation three = new RoomReservation();
        three.setTimeSpan(new TimeSpan(TestHelper.getDate(401), TestHelper.getDate(500)));
        three.setOpen(true);
        three.setApproved(true);
        three.setTitle("beschreibung");
        three.setId(null);

        String location = mockMvc.perform(post("/api/v1/roomreservations")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .accept(MediaType.APPLICATION_JSON)
                                                  .content(objectMapper.writeValueAsString(three))
        )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.notNullValue()))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user.id", Matchers.is(mockLoggedInUserHolder.getLoggedInUser().get().getId())))
                .andExpect(jsonPath("$.approved", is(false)))
                .andExpect(jsonPath("$.open", is(false)));
    }

    @Test
    public void testOpenRoomWithRoomReservation() throws Exception {
        RoomReservation open = new RoomReservation();
        open.setTimeSpan(new TimeSpan(new Date(new Date().getTime() - 30 * 60 * 1000), new Date(new Date().getTime() + 30 * 60 * 1000)));
        open.setOpen(true);
        open.setApproved(true);
        open.setTitle("title");
        open.setUser(mockLoggedInUserHolder.getLoggedInUser().get());

        roomReservationRepository.save(open);

        mockMvc.perform(patch("/api/v1/terminal/door")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{\"open\":true}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testIllegalRepeatOption() throws Exception {
        RoomReservation three = new RoomReservation();
        three.setRepeatOption(Optional.of(RepeatOption.WEEKLY));

        mockMvc.perform(post("/api/v1/roomreservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(three))
        )
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testDeleteRoomReservation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/roomreservations/" + one.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/roomreservations/" + one.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteNonExistentRoomReservation() throws Exception {
        mockMvc.perform(delete("/api/v1/roomreservations/blabla")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPostIllegalEndDate() throws Exception {
        RoomReservation three = new RoomReservation();
        three.setTimeSpan(new TimeSpan(TestHelper.getDateInPast(401), TestHelper.getDateInPast(500)));
        three.setOpen(true);
        three.setApproved(true);
        three.setTitle("beschreibung");
        three.setId(null);

        mockMvc.perform(post("/api/v1/roomreservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(three))
        )
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testPatchOutsideLocalNet() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/v1/roomreservations/" + one.getId())
                        .with(mockHttpServletRequest -> {
                            mockHttpServletRequest.setRemoteAddr("8.9.8.8");
                            return mockHttpServletRequest;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"open\": true}")
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPatchNonExistentRoomReservation() throws Exception {
        mockMvc.perform(
                patch("/api/v1/roomreservations/blabla")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"open\": true}")
        )
                .andExpect(status().isNotFound());
    }


    @Test
    public void testPatchInsideLocalNet() throws Exception {
        one.setOpen(true);
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/v1/roomreservations/" + one.getId())
                        .with(mockHttpServletRequest -> {
                            mockHttpServletRequest.setRemoteAddr("192.168.0.107");
                            return mockHttpServletRequest;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(one))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open", is(true)));
    }

    @Test
    public void testPatchIllegalEndDate() throws Exception {

    }
}
