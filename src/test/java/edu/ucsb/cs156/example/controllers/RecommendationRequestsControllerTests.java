package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.controllers.RecommendationRequestsController;
import edu.ucsb.cs156.example.entities.RecommendationRequests;
import edu.ucsb.cs156.example.entities.RecommendationRequests;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.RecommendationRequestsRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationRequestsController.class)
@Import(TestConfig.class)
public class RecommendationRequestsControllerTests extends ControllerTestCase {

    @MockBean
    RecommendationRequestsRepository recommendationRequestRepository;

    @MockBean
    UserRepository userRepository;

    // Tests for GET /api/recommendationrequests/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/recommendationrequests/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/recommendationrequests/all"))
                            .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

            // arrange
            LocalDateTime dr1 = LocalDateTime.parse("2022-04-20T00:00:00");
            LocalDateTime dn1 = LocalDateTime.parse("2022-05-01T00:00:00");

            RecommendationRequests recommendationRequest1 = RecommendationRequests.builder()
                            .requesterEmail("cgaucho@ucsb.edu")
                            .professorEmail("phtcon@ucsb.edu")
                            .explanation("BS/MS program")
                            .dateRequested(dr1)
                            .dateNeeded(dn1)
                            .done(false)
                            .build();

            LocalDateTime dr2 = LocalDateTime.parse("2022-05-20T00:00:00");
            LocalDateTime dn2 = LocalDateTime.parse("2022-11-15T00:00:00");

            RecommendationRequests recommendationRequest2 = RecommendationRequests.builder()
                            .requesterEmail("ldelplaya@ucsb.edu")
                            .professorEmail("richert@ucsb.edu")
                            .explanation("PhD CS Stanford")
                            .dateRequested(dr2)
                            .dateNeeded(dn2)
                            .done(true)
                            .build();

            ArrayList<RecommendationRequests> expectedRequests = new ArrayList<>();
            expectedRequests.addAll(Arrays.asList(recommendationRequest1, recommendationRequest2));

            when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

            // act
            MvcResult response = mockMvc.perform(get("/api/recommendationrequests/all"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(recommendationRequestRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedRequests);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    // Tests for POST /api/recommendationrequests/post...

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/recommendationrequests/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/recommendationrequests/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
            // arrange

            LocalDateTime dr1 = LocalDateTime.parse("2022-04-20T00:00:00");
            LocalDateTime dn1 = LocalDateTime.parse("2022-05-01T00:00:00");

            RecommendationRequests recommendationRequest1 = RecommendationRequests.builder()
                            .requesterEmail("cgaucho@ucsb.edu")
                            .professorEmail("phtcon@ucsb.edu")
                            .explanation("BS/MS program")
                            .dateRequested(dr1)
                            .dateNeeded(dn1)
                            .done(true)
                            .build();

            when(recommendationRequestRepository.save(eq(recommendationRequest1))).thenReturn(recommendationRequest1);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/recommendationrequests/post?requesterEmail=cgaucho@ucsb.edu&professorEmail=phtcon@ucsb.edu&explanation=BS/MS program&dateRequested=2022-04-20T00:00:00&dateNeeded=2022-05-01T00:00:00&done=true")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(recommendationRequestRepository, times(1)).save(recommendationRequest1);
            String expectedJson = mapper.writeValueAsString(recommendationRequest1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

}