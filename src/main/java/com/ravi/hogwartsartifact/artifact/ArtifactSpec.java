package com.ravi.hogwartsartifact.artifact;

import org.springframework.data.jpa.domain.Specification;

public class ArtifactSpec {

    public static Specification<Artifact> hasId(String providedId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"), providedId);
    }

    public static Specification<Artifact> containsName(String providedNane) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + providedNane.toLowerCase() + "%");
    }

    public static Specification<Artifact> containsDescription(String providedDescription){
        return (root,query,criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),"%"+providedDescription.toLowerCase()+"%");
    }

    public static Specification<Artifact> hasOwner(String providedOwner){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("owner").get("name")),providedOwner.toLowerCase());
    }
}
