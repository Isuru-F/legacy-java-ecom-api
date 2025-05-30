package com.ecommerce.legacy.service;

import com.ecommerce.legacy.model.User;
import com.ecommerce.legacy.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    public void testCreateUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(testUser);

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void testCreateUser_UsernameExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        try {
            userService.createUser(testUser);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Username already exists"));
        }

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testCreateUser_EmailExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        try {
            userService.createUser(testUser);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Email already exists"));
        }

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testGetUserById_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        try {
            userService.getUserById(1L);
            fail("Expected EntityNotFoundException");
        } catch (EntityNotFoundException e) {
            assertTrue(e.getMessage().contains("User not found with id"));
        }
    }

    @Test
    public void testGetUserByUsername_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        User result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    public void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        try {
            userService.getUserByUsername("nonexistent");
            fail("Expected EntityNotFoundException");
        } catch (EntityNotFoundException e) {
            assertTrue(e.getMessage().contains("User not found with username"));
        }
    }

    @Test
    public void testGetAllUsers() {
        List<User> userList = Arrays.asList(testUser, new User());
        when(userRepository.findAll()).thenReturn(userList);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testUpdateUser_Success() {
        User updateDetails = new User();
        updateDetails.setFirstName("UpdatedFirst");
        updateDetails.setLastName("UpdatedLast");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, updateDetails);

        assertNotNull(result);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteUser_NotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        try {
            userService.deleteUser(1L);
            fail("Expected EntityNotFoundException");
        } catch (EntityNotFoundException e) {
            assertTrue(e.getMessage().contains("User not found with id"));
        }

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testExistsByUsername() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        boolean result = userService.existsByUsername("testuser");

        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername("testuser");
    }

    @Test
    public void testExistsByEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        boolean result = userService.existsByEmail("test@example.com");

        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }
}
