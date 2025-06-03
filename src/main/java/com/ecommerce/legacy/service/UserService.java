package com.ecommerce.legacy.service;

import com.ecommerce.legacy.model.User;
import com.ecommerce.legacy.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        validateUser(user);
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User userDetails) {
        User existingUser = getUserById(id);
        
        if (StringUtils.isNotBlank(userDetails.getUsername()) && 
            !existingUser.getUsername().equals(userDetails.getUsername())) {
            if (userRepository.existsByUsername(userDetails.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + userDetails.getUsername());
            }
            existingUser.setUsername(userDetails.getUsername());
        }

        if (StringUtils.isNotBlank(userDetails.getEmail()) && 
            !existingUser.getEmail().equals(userDetails.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + userDetails.getEmail());
            }
            existingUser.setEmail(userDetails.getEmail());
        }

        if (StringUtils.isNotBlank(userDetails.getFirstName())) {
            existingUser.setFirstName(userDetails.getFirstName());
        }

        if (StringUtils.isNotBlank(userDetails.getLastName())) {
            existingUser.setLastName(userDetails.getLastName());
        }

        if (StringUtils.isNotBlank(userDetails.getPassword())) {
            existingUser.setPassword(userDetails.getPassword());
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (StringUtils.isBlank(user.getEmail())) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (StringUtils.isBlank(user.getPassword())) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
    }
}
