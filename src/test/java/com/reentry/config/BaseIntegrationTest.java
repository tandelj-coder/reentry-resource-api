package com.reentry.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reentry.auth.JwtService;
import com.reentry.user.Role;
import com.reentry.user.User;
import com.reentry.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all integration tests.
 * Sets up MockMvc with the full Spring Security filter chain,
 * and provides helper methods to build JWT tokens for each role.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected String adminToken;
    protected String caseworkerToken;
    protected String providerToken;

    protected User adminUser;
    protected User caseworkerUser;
    protected User providerUser;

    @BeforeEach
    void setUpUsers() {
        userRepository.deleteAll();

        adminUser = userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ADMIN)
                .active(true)
                .build());

        caseworkerUser = userRepository.save(User.builder()
                .firstName("Case")
                .lastName("Worker")
                .email("caseworker@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.CASEWORKER)
                .active(true)
                .build());

        providerUser = userRepository.save(User.builder()
                .firstName("Service")
                .lastName("Provider")
                .email("provider@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.PROVIDER)
                .active(true)
                .build());

        adminToken = "Bearer " + jwtService.generateToken(adminUser.getEmail());
        caseworkerToken = "Bearer " + jwtService.generateToken(caseworkerUser.getEmail());
        providerToken = "Bearer " + jwtService.generateToken(providerUser.getEmail());
    }

    protected String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
