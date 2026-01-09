package com.ravi.hogwartsartifact.hogwartsuser;

import com.ravi.hogwartsartifact.system.StatusCode;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for User API endpoints")
@Tag("integration")
@Transactional
@Rollback
public class HogwartsUserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String token;

    @Value("${api.endpoints.base-url}")
    String baseUrl;

    @BeforeEach
    void setup() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("john", "123456")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.token = "Bearer "+json.getJSONObject("data").getString("token");
    }

    @Test
    @DisplayName("Check findAllUsers (GET)")
    void testFindAllUsersSuccess() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Check findUserById (GET): User with ROLE_user Accessing Another Users Info")
    void testFindUserByIdWithUserAccessingAnotherUsersInfo() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        this.mockMvc.perform(get(this.baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DisplayName("Check findUserById with non-existent id (GET)")
    void testFindUserByIdNotFound() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check addUser with valid input (POST)")
    void testAddUserSuccess() throws Exception {
        HogwartsUser hogwartsUser = new HogwartsUser();
        hogwartsUser.setUserName("lily");
        hogwartsUser.setPassword("123456");
        hogwartsUser.setEnabled(true);
        hogwartsUser.setRole("admin user"); // The delimiter is space.

        String json = this.objectMapper.writeValueAsString(hogwartsUser);

        this.mockMvc.perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("lily"))
                .andExpect(jsonPath("$.data.isEnabled").value(true))
                .andExpect(jsonPath("$.data.role").value("admin user"));
        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(4)));
    }

    @Test
    @DisplayName("Check addUser with invalid input (POST)")
    void testAddUserErrorWithInvalidInput() throws Exception {
        HogwartsUser hogwartsUser = new HogwartsUser();
        hogwartsUser.setUserName(""); // Username is not provided.
        hogwartsUser.setPassword(""); // Password is not provided.
        hogwartsUser.setRole(""); // Roles field is not provided.

        String json = this.objectMapper.writeValueAsString(hogwartsUser);

        this.mockMvc.perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided Argument are invalid, see data for details"))
                .andExpect(jsonPath("$.data.userName").value("userName is not required"))
                .andExpect(jsonPath("$.data.password").value("password is required"))
                .andExpect(jsonPath("$.data.role").value("role is required"));
        this.mockMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Check updateUser with valid input (PUT)")
    void testUpdateUserWithAdminUpdatingAnyUsersInfo() throws Exception {
        Map<String, Object> userDtoMap = new HashMap<>();
        userDtoMap.put("name", "tom123"); // Username is changed. It was tom.
        userDtoMap.put("isEnabled", false);
        userDtoMap.put("role", "user");

        String json = this.objectMapper.writeValueAsString(userDtoMap);

        this.mockMvc.perform(put(this.baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.name").value("tom123"))
                .andExpect(jsonPath("$.data.isEnabled").value(false))
                .andExpect(jsonPath("$.data.role").value("user"));
    }

    @Test
    @DisplayName("Check updateUser with non-existent id (PUT)")
    void testUpdateUserErrorWithNonExistentId() throws Exception {
        Map<String, Object> userDtoMap = new HashMap<>();
        userDtoMap.put("id", 5); // This id does not exist in the database.
        userDtoMap.put("name", "john123"); // Username is changed.
        userDtoMap.put("isEnabled", true);
        userDtoMap.put("role", "admin user");

        String json = this.objectMapper.writeValueAsString(userDtoMap);

        this.mockMvc.perform(put(this.baseUrl + "/users/5").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

     @Test
     @DisplayName("Check updateUser with invalid input (PUT)")
     void testUpdateUserErrorWithInvalidInput() throws Exception {
         Map<String, Object> userDtoMap = new HashMap<>();
         userDtoMap.put("id", 1); // Valid id
         userDtoMap.put("name", ""); // Updated username is empty.
         userDtoMap.put("isEnabled", true); // Valid enabled value
         userDtoMap.put("role", ""); // Updated roles field is empty.

         String json = this.objectMapper.writeValueAsString(userDtoMap);

         this.mockMvc.perform(put(this.baseUrl + "/users/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                 .andExpect(jsonPath("$.flag").value(false))
                 .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                 .andExpect(jsonPath("$.message").value("Provided Argument are invalid, see data for details"))
                 .andExpect(jsonPath("$.data.name").value("username is required"))
                 .andExpect(jsonPath("$.data.role").value("role is required"));
         this.mockMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                 .andExpect(jsonPath("$.flag").value(true))
                 .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                 .andExpect(jsonPath("$.message").value("Find One Success"))
                 .andExpect(jsonPath("$.data.id").value(1))
                 .andExpect(jsonPath("$.data.name").value("john"));
     }

     @Test
     @DisplayName("Check updateUser with valid input (PUT): User with ROLE_user Updating Own Info")
     void testUpdateUserWithUserUpdatingOwnInfo() throws Exception {
         ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
         MvcResult mvcResult = resultActions.andDo(print()).andReturn();
         String contentAsString = mvcResult.getResponse().getContentAsString();
         JSONObject json = new JSONObject(contentAsString);
         String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

         Map<String, Object> userDtoMap = new HashMap<>();
         userDtoMap.put("name", "eric123"); // Username is changed. It was eric.
         userDtoMap.put("isEnabled", true);
         userDtoMap.put("role", "user");

         String hogwartsUserJson = this.objectMapper.writeValueAsString(userDtoMap);
         this.mockMvc.perform(put(this.baseUrl + "/users/2")
                         .contentType(MediaType.APPLICATION_JSON).content(hogwartsUserJson)
                         .accept(MediaType.APPLICATION_JSON)
                         .header(HttpHeaders.AUTHORIZATION, ericToken))
                 .andExpect(jsonPath("$.flag").value(false))
                 .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                 .andExpect(jsonPath("$.message").value("No permission."))
                 .andExpect(jsonPath("$.data").value("Access Denied"));
     }

    @Test
    @DisplayName("Check updateUser with valid input (PUT): User with ROLE_user Updating Another Users Info")
    void testUpdateUserWithUserUpdatingAnotherUsersInfo() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        Map<String, Object> userDtoMap = new HashMap<>();
        userDtoMap.put("name", "tom123"); // Username is changed. It was tom.
        userDtoMap.put("isEnabled", false);
        userDtoMap.put("role", "user");

        String hogwartsUserJson = this.objectMapper.writeValueAsString(userDtoMap);

        this.mockMvc.perform(put(this.baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(hogwartsUserJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DisplayName("Check deleteUser with valid input (DELETE)")
    void testDeleteUserSuccess() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"))
                .andExpect(jsonPath("$.data").isEmpty());
        this.mockMvc.perform(get(this.baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with id 2 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check deleteUser with non-existent id (DELETE)")
    void testDeleteUserErrorWithNonExistentId() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check deleteUser with insufficient permission (DELETE)")
    void testDeleteUserNoAccessAsRoleUser() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        this.mockMvc.perform(delete(this.baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("john"));
    }

}
