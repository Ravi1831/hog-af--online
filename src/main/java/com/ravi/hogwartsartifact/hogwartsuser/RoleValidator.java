package com.ravi.hogwartsartifact.hogwartsuser;

import com.ravi.hogwartsartifact.hogwartsuser.dto.enums.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class RoleValidator implements ConstraintValidator<ValidRole,String> {
    @Override
    public boolean isValid(String roleInput, ConstraintValidatorContext context) {
        if(roleInput == null || roleInput.isEmpty()){
            return false;
        }
        String[] roles = roleInput.split("\\s+");

        return Arrays.stream(roles)
                .allMatch(inputRole ->
                        Arrays.stream(Role.values())
                                .anyMatch(enumRole-> enumRole.name().equals(inputRole)));

    }
}
