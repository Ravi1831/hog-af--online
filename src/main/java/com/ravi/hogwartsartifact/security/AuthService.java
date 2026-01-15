package com.ravi.hogwartsartifact.security;

import com.ravi.hogwartsartifact.client.redisClient.RedisCacheClient;
import com.ravi.hogwartsartifact.hogwartsuser.HogwartsUser;
import com.ravi.hogwartsartifact.hogwartsuser.MyUserPrincipal;
import com.ravi.hogwartsartifact.hogwartsuser.convertor.UserToUserDtoConverter;
import com.ravi.hogwartsartifact.hogwartsuser.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ravi.hogwartsartifact.system.constant.RedisConstant.WHITELIST_USER;

@Service
public class AuthService {

    private final JwtProviders jwtProviders;
    private final UserToUserDtoConverter userToUserDtoConverter;
    private final RedisCacheClient redisCacheClient;

    public AuthService(JwtProviders jwtProviders, UserToUserDtoConverter userToUserDtoConverter, RedisCacheClient redisCacheClient) {
        this.jwtProviders = jwtProviders;
        this.userToUserDtoConverter = userToUserDtoConverter;
        this.redisCacheClient = redisCacheClient;
    }

    public Map<String,Object> createLoginInfo(Authentication authentication) {
        //create user info
        MyUserPrincipal principal = (MyUserPrincipal) authentication.getPrincipal();
        HogwartsUser hogwartsUser = principal.getHogwartsUser();
        UserDto userDto = this.userToUserDtoConverter.convert(hogwartsUser);
        //create a JWT
        String token = this.jwtProviders.createToken(authentication);

        //save token in redis, key is "whitelist:"{userId},value is token
        redisCacheClient.set(WHITELIST_USER+":"+hogwartsUser.getId(),token,2, TimeUnit.HOURS);
        Map<String,Object> loginResultMap = new HashMap<>();
        loginResultMap.put("userInfo",userDto);
        loginResultMap.put("token",token);
        return loginResultMap;
    }
}
