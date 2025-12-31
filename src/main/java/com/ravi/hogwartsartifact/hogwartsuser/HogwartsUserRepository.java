package com.ravi.hogwartsartifact.hogwartsuser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HogwartsUserRepository extends JpaRepository<HogwartsUser, Integer> {

    Optional<HogwartsUser> findByUserName(String userName);


}