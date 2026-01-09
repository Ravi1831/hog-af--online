package com.ravi.hogwartsartifact.wizard;

import com.ravi.hogwartsartifact.artifact.Artifact;
import com.ravi.hogwartsartifact.artifact.ArtifactRepository;
import com.ravi.hogwartsartifact.artifact.utils.IdWorker;
import com.ravi.hogwartsartifact.system.ExceptionConstants;
import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WizardService {

    private final WizardRepository wizardRepository;
    private final ArtifactRepository artifactRepository;
    private final IdWorker idWorker;

    public WizardService(WizardRepository wizardRepository, ArtifactRepository artifactRepository, IdWorker idWorker) {
        this.wizardRepository = wizardRepository;
        this.artifactRepository = artifactRepository;
        this.idWorker = idWorker;
    }

    public Page<Wizard> findAll(Pageable pageable){
        return this.wizardRepository.findAll(pageable);
    }

    public Wizard save(Wizard wizard){
        return this.wizardRepository.save(wizard);
    }


    public Wizard findById(Integer wizardId){
        return this.wizardRepository.findById(wizardId)
                .orElseThrow(()-> new ObjectNotFoundException(ExceptionConstants.WIZARD,wizardId));
    }

    public Wizard updateWizard(Wizard wizard, Integer wizardId){
        this.wizardRepository.findById(wizardId).orElseThrow(
                ()-> new ObjectNotFoundException(ExceptionConstants.WIZARD,wizardId));
        return this.wizardRepository.save(wizard);
    }

    public void deleteWizard(Integer wizardId){
        this.wizardRepository.findById(wizardId)
                .orElseThrow(()-> new ObjectNotFoundException(ExceptionConstants.WIZARD,wizardId));
        this.wizardRepository.deleteById(wizardId);
    }

    public void assignArtifact(Integer wizardId,String artifactId){
        Artifact artifactToBeAssigned = this.artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.ARTIFACT,artifactId));
        Wizard wizard = this.wizardRepository.findById(wizardId)
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.WIZARD,wizardId));
        if (artifactToBeAssigned.getOwner() != null){
            artifactToBeAssigned.getOwner().removeArtifact(artifactToBeAssigned);
        }
        wizard.addArtifact(artifactToBeAssigned);
        this.wizardRepository.save(wizard);
    }
}
