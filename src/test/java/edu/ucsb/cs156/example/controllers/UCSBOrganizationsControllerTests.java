package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;

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


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase { 

        @MockBean
        UCSBOrganizationsRepository ucsbOrgsRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/UCSBOrganizations/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborgs() throws Exception {

                // arrange

                UCSBOrganizations ucsbOrg1 = UCSBOrganizations.builder()
                                .orgCode("1234")
                                .orgTranslationShort("HOB")
                                .orgTranslation("House of Blues")
                                .inactive(false)
                                .build();


                UCSBOrganizations ucsbOrg2 = UCSBOrganizations.builder()
                .orgCode("5678")
                .orgTranslationShort("COF")
                .orgTranslation("Cards of Fate")
                .inactive(true)
                .build();

                ArrayList<UCSBOrganizations> expectedOrgs = new ArrayList<>();
                expectedOrgs.addAll(Arrays.asList(ucsbOrg1, ucsbOrg2));

                when(ucsbOrgsRepository.findAll()).thenReturn(expectedOrgs);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrgsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrgs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/UCSBOrganizations/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganizations/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganizations/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsborg() throws Exception {
                // arrange

                UCSBOrganizations ucsbOrg1 = UCSBOrganizations.builder()
                                .orgCode("1234")
                                .orgTranslationShort("HOB")
                                .orgTranslation("House of Blues")
                                .inactive(true)
                                .build();

                when(ucsbOrgsRepository.save(eq(ucsbOrg1))).thenReturn(ucsbOrg1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganizations/post?orgCode=1234&orgTranslationShort=HOB&orgTranslation=House of Blues&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrgsRepository, times(1)).save(ucsbOrg1);
                String expectedJson = mapper.writeValueAsString(ucsbOrg1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/UCSBOrganizations?orgCode=...

        @Test
        public void logged_out_users_cannot_get_by_orgCode() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations?orgCode=134"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_orgcode_when_the_org_exists() throws Exception {

                // arrange
                UCSBOrganizations ucsbOrg = UCSBOrganizations.builder()
                                .orgCode("234")
                                .orgTranslationShort("HOB")
                                .orgTranslation("House of Blues")
                                .inactive(false)
                                .build();

                when(ucsbOrgsRepository.findById(eq("234"))).thenReturn(Optional.of(ucsbOrg));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgCode=234"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrgsRepository, times(1)).findById(eq("234"));
                String expectedJson = mapper.writeValueAsString(ucsbOrg);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_orgcode_when_the_orgcode_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrgsRepository.findById(eq("124"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgCode=124"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrgsRepository, times(1)).findById(eq("124"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganizations with id 124 not found", json.get("message"));
        }


        // Tests for DELETE /api/ucsbdates?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_org() throws Exception {
                // arrange

                UCSBOrganizations ucsbOrg = UCSBOrganizations.builder()
                                .orgCode("123")
                                .orgTranslationShort("HOB")
                                .orgTranslation("House of Blues")
                                .inactive(false)
                                .build();

                when(ucsbOrgsRepository.findById(eq("123"))).thenReturn(Optional.of(ucsbOrg));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganizations?orgCode=123")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrgsRepository, times(1)).findById("123");
                verify(ucsbOrgsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrg with orgCode 123 deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsborg_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrgsRepository.findById(eq("457"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganizations?orgCode=457")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrgsRepository, times(1)).findById("457");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id 457 not found", json.get("message"));
        }

        // Tests for PUT /api/ucsbdates?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsborg() throws Exception {
                // arrange

                UCSBOrganizations ucsbOrgOrig = UCSBOrganizations.builder()
                                .orgCode("1123")
                                .orgTranslationShort("HOB")
                                .orgTranslation("House of Blues")
                                .inactive(false)
                                .build();


                UCSBOrganizations ucsbOrgEdited = UCSBOrganizations.builder()
                .orgCode("112")
                .orgTranslationShort("COF")
                .orgTranslation("Cards of Fate")
                .inactive(true)
                .build();

                String requestBody = mapper.writeValueAsString(ucsbOrgEdited);

                when(ucsbOrgsRepository.findById(eq("1123"))).thenReturn(Optional.of(ucsbOrgOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganizations?orgCode=1123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrgsRepository, times(1)).findById("1123");
                verify(ucsbOrgsRepository, times(1)).save(ucsbOrgEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsborg_that_does_not_exist() throws Exception {
                // arrange

                UCSBOrganizations ucsbOrgEdited = UCSBOrganizations.builder()
                .orgCode("1234")
                .orgTranslationShort("HOB")
                .orgTranslation("House of Blues")
                .inactive(false)
                .build();

                String requestBody = mapper.writeValueAsString(ucsbOrgEdited);

                when(ucsbOrgsRepository.findById(eq("1234"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganizations?orgCode=56781")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrgsRepository, times(1)).findById("56781");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id 56781 not found", json.get("message"));

        }
}
