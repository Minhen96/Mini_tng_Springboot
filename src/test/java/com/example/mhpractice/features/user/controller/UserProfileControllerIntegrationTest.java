package com.example.mhpractice.features.user.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.repository.UserRepository;
import com.example.mhpractice.features.user.service.result.LoginResult;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest // ← Load FULL Spring context
@AutoConfigureMockMvc // ← Create MockMvc for HTTP requests
@Transactional // ← Rollback after each test
public class UserProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private MvcResult loginResult;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        // 1. Create user
        User user = User.builder()
                .email("test@example.com")
                .name("Test")
                .password(passwordEncoder.encode("password"))
                .build();
        userRepository.save(user);

        // 2. Login to get token
        loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andReturn();
    }

    @Test
    void success_get_user_profile() throws Exception {
        // ARRANGE: Prepare request
        // actually no need to prepare request

        // ACT: Make request
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(loginResult.getResponse().getCookie("auth_token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        // ASSERT: Check response
        assertTrue(userRepository.existsByEmail("test@example.com"));
    }

    @Test
    void fail_get_user_profile() throws Exception {
        // ARRANGE: Prepare request

        // ACT: Make request
        mockMvc.perform(get("/api/users/profile"))
                // .contentType(MediaType.APPLICATION_JSON)
                // .cookie(loginResult.getResponse().getCookie("auth_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void success_update_user_profile() throws Exception {
        // ARRANGE: Prepare request
        String requestBody = """
                {
                    "name": "test"
                }
                """;

        // ACT: Make request
        mockMvc.perform(put("/api/users/profile")
                .cookie(loginResult.getResponse().getCookie("auth_token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("test"));

        // ASSERT: Check response
        assertTrue(userRepository.existsByEmail("test@example.com"));
    }
}
