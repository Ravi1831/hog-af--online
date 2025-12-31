package com.ravi.hogwartsartifact.hogwartsuser;

import com.ravi.hogwartsartifact.system.ExceptionConstants;
import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final HogwartsUserRepository hogwartsUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(HogwartsUserRepository hogwartsUserRepository, PasswordEncoder passwordEncoder) {
        this.hogwartsUserRepository = hogwartsUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<HogwartsUser> findAll(){
        return this.hogwartsUserRepository.findAll();
    }

    public HogwartsUser findByUserId(Integer userId){
        return this.hogwartsUserRepository.findById(userId)
                .orElseThrow(()-> new ObjectNotFoundException(ExceptionConstants.USER,userId));
    }

    public HogwartsUser save(HogwartsUser newHogwartsUser){
        newHogwartsUser.setPassword(this.passwordEncoder.encode(newHogwartsUser.getPassword()));
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


    public long count() {
        return this.hogwartsUserRepository.count();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //find user from db id found wrap the return user instance in MyUserPrinciple instance otherwise throw an exception
        return this.hogwartsUserRepository.findByUserName(username)
                .map(MyUserPrincipal::new)
                .orElseThrow(()-> new UsernameNotFoundException("username "+ username+ "is not found"));
    }
}
