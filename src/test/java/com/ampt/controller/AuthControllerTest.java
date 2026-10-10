package com.ampt.controller;

import com.ampt.entity.AppUser;
import com.ampt.entity.Role;
import com.ampt.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("GET /login renders login view with 200 OK")
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("POST /login with valid credentials authenticates user and sets session")
    void testLogin_Success() throws Exception {
        AppUser user = new AppUser("entry", "hashed", Role.ENTRY_USER);
        when(authService.authenticate("entry", "entry123")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/login")
                        .param("username", "entry")
                        .param("password", "entry123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prices"))
                .andExpect(request().sessionAttribute(AuthController.SESSION_USER_KEY, user));
    }

    @Test
    @DisplayName("POST /login with invalid credentials fails and redirects with error flash")
    void testLogin_Failure() throws Exception {
        when(authService.authenticate("baduser", "wrongpwd")).thenReturn(Optional.empty());

        mockMvc.perform(post("/login")
                        .param("username", "baduser")
                        .param("password", "wrongpwd"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("GET /logout clears session and redirects to login")
    void testLogout() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }
}
