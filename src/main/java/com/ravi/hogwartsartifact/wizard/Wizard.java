package com.ravi.hogwartsartifact.wizard;

import com.ravi.hogwartsartifact.artifact.Artifact;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Wizard implements Serializable {


    @Id
    @SequenceGenerator(
            name = "wizard_seq",
            allocationSize =  1,
            sequenceName = "wizard_sequence",
            initialValue = 1
    )
    @GeneratedValue(strategy = GenerationType.AUTO,generator = "wizard_seq")
    private Integer id;

    private String name;

    @OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE}, mappedBy = "owner")
    private List<Artifact> artifacts = new ArrayList<>();

    public Wizard() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public void addArtifact(Artifact artifact){
        artifact.setOwner(this);
        this.artifacts.add(artifact);
    }

    @Override
    public String toString() {
        return "Wizard{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", numberOfArtifacts=" + (artifacts != null ? artifacts.size() : 0) +
                '}';
    }

    public Integer getNumberOfArtifacts() {
        return  this.artifacts.size();
    }

    public void removeArtifact(Artifact artifactToBeAssigned){
        //remove artifact owner
        artifactToBeAssigned.setOwner(null);
        this.artifacts.remove(artifactToBeAssigned);
    }
}
