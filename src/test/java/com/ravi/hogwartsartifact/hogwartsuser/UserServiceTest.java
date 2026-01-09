package com.ravi.hogwartsartifact.hogwartsuser;

import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    HogwartsUserRepository hogwartsUserRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    List<HogwartsUser> hogwartsUsers;
    @BeforeEach
    void setUp() {
        HogwartsUser u1 = new HogwartsUser();
        u1.setId(1);
        u1.setUserName("john");
        u1.setPassword("123456");
        u1.setEnabled(true);
        u1.setRole("admin user");

        HogwartsUser u2 = new HogwartsUser();
        u2.setId(2);
        u2.setUserName("eric");
        u2.setPassword("654321");
        u2.setEnabled(true);
        u2.setRole("user");

        HogwartsUser u3 = new HogwartsUser();
        u3.setId(3);
        u3.setUserName("tom");
        u3.setPassword("qwerty");
        u3.setEnabled(false);
        u3.setRole("user");

        this.hogwartsUsers = new ArrayList<>();
        this.hogwartsUsers.add(u1);
        this.hogwartsUsers.add(u2);
        this.hogwartsUsers.add(u3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findAll() {
        //given
        Pageable pageable = PageRequest.of(0,20);
        PageImpl<HogwartsUser> userPage = new PageImpl<>(this.hogwartsUsers,pageable,this.hogwartsUsers.size());
        given(this.hogwartsUserRepository.findAll(pageable)).willReturn(userPage);
        //when
        Page<HogwartsUser> actualUsers = this.userService.findAll(pageable);
        //then
        assertThat(actualUsers.getContent()).isEqualTo(hogwartsUsers);
        assertThat(actualUsers.getTotalElements()).isEqualTo(hogwartsUsers.size());
        verify(this.hogwartsUserRepository,times(1)).findAll(pageable);
    }

    @Test
    void testFindByIdSuccess(){
        //given
        HogwartsUser u = new HogwartsUser();
        u.setId(1);
        u.setUserName("john");
        u.setPassword("123456");
        u.setEnabled(true);
        u.setRole("admin user");
        given(this.hogwartsUserRepository.findById(1)).willReturn(Optional.of(u));
        
        //when
        HogwartsUser foundUser = this.userService.findByUserId(1);
        //then
        assertThat(foundUser.getId()).isEqualTo(u.getId());
        assertThat(foundUser.getUserName()).isEqualTo(u.getUserName());
        assertThat(foundUser.getPassword()).isEqualTo(u.getPassword());
        assertThat(foundUser.isEnabled()).isEqualTo(u.isEnabled());
        assertThat(foundUser.getRole()).isEqualTo(u.getRole());
        verify(this.hogwartsUserRepository, times(1)).findById(1);
    }


    @Test
    void testFindByIdNotFound(){
        //given
        given(this.hogwartsUserRepository.findById(Mockito.anyInt()))
                .willReturn(Optional.empty());

        Throwable throwable = catchThrowable(()->{
            this.userService.findByUserId(10);
        });
        //then
        assertThat(throwable).isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find user with id 10 :(");

        verify(hogwartsUserRepository,times(1)).findById(10);
    }

    @Test
    void testSaveSuccess(){
        // Given
        HogwartsUser newUser = new HogwartsUser();
        newUser.setUserName("lily");
        newUser.setPassword("123456");
        newUser.setEnabled(true);
        newUser.setRole("user");
        String encodedPassword = "encoded123456";
        given(this.passwordEncoder.encode("123456")).willReturn(encodedPassword);
        given(this.hogwartsUserRepository.save(newUser)).willReturn(newUser);
        //when
        HogwartsUser returnedUser = this.userService.save(newUser);
        //then
        assertThat(returnedUser.getUserName()).isEqualTo(newUser.getUserName());
        assertThat(returnedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(returnedUser.isEnabled()).isEqualTo(newUser.isEnabled());
        assertThat(returnedUser.getRole()).isEqualTo(newUser.getRole());
        verify(this.passwordEncoder, times(1)).encode("123456");
        verify(this.hogwartsUserRepository, times(1)).save(newUser);
    }

    @Test
    void testUpdateByAdminSuccess(){
// Given
        HogwartsUser oldUser = new HogwartsUser();
        oldUser.setId(2);
        oldUser.setUserName("eric");
        oldUser.setPassword("654321");
        oldUser.setEnabled(true);
        oldUser.setRole("user");

        HogwartsUser update = new HogwartsUser();
        update.setUserName("eric - update");
        update.setPassword("654321");
        update.setEnabled(true);
        update.setRole("admin user");

        given(this.hogwartsUserRepository.findById(2)).willReturn(Optional.of(oldUser));
        given(this.hogwartsUserRepository.save(oldUser)).willReturn(oldUser);
        //when
        HogwartsUser updatedUser = this.userService.updateUser(2, update);
        //then
        assertThat(updatedUser.getId()).isEqualTo(2);
        assertThat(updatedUser.getUserName()).isEqualTo(update.getUserName());
        verify(this.hogwartsUserRepository, times(1)).findById(2);
        verify(this.hogwartsUserRepository, times(1)).save(oldUser);

    }

    @Test
    void testUpdateNotFound(){
        // Given
        HogwartsUser update = new HogwartsUser();
        update.setUserName("john - update");
        update.setPassword("123456");
        update.setEnabled(true);
        update.setRole("admin user");

        given(this.hogwartsUserRepository.findById(1)).willReturn(Optional.empty());
        //when
        Throwable throwable = assertThrows(ObjectNotFoundException.class,()->{
            this.userService.updateUser(1,update);
        });
        //then
        assertThat(throwable)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find user with id 1 :(");
        verify(this.hogwartsUserRepository,times(1)).findById(1);
    }

    @Test
    void testDeleteSuccess(){
        // Given
        HogwartsUser user = new HogwartsUser();
        user.setId(1);
        user.setUserName("john");
        user.setPassword("123456");
        user.setEnabled(true);
        user.setRole("admin user");

        given(this.hogwartsUserRepository.findById(1)).willReturn(Optional.of(user));
        doNothing().when(this.hogwartsUserRepository).deleteById(1);
        //when
        this.userService.deleteUser(1);
        //then
        verify(this.hogwartsUserRepository,times(1)).deleteById(1);
    }

    @Test
    void testDeleteNotFound(){
        //given
        given(this.hogwartsUserRepository.findById(1)).willReturn(Optional.empty());
        //when
        Throwable throwable = assertThrows(ObjectNotFoundException.class,()->{
            this.userService.deleteUser(1);
        });
        //then
        assertThat(throwable)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find user with id 1 :(");
        verify(this.hogwartsUserRepository,times(1)).findById(1);
    }



}