package com.ravi.hogwartsartifact.security;

import com.ravi.hogwartsartifact.hogwartsuser.HogwartsUser;
import com.ravi.hogwartsartifact.hogwartsuser.MyUserPrincipal;
import com.ravi.hogwartsartifact.hogwartsuser.convertor.UserToUserDtoConverter;
import com.ravi.hogwartsartifact.hogwartsuser.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final JwtProviders jwtProviders;
    private final UserToUserDtoConverter userToUserDtoConverter;

    public AuthService(JwtProviders jwtProviders, UserToUserDtoConverter userToUserDtoConverter) {
        this.jwtProviders = jwtProviders;
        this.userToUserDtoConverter = userToUserDtoConverter;
    }

    public Map<String,Object> createLoginInfo(Authentication authentication) {
        //create user info
        MyUserPrincipal principal = (MyUserPrincipal) authentication.getPrincipal();
        HogwartsUser hogwartsUser = principal.getHogwartsUser();
        UserDto userDto = this.userToUserDtoConverter.convert(hogwartsUser);
        //create a JWT
        String token = this.jwtProviders.createToken(authentication);
        Map<String,Object> loginResultMap = new HashMap<>();
        loginResultMap.put("userInfo",userDto);
        loginResultMap.put("token",token);
        return loginResultMap;
    }
}
