package com.ravi.hogwartsartifact.hogwartsuser;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import java.util.Arrays;
import java.util.Collection;

public class MyUserPrincipal implements UserDetails {

    private final HogwartsUser hogwartsUser;

    public MyUserPrincipal(HogwartsUser hogwartsUser) {
        this.hogwartsUser = hogwartsUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String[] rolesArray = StringUtils.tokenizeToStringArray(this.hogwartsUser.getRole(), " ");
        return Arrays.stream(rolesArray)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @Override
    public @Nullable String getPassword() {
        return this.hogwartsUser.getPassword();
    }

    @Override
    public String getUsername() {
        return this.hogwartsUser.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.hogwartsUser.isEnabled();
    }

    public HogwartsUser getHogwartsUser() {
        return hogwartsUser;
    }
}
