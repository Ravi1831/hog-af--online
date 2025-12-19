package com.ravi.hogwartsartifact.wizard.convertor;

import com.ravi.hogwartsartifact.wizard.Wizard;
import com.ravi.hogwartsartifact.wizard.dto.WizardDto;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WizardToWizardDtoConvertor implements Converter<Wizard, WizardDto> {
    @Override
    public @Nullable WizardDto convert(Wizard source) {
        return new WizardDto(
                source.getId(),
                source.getName(),
                source.getNumberOfArtifacts()
        );
    }
}
