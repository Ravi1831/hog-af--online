package com.ravi.hogwartsartifact.hogwartsuser.convertor;

import com.ravi.hogwartsartifact.hogwartsuser.HogwartsUser;
import com.ravi.hogwartsartifact.hogwartsuser.dto.UserDto;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserDtoConverter implements Converter<HogwartsUser, UserDto> {

    @Override
    public UserDto convert(HogwartsUser source) {
       return new UserDto(source.getId(),
                source.getUserName(),
                source.isEnabled(),
                source.getRole());
    }
}
