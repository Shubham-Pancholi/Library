package com.project.library.patron.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.library.SecurityConfig;

@WebMvcTest (StaffMembershipController.class)
@Import (SecurityConfig.class)
public class StaffMembershipControllerTests {

    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean 
    private MembershipService membershipService;

    @MockitoBean 
    private JwtDecoder jwtDecoder;

    @Test 
    public void staffLookupMembershipSucceeds() throws Exception{
        UUID membership = UUID.randomUUID();
        UUID patron = UUID.randomUUID();
        UUID planVersion = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2026-09-30T00:00:00Z");
        MembershipStatus status = MembershipStatus.ACTIVE;
        boolean valid = false;

        MembershipDetails membershipDetails = new MembershipDetails(membership, patron, planVersion, startsAt, endsAt, status, valid);

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);
        when(membershipService.findMembership(membership)).thenReturn(Optional.of(membershipDetails));

        mockMvc.perform(
                get("/api/staff/memberships/{id}", membership)
                .header("Authorization", "Bearer staff-test-token")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(membership.toString()))
            .andExpect(jsonPath("$.patronId").value(patron.toString()))
            .andExpect(jsonPath("$.planVersionId").value(planVersion.toString()))
            .andExpect(jsonPath("$.startsAt").value(startsAt.toString()))
            .andExpect(jsonPath("$.endsAt").value(endsAt.toString()))
            .andExpect(jsonPath("$.status").value(status.toString()))
            .andExpect(jsonPath("$.currentlyValid").value(valid));

        verify(membershipService).findMembership(membership);
    }

    @Test 
    public void unknownMembershipReturns404() throws Exception {
        UUID membership = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);
        when(membershipService.findMembership(membership)).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/api/staff/memberships/{id}", membership)
                .header("Authorization", "Bearer staff-test-token")
            )
            .andExpect(status().isNotFound());
    }

    @Test 
    public void nonStaffRequestReturns403() throws Exception{
        UUID membership = UUID.randomUUID();

        Jwt nonstaffJwt = Jwt.withTokenValue("nonstaff-test-token")
                        .header("alg", "RS256")
                        .subject("nonstaff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of()))
                        .build();

        when(jwtDecoder.decode("nonstaff-test-token")).thenReturn(nonstaffJwt);

        mockMvc.perform(
                get("/api/staff/memberships/{id}", membership)
                .header("Authorization", "Bearer nonstaff-test-token")
            )
            .andExpect(status().isForbidden());

        verifyNoInteractions(membershipService);
    }
}