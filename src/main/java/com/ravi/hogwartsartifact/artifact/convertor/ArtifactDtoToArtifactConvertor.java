package com.ravi.hogwartsartifact.artifact.convertor;

import com.ravi.hogwartsartifact.artifact.Artifact;
import com.ravi.hogwartsartifact.artifact.dto.ArtifactDto;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ArtifactDtoToArtifactConvertor implements Converter<ArtifactDto, Artifact> {
    @Override
    public @Nullable Artifact convert(ArtifactDto source) {
        Artifact artifact = new Artifact();
        artifact.setId(source.id());
        artifact.setName(source.name());
        artifact.setDescription(source.description());
        artifact.setImageUrl(source.imageUrl());
        return artifact;
    }
}
