package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequests;
import edu.ucsb.cs156.example.repositories.RecommendationRequestsRepository;

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
        RecommendationRequestsRepository recReqRep;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsbdates/all

        //loged out users
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationrequests/all"))
                                .andExpect(status().is(403)); 
        }

        //logged in users
        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationrequests/all"))
                                .andExpect(status().is(200));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

                // arrange
                LocalDateTime l1 = LocalDateTime.parse("2022-04-20T00:00:00");
                LocalDateTime l2 = LocalDateTime.parse("2022-05-01T00:00:00");

                RecommendationRequests recommendationRequests1 = RecommendationRequests.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("BS/MS program")
                                .dateRequested(l1)
                                .dateNeeded(l2)
                                .done(false)
                                .build();

                LocalDateTime l3 = LocalDateTime.parse("2022-05-20T00:00:00");
                LocalDateTime l4 = LocalDateTime.parse("2022-11-15T00:00:00");

                RecommendationRequests recommendationRequests2 = RecommendationRequests.builder()
                                .requesterEmail("ldelplaya@ucsb.edu")
                                .professorEmail("richert@ucsb.edu")
                                .explanation("PhD CS Stanford")
                                .dateRequested(l3)
                                .dateNeeded(l4)
                                .done(true)
                                .build();

                LocalDateTime l5 = LocalDateTime.parse("2022-05-20T00:00:00");
                LocalDateTime l6 = LocalDateTime.parse("2022-11-15T00:00:00");

                RecommendationRequests recommendationRequests3 = RecommendationRequests.builder()
                                .requesterEmail("ldelplaya@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("PhD CS Stanford")
                                .dateRequested(l5)
                                .dateNeeded(l6)
                                .done(false)
                                .build();

                LocalDateTime l7 = LocalDateTime.parse("2022-05-20T00:00:00");
                LocalDateTime l8 = LocalDateTime.parse("2022-11-15T00:00:00");

                RecommendationRequests recommendationRequests4 = RecommendationRequests.builder()
                                .requesterEmail("alu@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("PhD CE Cal Tech")
                                .dateRequested(l7)
                                .dateNeeded(l8)
                                .done(false)
                                .build();


                ArrayList<RecommendationRequests> expectedRequests = new ArrayList<>();
                expectedRequests.addAll(Arrays.asList(recommendationRequests1, recommendationRequests2, recommendationRequests3, recommendationRequests4));

                when(recReqRep.findAll()).thenReturn(expectedRequests);

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequests/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recReqRep, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRequests);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // DELETE Endpoint Tests

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void adminCanDeleteRecommendationRequest() throws Exception {
        // Prepare
        LocalDateTime requestDate = LocalDateTime.parse("2022-04-20T00:00:00");
        LocalDateTime neededByDate = LocalDateTime.parse("2022-05-01T00:00:00");

        RecommendationRequests requestToDelete = RecommendationRequests.builder()
                        .requesterEmail("cgaucho@ucsb.edu")
                        .professorEmail("phtcon@ucsb.edu")
                        .explanation("BS/MS program")
                        .dateRequested(requestDate)
                        .dateNeeded(neededByDate)
                        .done(false)
                        .build();

        when(recReqRep.findById(eq(15L))).thenReturn(Optional.of(requestToDelete));

        // Perform deletion
        MvcResult response = mockMvc.perform(
                        delete("/api/recommendationrequests?id=15")
                                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andReturn();

        // Verify and assert
        verify(recReqRep, times(1)).findById(15L);
        verify(recReqRep, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("RecommendationRequests with id 15 deleted", json.get("message"));
        }
        


        // PUT Endpoint Tests

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void adminCanEditExistingRecommendationRequest() throws Exception {
        // Prepare
        LocalDateTime origRequestDate = LocalDateTime.parse("2022-04-20T00:00:00");
        LocalDateTime origNeededByDate = LocalDateTime.parse("2022-05-01T00:00:00");
        LocalDateTime newRequestDate = LocalDateTime.parse("2022-04-03T00:00:00");
        LocalDateTime newNeededByDate = LocalDateTime.parse("2022-05-02T00:00:00");

        RecommendationRequests origRequest = RecommendationRequests.builder()
                        .requesterEmail("cgaucho@ucsb.edu")
                        .professorEmail("phtcon@ucsb.edu")
                        .explanation("BS/MS program")
                        .dateRequested(origRequestDate)
                        .dateNeeded(origNeededByDate)
                        .done(false)
                        .build();

        RecommendationRequests editedRequest = RecommendationRequests.builder()
                        .requesterEmail("agaucho@ucsb.edu")
                        .professorEmail("schow@ucsb.edu")
                        .explanation("PhD program")
                        .dateRequested(newRequestDate)
                        .dateNeeded(newNeededByDate)
                        .done(true)
                        .build();

        String requestBody = mapper.writeValueAsString(editedRequest);

        when(recReqRep.findById(eq(67L))).thenReturn(Optional.of(origRequest));

        // Perform edit
        MvcResult response = mockMvc.perform(
                        put("/api/recommendationrequests?id=67")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andReturn();

        // Verify and assert
        verify(recReqRep, times(1)).findById(67L);
        verify(recReqRep, times(1)).save(editedRequest); // Ensure it's saved with the correct user
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
        }
        // Tests for POST 

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

                LocalDateTime l1 = LocalDateTime.parse("2022-04-20T00:00:00");
                LocalDateTime l2 = LocalDateTime.parse("2022-05-01T00:00:00");

                RecommendationRequests recommendationRequests1 = RecommendationRequests.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("BS/MS program")
                                .dateRequested(l1)
                                .dateNeeded(l2)
                                .done(false)
                                .build();

                when(recReqRep.save(eq(recommendationRequests1))).thenReturn(recommendationRequests1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/recommendationrequests/post?requesterEmail=cgaucho@ucsb.edu&professorEmail=phtcon@ucsb.edu&Explanation=BS/MS program&dateRequested=2022-04-20T00:00:00&dateNeeded=2022-05-01T00:00:00&done=false")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recReqRep, times(1)).save(recommendationRequests1);
                String expectedJson = mapper.writeValueAsString(recommendationRequests1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_recommendationrequest1() throws Exception {
                // arrange

                LocalDateTime l1 = LocalDateTime.parse("2022-04-20T00:00:00");
                LocalDateTime l2 = LocalDateTime.parse("2022-05-01T00:00:00");

                RecommendationRequests recommendationRequests1 = RecommendationRequests.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("BS/MS program")
                                .dateRequested(l1)
                                .dateNeeded(l2)
                                .done(true)
                                .build();

                when(recReqRep.save(eq(recommendationRequests1))).thenReturn(recommendationRequests1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/recommendationrequests/post?requesterEmail=cgaucho@ucsb.edu&professorEmail=phtcon@ucsb.edu&Explanation=BS/MS program&dateRequested=2022-04-20T00:00:00&dateNeeded=2022-05-01T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recReqRep, times(1)).save(recommendationRequests1);
                String expectedJson = mapper.writeValueAsString(recommendationRequests1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        //Test for GET /api/recommendationrequests?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/recommendationrequests?id=10"))
                    .andExpect(status().isForbidden()); // logged out users can't get by id
        }
    
        @WithMockUser(roles = { "USER" })
        @Test
        public void loggedInUserGetsNotFoundWhenIdDoesNotExist() throws Exception {
            // Arrange
            long nonExistentId = 10L;
            when(recReqRep.findById(nonExistentId)).thenReturn(Optional.empty());
    
            // Act & Assert
            mockMvc.perform(get("/api/recommendationrequests?id=" + nonExistentId))
                    .andExpect(status().isNotFound());
        }
    
        @WithMockUser(roles = { "USER" })
        @Test
        public void loggedInUserCanGetByIdWhenIdExists() throws Exception {
            // Arrange
            long existingId = 7L;
            LocalDateTime l1 = LocalDateTime.parse("2022-04-20T00:00:00");
            LocalDateTime l2 = LocalDateTime.parse("2022-05-01T00:00:00");
            RecommendationRequests recommendation = RecommendationRequests.builder()
                    .requesterEmail("cgaucho@ucsb.edu")
                    .professorEmail("phtcon@ucsb.edu")
                    .explanation("BS/MS program")
                    .dateRequested(l1)
                    .dateNeeded(l2)
                    .done(true)
                    .build();
            when(recReqRep.findById(existingId)).thenReturn(Optional.of(recommendation));
    
            // Act & Assert
            mockMvc.perform(get("/api/recommendationrequests?id=" + existingId))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(recommendation)));
        }
    
        @WithMockUser(roles = { "USER" })
        @Test
        public void loggedInUserGetsEntityNotFoundExceptionWhenIdDoesNotExist() throws Exception {
            // Arrange
            long nonExistentId = 10L;
            when(recReqRep.findById(nonExistentId)).thenReturn(Optional.empty());
    
            // Act & Assert
            mockMvc.perform(get("/api/recommendationrequests?id=" + nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("RecommendationRequests with id 10 not found"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_recommendationrequest_and_gets_right_error_message()
                        throws Exception {
                // Arrange: Mocking the absence of the recommendation request with ID 15

                when(recReqRep.findById(eq(15L))).thenReturn(Optional.empty());

                // Act: Performing a delete request to delete the non-existent recommendation request
                MvcResult response = mockMvc.perform(
                                delete("/api/recommendationrequests?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // Assert: Verifying that the findById method was called once with ID 15
                verify(recReqRep, times(1)).findById(15L);

                // Parsing the response JSON and asserting that the correct error message is returned
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequests with id 15 not found", json.get("message"));
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime l1 = LocalDateTime.parse("2022-04-20T00:00:00");
                LocalDateTime l2 = LocalDateTime.parse("2022-05-01T00:00:00");
       

                RecommendationRequests recommendationRequestsOG = RecommendationRequests.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("BS/MS program")
                                .dateRequested(l1)
                                .dateNeeded(l2)
                                .done(false)
                                .build();

                String requestBody = mapper.writeValueAsString(recommendationRequestsOG);

                when(recReqRep.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recommendationrequests?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recReqRep, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequests with id 67 not found", json.get("message"));

        }

        
}



