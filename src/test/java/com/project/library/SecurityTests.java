package com.project.library;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@WebMvcTest (IdentityController.class)
@Import (SecurityConfig.class)
public class SecurityTests {
    
    @Autowired 
    MockMvc mockMvc;

    @MockitoBean 
    JwtDecoder jwtDecoder;

    @Test 
    public void staffEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/staff/check"))
            .andExpect(status().isUnauthorized());
    }

    @Test 
    public void staffEndpointWithStaffTokenReturns200() throws Exception {
        
        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
            .header("alg", "RS256")
            .subject("staff-test-subject")
            .claim("scope", "openid")
            .claim("realm_access", Map.of("roles", List.of("STAFF")))
            .build();
        
        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        mockMvc.perform(get("/api/staff/check")
            .header("Authorization", "Bearer staff-test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Staff access confirmed"));
    }

    @Test 
    public void staffEndpointWithNonStaffTokenReturns403() throws Exception {
        
        Jwt nonStaffJwt = Jwt.withTokenValue("nonstaff-test-token")
            .header("alg", "RS256")
            .subject("nonstaff-test-subject")
            .claim("scope", "openid")
            .claim("realm_access", Map.of("roles", List.of()))
            .build();
        
        when(jwtDecoder.decode("nonstaff-test-token")).thenReturn(nonStaffJwt);

        mockMvc.perform(get("/api/staff/check")
            .header("Authorization", "Bearer nonstaff-test-token"))
            .andExpect(status().isForbidden());
    }

    @Test 
    public void identityEndpointWithoutRealmRolesReturns200() throws Exception {
        
        Jwt jwtWithoutRoles = Jwt.withTokenValue("nonstaff-test-token")
            .header("alg", "RS256")
            .subject("nonstaff-test-subject")
            .claim("scope", "openid")
            .build();

        when(jwtDecoder.decode("nonstaff-test-token")).thenReturn(jwtWithoutRoles);

        mockMvc.perform(get("/api/me")
            .header("Authorization", "Bearer nonstaff-test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subject").value("nonstaff-test-subject"))
            .andExpect(jsonPath("$.authorities").value(hasItem("SCOPE_openid")))
            .andExpect(jsonPath("$.authorities").value(not(hasItem("ROLE_STAFF"))));
    }
}