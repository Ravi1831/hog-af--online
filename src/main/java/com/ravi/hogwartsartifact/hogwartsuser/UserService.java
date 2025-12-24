package com.ravi.hogwartsartifact.hogwartsuser;

import com.ravi.hogwartsartifact.system.ExceptionConstants;
import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final HogwartsUserRepository hogwartsUserRepository;

    public UserService(HogwartsUserRepository hogwartsUserRepository) {
        this.hogwartsUserRepository = hogwartsUserRepository;
    }

    public List<HogwartsUser> findAll(){
        return this.hogwartsUserRepository.findAll();
    }

    public HogwartsUser findByUserId(Integer userId){
        return this.hogwartsUserRepository.findById(userId)
                .orElseThrow(()-> new ObjectNotFoundException(ExceptionConstants.USER,userId));
    }

    public HogwartsUser save(HogwartsUser newHogwartsUser){
        return this.hogwartsUserRepository.save(newHogwartsUser);
    }

    public HogwartsUser updateUser(Integer userId, HogwartsUser hogwartsUser){
        HogwartsUser userToBeUpdated = this.hogwartsUserRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.USER, userId));
        userToBeUpdated.setUserName(hogwartsUser.getUserName());
        userToBeUpdated.setRole(hogwartsUser.getRole());
        userToBeUpdated.setEnabled(hogwartsUser.isEnabled());
        return this.hogwartsUserRepository.save(userToBeUpdated);
    }

    public void deleteUser(Integer userId){
        this.hogwartsUserRepository.findById(userId)
                .orElseThrow(()->new ObjectNotFoundException(ExceptionConstants.USER,userId));
        this.hogwartsUserRepository.deleteById(userId);
    }



}
