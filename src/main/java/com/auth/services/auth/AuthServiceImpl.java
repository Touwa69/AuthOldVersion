package com.auth.services.auth;

import com.auth.dto.SignupRequest;
import com.auth.dto.UserDto;
import com.auth.entity.User;
import com.auth.enums.UserRole;
import com.auth.repository.UserRepository;
import com.auth.dto.ChangePasswordDto;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserDto createUser(SignupRequest signupRequest) {
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setName(signupRequest.getName());
        user.setPassword(new BCryptPasswordEncoder().encode(signupRequest.getPassword()));
        user.setRole(UserRole.USER);
        User createdUser = userRepository.save(user);
        UserDto userDto = new UserDto();
        userDto.setId(createdUser.getId());
        return userDto;
    }
    @Override
    public ResponseEntity<?> updatePasswordById(ChangePasswordDto changePasswordDto) {
        User user = null;
        try {
            Optional<User> userOptional = userRepository.findById(changePasswordDto.getId());
            if (userOptional.isPresent()) {
                user = userOptional.get();
                if (this.bCryptPasswordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
                    user.setPassword(bCryptPasswordEncoder.encode(changePasswordDto.getNewPassword()));
                    user.setCreationDate(new Date());
                    User updateUser = userRepository.save(user);
                    UserDto userDto = new UserDto();
                    userDto.setId(updateUser.getId());
                    return ResponseEntity.status(HttpStatus.OK).body(userDto);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Old password is incorrect");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }
    public Boolean hasUserWithEmail(String email) {
        return userRepository.findFirstByEmail(email).isPresent();
    }

    @Override
    public UserDto getUserById(UUID id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.map(User::getUserDto).orElse(null);
    }
    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();  // Fetch all users from the repository
        return users.stream().map(User::getUserDto).collect(Collectors.toList());  // Convert to UserDto list
    }


    @Override
    public UserDto makeAdmin(UUID id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setRole(UserRole.ADMIN);
            return userRepository.save(existingUser).getUserDto();
        }
        return null;
    }

    @Override
    public List<UserDto> findByNameContains(String name) {
        return  userRepository.findByNameContains(name);
    }

    public boolean checkIfPasswordNeedsUpdate(User user) {
        Date lastCreationDate = user.getCreationDate();
        long differenceInMilliseconds = new Date().getTime() - lastCreationDate.getTime();
        long differenceInDays = differenceInMilliseconds / (1000 * 60 * 60 * 24);
        return differenceInDays >= 30;
    }
    @PostConstruct
    public void createAdminAccount() {
        User superAdminAccount = userRepository.findByRole(UserRole.SUPERADMIN);
        if (null == superAdminAccount) {
            User user = new User();
            user.setEmail("superadmin@test.com");
            user.setName("superadmin");
            user.setRole(UserRole.SUPERADMIN);
            user.setPassword(new BCryptPasswordEncoder().encode("superadmin"));
            userRepository.save(user);
        }
    }
}

