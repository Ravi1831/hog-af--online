package com.ravi.hogwartsartifact.artifact;

import com.ravi.hogwartsartifact.artifact.utils.IdWorker;
import com.ravi.hogwartsartifact.system.ExceptionConstants;
import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final IdWorker idWorker;

    public ArtifactService(ArtifactRepository artifactRepository, IdWorker idWorker) {
        this.artifactRepository = artifactRepository;
        this.idWorker = idWorker;
    }


    public Artifact findById(String artifactId) {
        return this.artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.ARTIFACT,artifactId));
    }

    @Timed("findAllArtifactService.time")
    public List<Artifact> findAll() {
        return this.artifactRepository.findAll();
    }

    public Artifact save(Artifact newArtifact) {
        newArtifact.setId(String.valueOf(idWorker.nextId()));
        return this.artifactRepository.save(newArtifact);
    }

    public Artifact update(String artifactId, Artifact update) {
        return this.artifactRepository.findById(artifactId)
                .map(oldArtifacts -> {
                    oldArtifacts.setName(update.getName());
                    oldArtifacts.setDescription(update.getDescription());
                    oldArtifacts.setImageUrl(update.getImageUrl());
                    return this.artifactRepository.save(oldArtifacts);
                })
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.ARTIFACT,artifactId));
    }

    public void delete(String artifactId){
        this.artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.ARTIFACT,artifactId));
        this.artifactRepository.deleteById(artifactId);
    }

}
