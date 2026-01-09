package com.ravi.hogwartsartifact.hogwartsuser;

import com.ravi.hogwartsartifact.hogwartsuser.dto.UserDto;
import com.ravi.hogwartsartifact.system.StatusCode;
import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    List<HogwartsUser> users;

    @Value("${api.endpoints.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {
        this.users = new ArrayList<>();

        HogwartsUser u1 = new HogwartsUser();
        u1.setId(1);
        u1.setUserName("john");
        u1.setPassword("123456");
        u1.setEnabled(true);
        u1.setRole("admin user");
        this.users.add(u1);

        HogwartsUser u2 = new HogwartsUser();
        u2.setId(2);
        u2.setUserName("eric");
        u2.setPassword("654321");
        u2.setEnabled(true);
        u2.setRole("user");
        this.users.add(u2);

        HogwartsUser u3 = new HogwartsUser();
        u3.setId(3);
        u3.setUserName("tom");
        u3.setPassword("qwerty");
        u3.setEnabled(false);
        u3.setRole("user");
        this.users.add(u3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findAllUser() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0,20);
        PageImpl<HogwartsUser> userPage = new PageImpl<>(this.users,pageable,this.users.size());
        given(this.userService.findAll(pageable)).willReturn(userPage);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(this.users.size())))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("john"))
                .andExpect(jsonPath("$.data.content[1].id").value(2))
                .andExpect(jsonPath("$.data.content[1].name").value("eric"));
    }

    @Test
    void testFindUserByIdSuccess() throws Exception {
        //given
        given(this.userService.findByUserId(2)).willReturn(this.users.get(1));
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/users/2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("eric"));

    }

    @Test
    void testFindUserByIdNotFound() throws Exception {
        //given
        given(this.userService.findByUserId(5)).willThrow(new ObjectNotFoundException("user", 5));
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/users/5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAddUserSuccess() throws Exception {
        //given
        HogwartsUser user = new HogwartsUser();
        user.setId(4);
        user.setUserName("lily");
        user.setPassword("123456");
        user.setEnabled(true);
        user.setRole("admin user");
        String json = this.objectMapper.writeValueAsString(user);

        given(this.userService.save(Mockito.any(HogwartsUser.class))).willReturn(user);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("lily"))
                .andExpect(jsonPath("$.data.isEnabled").value(true))
                .andExpect(jsonPath("$.data.role").value("admin user"));
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        UserDto userDto = new UserDto(3, "tom123", false, "user");
        HogwartsUser updatedUser = new HogwartsUser();
        updatedUser.setId(3);
        updatedUser.setUserName("tom123"); // Username is changed. It was tom.
        updatedUser.setEnabled(false);
        updatedUser.setRole("user");

        String json = this.objectMapper.writeValueAsString(userDto);
        //given
        given(this.userService.updateUser(eq(3), Mockito.any(HogwartsUser.class))).willReturn(updatedUser);
        // When and then
        this.mockMvc.perform(MockMvcRequestBuilders.put(this.baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.name").value("tom123"))
                .andExpect(jsonPath("$.data.isEnabled").value(false))
                .andExpect(jsonPath("$.data.role").value("user"));
    }

    @Test
    void testUpdateUserErrorWithNonExistentId() throws Exception {
        //given
        given(this.userService.updateUser(eq(5), Mockito.any(HogwartsUser.class)))
                .willThrow(new ObjectNotFoundException("user", 5));

        UserDto userDto = new UserDto(5, "tom123", false, "user");

        String json = this.objectMapper.writeValueAsString(userDto);

        // When and then
        this.mockMvc.perform(MockMvcRequestBuilders.put(this.baseUrl + "/users/5")
                        .contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteUserSuccess() throws Exception {
        //given
        doNothing().when(this.userService).deleteUser(2);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.delete(this.baseUrl+"/users/2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"));

    }

    @Test
    void testDeleteUserErrorWithNonExistentId() throws Exception {
        //given
        doThrow(new ObjectNotFoundException("user",5)).when(this.userService).deleteUser(5);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.delete(this.baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}