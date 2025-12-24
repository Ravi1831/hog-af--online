package com.ravi.hogwartsartifact.hogwartsuser.convertor;

import com.ravi.hogwartsartifact.hogwartsuser.HogwartsUser;
import com.ravi.hogwartsartifact.hogwartsuser.dto.UserDto;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserDtoToUserConverter implements Converter<UserDto, HogwartsUser> {
    @Override
    public @Nullable HogwartsUser convert(UserDto source) {
        HogwartsUser hogwartsUser = new HogwartsUser();
        hogwartsUser.setUserName(source.name());
        hogwartsUser.setEnabled(source.isEnabled());
        hogwartsUser.setRole(source.role());
        return hogwartsUser;
    }
}
