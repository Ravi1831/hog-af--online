package com.ravi.hogwartsartifact.wizard.convertor;

import com.ravi.hogwartsartifact.wizard.Wizard;
import com.ravi.hogwartsartifact.wizard.dto.WizardDto;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WizardDtoToWizardConverter implements Converter<WizardDto, Wizard> {

    @Override
    public @Nullable Wizard convert(WizardDto source) {
        Wizard wizard = new Wizard();
        wizard.setId(source.id());
        wizard.setName(source.name());
        return wizard;
    }
}
