package com.ecommerce.legacy.controller;

import com.ecommerce.legacy.model.User;
import com.ecommerce.legacy.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        
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
    public void testCreateUser_Success() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    public void testCreateUser_BadRequest() throws Exception {
        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    public void testGetUserById_Success() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(testUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    public void testGetUserByUsername_Success() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(testUser);

        mockMvc.perform(get("/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).getUserByUsername("testuser");
    }

    @Test
    public void testGetAllUsers_Success() throws Exception {
        List<User> userList = Arrays.asList(testUser, new User());
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void testUpdateUser_Success() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");

        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"));

        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    @Test
    public void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    public void testCheckUsernameExists_True() throws Exception {
        when(userService.existsByUsername(anyString())).thenReturn(true);

        mockMvc.perform(get("/users/exists/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));

        verify(userService, times(1)).existsByUsername("testuser");
    }

    @Test
    public void testCheckEmailExists_False() throws Exception {
        when(userService.existsByEmail(anyString())).thenReturn(false);

        mockMvc.perform(get("/users/exists/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));

        verify(userService, times(1)).existsByEmail("test@example.com");
    }
}
