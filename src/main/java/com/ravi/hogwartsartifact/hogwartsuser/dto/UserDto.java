package com.ravi.hogwartsartifact.hogwartsuser.dto;

import jakarta.validation.constraints.NotEmpty;

public record UserDto(Integer id,
                      @NotEmpty(message = "username is required")
                      String name,
                      boolean isEnabled,
                      @NotEmpty(message = "role is required")
                      String role) {
}
