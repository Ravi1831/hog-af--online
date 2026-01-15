package com.ravi.hogwartsartifact.hogwartsuser;

import com.ravi.hogwartsartifact.client.redisClient.RedisCacheClient;
import com.ravi.hogwartsartifact.system.constant.ExceptionConstants;
import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import com.ravi.hogwartsartifact.system.exception.PasswordChangeIllegalArgumentException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ravi.hogwartsartifact.system.constant.RedisConstant.WHITELIST_USER;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final HogwartsUserRepository hogwartsUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisCacheClient redisCacheClient;

    public UserService(HogwartsUserRepository hogwartsUserRepository, PasswordEncoder passwordEncoder, RedisCacheClient redisCacheClient) {
        this.hogwartsUserRepository = hogwartsUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisCacheClient = redisCacheClient;
    }

    public Page<HogwartsUser> findAll(Pageable pageable){
        return this.hogwartsUserRepository.findAll(pageable);
    }

    public HogwartsUser findByUserId(Integer userId){
        return this.hogwartsUserRepository.findById(userId)
                .orElseThrow(()-> new ObjectNotFoundException(ExceptionConstants.USER,userId));
    }

    public HogwartsUser save(HogwartsUser newHogwartsUser){
//        this.hogwartsUserRepository.
        newHogwartsUser.setPassword(this.passwordEncoder.encode(newHogwartsUser.getPassword()));
        return this.hogwartsUserRepository.save(newHogwartsUser);
    }

    public HogwartsUser updateUser(Integer userId, HogwartsUser hogwartsUser){
        HogwartsUser userToBeUpdated = this.hogwartsUserRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.USER, userId));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"));

        // If the user is not an admin, then the user can only update her username (username can be modified by user and admin)
        userToBeUpdated.setUserName(hogwartsUser.getUserName());

        if(isAdmin){
            // If the user is an admin, then the user can update username, enabled, and roles.
            userToBeUpdated.setRole(hogwartsUser.getRole());
            userToBeUpdated.setEnabled(hogwartsUser.isEnabled());
            this.redisCacheClient.delete(WHITELIST_USER+":"+userId);
        }

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

    public void changePassword(Integer userId, String oldPassword, String newPassword, String conformNewPassword) {
        HogwartsUser hogwartsUser = this.hogwartsUserRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.USER, userId));
        if(!this.passwordEncoder.matches(oldPassword,hogwartsUser.getPassword())){
            throw new BadCredentialsException("Old Password is incorrect");
        }
        if(!newPassword.equals(conformNewPassword)){
            throw new PasswordChangeIllegalArgumentException("New password and confirm new password do not match.");
        }
        // The new password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long.
        String passwordPolicy = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";

        if(!newPassword.matches(passwordPolicy)){
            throw new PasswordChangeIllegalArgumentException("New password does not conform to password policy.");
        }
        hogwartsUser.setPassword(this.passwordEncoder.encode(newPassword));
        //revoke the user current JWT by deleting it from the Redis
        this.redisCacheClient.delete(WHITELIST_USER+":"+userId);
        this.hogwartsUserRepository.save(hogwartsUser);
    }
}
