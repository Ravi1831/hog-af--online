package com.ravi.hogwartsartifact.hogwartsuser;

import com.ravi.hogwartsartifact.hogwartsuser.convertor.UserDtoToUserConverter;
import com.ravi.hogwartsartifact.hogwartsuser.convertor.UserToUserDtoConverter;
import com.ravi.hogwartsartifact.hogwartsuser.dto.UserDto;
import com.ravi.hogwartsartifact.system.Result;
import com.ravi.hogwartsartifact.system.StatusCode;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.endpoints.base-url}/users")
public class UserController {

    private final UserService userService;
    private final UserToUserDtoConverter userToUserDtoConverter;
    private final UserDtoToUserConverter userDtoToUserConverter;

    Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, UserToUserDtoConverter userToUserDtoConverter, UserDtoToUserConverter userDtoToUserConverter) {
        this.userService = userService;
        this.userToUserDtoConverter = userToUserDtoConverter;
        this.userDtoToUserConverter = userDtoToUserConverter;
    }

    @GetMapping
   public Result findAllUser(Pageable pageable){
        Page<HogwartsUser> foundHogwartsUsersPage = this.userService.findAll(pageable);
        Page<UserDto> userDtoPage = foundHogwartsUsersPage.map(userToUserDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find All Success",userDtoPage);
   }

   @GetMapping("/{userId}")
   public Result findByUserId(@PathVariable Integer userId){
       HogwartsUser foundUser = this.userService.findByUserId(userId);
       UserDto userDto = this.userToUserDtoConverter.convert(foundUser);
       return new Result(true,StatusCode.SUCCESS,"Find One Success",userDto);
   }

   @PostMapping
   public Result addUser(
           @RequestBody @Valid HogwartsUser newHogwartsUser){
       HogwartsUser hogwartsUserFound = this.userService.save(newHogwartsUser);
       UserDto userDtoFound = this.userToUserDtoConverter.convert(hogwartsUserFound);
       logger.info("userDto result {}",userDtoFound);
       return new Result(true,StatusCode.SUCCESS,"Add Success",userDtoFound);
   }

   @PutMapping("/{userId}")
   public Result updateUser(@PathVariable Integer userId,@RequestBody @Valid UserDto userDto){
       HogwartsUser update = this.userDtoToUserConverter.convert(userDto);
       HogwartsUser updatedHogwardsUsers = this.userService.updateUser(userId, update);
       UserDto updatedUserDto = this.userToUserDtoConverter.convert(updatedHogwardsUsers);
       return new Result(true,StatusCode.SUCCESS,"Update Success",updatedUserDto);
   }

   @DeleteMapping("/{userId}")
   public Result  deleteUser(@PathVariable Integer userId){
        this.userService.deleteUser(userId);
        return new Result(true,StatusCode.SUCCESS,"Delete Success");
   }

   @PatchMapping("/{userId}/password")
   public Result changePassword(@PathVariable Integer userId, @RequestBody Map<String, String> passwordMap){
       String oldPassword = passwordMap.get("oldPassword");
       String newPassword = passwordMap.get("newPassword");
       String confirmNewPassword = passwordMap.get("confirmNewPassword");
       this.userService.changePassword(userId,oldPassword,newPassword,confirmNewPassword);
       return new Result(true,StatusCode.SUCCESS,"Change Password Success",null);
   }
}
