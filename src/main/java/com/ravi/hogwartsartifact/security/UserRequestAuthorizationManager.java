package com.ravi.hogwartsartifact.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;


@Component
public class UserRequestAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private static final UriTemplate USER_URI_TEMPLATE = new UriTemplate("/users/{userId}");
    @Override
    public @Nullable AuthorizationResult authorize(
            Supplier<? extends @Nullable Authentication> authenticationSupplier,
                                                   RequestAuthorizationContext context) {
        Map<String, String> uriVariables = USER_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        String userIdFromRequestUri = uriVariables.get("userId");
        //Extract userId from the authentication object which is in JWT Object
        Authentication authentication = authenticationSupplier.get();
        Assert.notNull(authentication,"authentication should not be null");
        String userIdFromJwt = ((Jwt) authentication.getPrincipal()).getClaim("userId").toString();
        //Check if the user has role "ROLE_user"
        boolean hasUserRole = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_user"));
        //Check if the user had role "ROLE_admin"
        boolean hasRoleAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"));
        //compare two userID
        boolean userIdsMatch = userIdFromRequestUri != null && userIdFromRequestUri.equals(userIdFromJwt);

        return new AuthorizationDecision(hasRoleAdmin || (hasUserRole && userIdsMatch));
    }
}
