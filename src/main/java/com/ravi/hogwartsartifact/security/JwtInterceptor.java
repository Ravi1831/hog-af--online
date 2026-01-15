package com.ravi.hogwartsartifact.security;

import com.ravi.hogwartsartifact.client.redisClient.RedisCacheClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final RedisCacheClient redisCacheClient;

    public JwtInterceptor(RedisCacheClient redisCacheClient) {
        this.redisCacheClient = redisCacheClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //get the token from the request header
        String authorizationHeader = request.getHeader("Authorization");
        //if the token is not null, and it starts with Bearer, then we need to verify if the token is present in the redis
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwt = (Jwt) authentication.getPrincipal();

            //Retrieve the user id from the JWWT claims and check if the token is in the redis whitelist or not
            String userId = jwt.getClaim("userId").toString();
            if(!this.redisCacheClient.isUserTokenInWhiteList(userId,jwt.getTokenValue())){
                throw new BadCredentialsException("Invalid token");
            }
        }
        //Else the request is just a public request that does not need the token E.g. Login, register
        return true;
    }
}
