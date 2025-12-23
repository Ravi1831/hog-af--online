package com.ravi.hogwartsartifact.wizard.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record WizardDto(Integer id,
                        @Size(min = 3, message = "min required length is 3")
                        String name,
                        Integer numberOfArtifacts) {
}
