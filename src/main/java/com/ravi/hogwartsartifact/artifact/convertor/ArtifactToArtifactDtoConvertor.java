package com.ravi.hogwartsartifact.artifact.convertor;

import com.ravi.hogwartsartifact.artifact.Artifact;
import com.ravi.hogwartsartifact.artifact.dto.ArtifactDto;
import com.ravi.hogwartsartifact.wizard.convertor.WizardToWizardDtoConvertor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
public class ArtifactToArtifactDtoConvertor implements Converter<Artifact, ArtifactDto> {

    private final WizardToWizardDtoConvertor wizardToWizardDtoConvertor;

    public ArtifactToArtifactDtoConvertor(WizardToWizardDtoConvertor wizardToWizardDtoConvertor) {
        this.wizardToWizardDtoConvertor = wizardToWizardDtoConvertor;
    }

    @Override
    public @Nullable ArtifactDto convert(Artifact source) {
        return new ArtifactDto(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getImageUrl(),
                source.getOwner()!=null ?
                        wizardToWizardDtoConvertor.convert(source.getOwner())
                        : null
        );
    }
}
