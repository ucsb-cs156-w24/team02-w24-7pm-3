package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

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

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

        @MockBean
        HelpRequestRepository helpRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/helprequest/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_helprequest() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest req1 = HelpRequest.builder()
                                .requesterEmail("1")
                                .teamId("1")
                                .tableOrBreakoutRoom("1")
                                .requestTime(ldt1)
                                .explanation("1")
                                .solved(true)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                HelpRequest req2 = HelpRequest.builder()
                                .requesterEmail("2")
                                .teamId("2")
                                .tableOrBreakoutRoom("2")
                                .requestTime(ldt2)
                                .explanation("2")
                                .solved(false)
                                .build();

                ArrayList<HelpRequest> expectedHelpRequests = new ArrayList<>();
                expectedHelpRequests.addAll(Arrays.asList(req1, req2));

                when(helpRequestRepository.findAll()).thenReturn(expectedHelpRequests);

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedHelpRequests);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/helprequest/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/helprequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/helprequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_help_request() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest req1 = HelpRequest.builder()
                                .requesterEmail("1")
                                .teamId("1")
                                .tableOrBreakoutRoom("1")
                                .requestTime(ldt1)
                                .explanation("1")
                                .solved(true)
                                .build();

                when(helpRequestRepository.save(eq(req1))).thenReturn(req1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/helprequest/post?requesterEmail=1&teamId=1&tableOrBreakoutRoom=1&requestTime=2022-01-03T00:00:00&explanation=1&solved=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).save(req1);
                String expectedJson = mapper.writeValueAsString(req1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/helprequest?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/helprequest?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest req1 = HelpRequest.builder()
                                .requesterEmail("1")
                                .teamId("1")
                                .tableOrBreakoutRoom("1")
                                .requestTime(ldt)
                                .explanation("1")
                                .solved(true)
                                .build();

                when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(req1));

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequest?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(req1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequest?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("HelpRequest with id 7 not found", json.get("message"));
        }


        // Tests for DELETE /api/helprequest?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest req1 = HelpRequest.builder()
                                .requesterEmail("1")
                                .teamId("1")
                                .tableOrBreakoutRoom("1")
                                .requestTime(ldt1)
                                .explanation("1")
                                .solved(true)
                                .build();

                when(helpRequestRepository.findById(eq(15L))).thenReturn(Optional.of(req1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/helprequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(15L);
                verify(helpRequestRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Help Request with id 15 deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_help_request_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(helpRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/helprequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("HelpRequest with id 15 not found", json.get("message"));
        }

        // Tests for PUT /api/helprequest?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_help_request() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest req1 = HelpRequest.builder()
                                .requesterEmail("1")
                                .teamId("1")
                                .tableOrBreakoutRoom("1")
                                .requestTime(ldt1)
                                .explanation("1")
                                .solved(true)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                HelpRequest req2 = HelpRequest.builder()
                                .requesterEmail("2")
                                .teamId("2")
                                .tableOrBreakoutRoom("2")
                                .requestTime(ldt2)
                                .explanation("2")
                                .solved(false)
                                .build();
                                
                String requestBody = mapper.writeValueAsString(req2);

                when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.of(req1));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/helprequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(67L);
                verify(helpRequestRepository, times(1)).save(req2); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_help_request_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest req1 = HelpRequest.builder()
                                .requesterEmail("1")
                                .teamId("1")
                                .tableOrBreakoutRoom("1")
                                .requestTime(ldt1)
                                .explanation("1")
                                .solved(true)
                                .build();

                String requestBody = mapper.writeValueAsString(req1);

                when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/helprequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("HelpRequest with id 67 not found", json.get("message"));

        }
}
