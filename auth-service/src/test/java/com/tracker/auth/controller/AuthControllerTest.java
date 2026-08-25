package com.tracker.auth.controller;

import com.tracker.auth.dto.AuthResponse;
import com.tracker.auth.dto.RegisterRequest;
import com.tracker.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_ShouldReturnCreated() throws Exception {
        String requestJson = """
                {
                    "username": "testuser",
                    "password": "testpass",
                    "email": "test@example.com",
                    "firstName": "Test",
                    "lastName": "User"
                }
                """;

        doNothing().when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    void login_ShouldReturnToken() throws Exception {
        String requestJson = """
                {
                    "username": "testuser",
                    "password": "testpass"
                }
                """;

        AuthResponse response = new AuthResponse();
        response.setToken("jwt-token");
        response.setUsername("testuser");
        response.setRole("USER");
        response.setExpiresIn(86400L);

        when(authService.login(any(String.class), any(String.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}