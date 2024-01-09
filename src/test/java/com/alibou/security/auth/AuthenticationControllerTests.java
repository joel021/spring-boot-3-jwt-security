package com.alibou.security.auth;

import com.alibou.security.config.JwtService;
import com.alibou.security.token.Token;
import com.alibou.security.token.TokenRepository;
import com.alibou.security.token.TokenType;
import com.alibou.security.user.Role;
import com.alibou.security.user.User;
import com.alibou.security.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTests {


    @Autowired
    public MockMvc mockMvc;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenRepository tokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User user;

    private User alreadyExistentUser;

    private Token token;

    @BeforeEach
    public void setup() {

        user = new User().builder().id(102020202)
                .role(Role.ADMIN)
                .email("usernameauthenticated@authencitcated.com")
                .password("password").build();
        token = new Token().builder()
                .token(jwtService.generateToken(user))
                .tokenType(TokenType.BEARER)
                .id(1)
                .build();
        user.setTokens(Collections.singletonList(token));
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

        alreadyExistentUser = new User().builder()
                .id(993988998)
                .email("emailalreadyexistent@email.com")
                .password("password").build();

        if (userRepository.findByEmail(alreadyExistentUser.getEmail()).isEmpty()){
            userRepository.save(alreadyExistentUser);
        }
    }

    @Test
    public void registerWithoutEmailTest() throws Exception {

        final RegisterRequest requestRequest = new RegisterRequest(user.getFirstname(),
                user.getLastname(),
                null,
                user.getPassword(),
                Role.USER);
        final String requestBodyString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(requestRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .content(requestBodyString).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotAcceptable());
    }

    @Test
    public void registerDoesNotPasswordTest() throws Exception {

        final RegisterRequest requestRequest = new RegisterRequest(user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                null,
                Role.ADMIN);
        final String requestBodyString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(requestRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                .content(requestBodyString).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void registerAlreadyExistentTest() throws Exception {

        final RegisterRequest requestRequest = new RegisterRequest(alreadyExistentUser.getFirstname(),
                alreadyExistentUser.getLastname(),
                alreadyExistentUser.getEmail(),
                alreadyExistentUser.getPassword(),
                Role.USER);
        final String requestBodyString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(requestRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                .content(requestBodyString).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isConflict());
    }

    @Test
    public void registerTest() throws Exception {

        final RegisterRequest requestRequest = new RegisterRequest(user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPassword(),
                Role.USER);
        final String requestBodyString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(requestRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                .content(requestBodyString).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

}
