package com.sbaldasso.auth_microservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbaldasso.auth_microservice.dtos.LoginDto;
import com.sbaldasso.auth_microservice.dtos.RegisterDto;
import com.sbaldasso.auth_microservice.models.User;
import com.sbaldasso.auth_microservice.repositories.UserRepository;
import com.sbaldasso.auth_microservice.services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void registerUser_whenUsernameIsAvailable_shouldReturnOk() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("test@example.com");
        registerDto.setPassword("password123");

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));
    }

    @Test
    public void registerUser_whenUsernameIsTaken_shouldReturnBadRequest() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("test@example.com");
        registerDto.setPassword("password123");

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken!"));
    }

    @Test
    public void loginUser_withValidCredentials_shouldReturnToken() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("test@example.com");
        loginDto.setPassword("password123");

        User user = new User();
        user.setUsername("test@example.com");
        user.setPassword("encodedPassword");

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenService.generateToken(any(User.class))).thenReturn("test-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    public void loginUser_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("test@example.com");
        loginDto.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

}
