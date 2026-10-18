package com.project.library.patron.internal;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.library.SecurityConfig;

@WebMvcTest (StaffPatronController.class)
@Import (
    {
        SecurityConfig.class,
        PatronExceptionHandler.class
    }
)
public class StaffPatronControllerTests {

    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean 
    private PatronService patronService;

    @MockitoBean 
    private JwtDecoder jwtDecoder;

    @Test 
    public void staffCreatesPatron() throws Exception {
        UUID id = UUID.randomUUID();
        PatronType patronType = PatronType.MEMBER;
        String firstName = "Asha";
        String lastName = "Rao";
        String phone = "0123456789";

        PatronDetails patron = new PatronDetails(id, patronType, firstName, lastName, phone);

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        when(patronService.createPatron(patronType, firstName, lastName, phone)).thenReturn(patron);

        mockMvc.perform(post("/api/staff/patrons")
                    .header("Authorization", "Bearer staff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "patronType": "MEMBER",
                            "firstName": "Asha",
                            "lastName": "Rao",
                            "phone": "0123456789"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/staff/patrons/" + patron.id()))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.patronType").value(patronType.toString()))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.phone").value(phone));

        verify(patronService).createPatron(patronType, firstName, lastName, phone);
    }

    @Test 
    public void staffGetsAPatron() throws Exception {
        UUID id = UUID.randomUUID();
        PatronType patronType = PatronType.MEMBER;
        String firstName = "Asha";
        String lastName = "Rao";
        String phone = "0123456789";

        PatronDetails patron = new PatronDetails(id, patronType, firstName, lastName, phone);

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        when(patronService.findPatron(id)).thenReturn(Optional.of(patron));

        mockMvc.perform(get("/api/staff/patrons/{id}", id)
                    .header("Authorization", "Bearer staff-test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.patronType").value(patronType.toString()))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.phone").value(phone));
    }

    @Test 
    public void staffGets404ForUnknownPatron() throws Exception {
        UUID id = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        when(patronService.findPatron(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/staff/patrons/{id}", id)
                    .header("Authorization", "Bearer staff-test-token"))
                .andExpect(status().isNotFound());
    }

    @Test 
    public void staffGets400ForBlankFirstName() throws Exception {
        
        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        mockMvc.perform(post("/api/staff/patrons")
                    .header("Authorization", "Bearer staff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "patronType": "MEMBER",
                            "firstName": "   ",
                            "lastName": "Rao",
                            "phone": "0123456789"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(patronService);
    }

    @Test 
    public void staffGets400ForOmittedPatronType() throws Exception {
        
        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        mockMvc.perform(post("/api/staff/patrons")
                    .header("Authorization", "Bearer staff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "firstName": "Asha",
                            "lastName": "Rao",
                            "phone": "0123456789"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(patronService);
    }

    @Test 
    public void nonstaffGets403ForRequestingPatron() throws Exception {
        
        Jwt nonstaffJwt = Jwt.withTokenValue("nonstaff-test-token")
                        .header("alg", "RS256")
                        .subject("nonstaff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of()))
                        .build();

        when(jwtDecoder.decode("nonstaff-test-token")).thenReturn(nonstaffJwt);

        mockMvc.perform(get("/api/staff/patrons/{id}", UUID.randomUUID())
                    .header("Authorization", "Bearer nonstaff-test-token"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(patronService);
    }

    @Test 
    public void staffBlocksPatron() throws Exception {
        UUID id = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        mockMvc.perform(post("/api/staff/patrons/{id}/administrative-block", id)
                    .header("Authorization", "Bearer staff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "reason": "Reason A"
                        }
                        """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(patronService).blockPatron(id, "Reason A");
    }

    @Test 
    public void blockingUnknownPatronThrows() throws Exception {
        UUID id = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);
        doThrow(new PatronNotFoundException("Patron not found with id: " + id))
            .when(patronService).blockPatron(id, "Reason A");

        mockMvc.perform(post("/api/staff/patrons/{id}/administrative-block", id)
                    .header("Authorization", "Bearer staff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "reason": "Reason A"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Patron not found with id: " + id))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(content().contentTypeCompatibleWith(
                    MediaType.APPLICATION_PROBLEM_JSON
                ));

        verify(patronService).blockPatron(id, "Reason A");
    }

    @Test 
    public void blockingBlockedPatronThrows() throws Exception {
        UUID id = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);
        doThrow(new AdministrativeBlockConflictException("Invalid Blocking operation: patron already blocked"))
            .when(patronService).blockPatron(id, "Reason A");

        mockMvc.perform(post("/api/staff/patrons/{id}/administrative-block", id)
                    .header("Authorization", "Bearer staff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "reason": "Reason A"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Invalid Blocking operation: patron already blocked"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(content().contentTypeCompatibleWith(
                    MediaType.APPLICATION_PROBLEM_JSON
                ));

        verify(patronService).blockPatron(id, "Reason A");
    }

    @ParameterizedTest 
    @ValueSource (strings = {"{\"reason\":\"  \"}","{}"})
    public void invalidReasonIsRejectedBlock(String json) throws Exception {
        UUID id = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        mockMvc.perform(post("/api/staff/patrons/{id}/administrative-block", id)
                    .header("Authorization", "Bearer staff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        %s
                        """.formatted(json)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(patronService);
    }

    @ParameterizedTest 
    @ValueSource (strings = {"{\"reason\":\"  \"}","{}"})
    public void invalidReasonIsRejectedClear(String json) throws Exception {
        UUID id = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        mockMvc.perform(post("/api/staff/patrons/{id}/administrative-block/clear", id)
                    .header("Authorization", "Bearer staff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        %s
                        """.formatted(json)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(patronService);
    }

    @ParameterizedTest 
    @ValueSource (strings = {"", "/clear"})
    public void nonstaffGets403ForBlockingOrClearingPatron(String apiEndpoint) throws Exception {
        
        Jwt nonstaffJwt = Jwt.withTokenValue("nonstaff-test-token")
                        .header("alg", "RS256")
                        .subject("nonstaff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of()))
                        .build();

        when(jwtDecoder.decode("nonstaff-test-token")).thenReturn(nonstaffJwt);

        mockMvc.perform(post("/api/staff/patrons/{id}/administrative-block{x}", UUID.randomUUID(), apiEndpoint)
                    .header("Authorization", "Bearer nonstaff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "reason": "Reason A"
                        }
                        """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(patronService);
    }

    @Test 
    public void unauthenticatedGets401ForBlockingPatron() throws Exception {

        mockMvc.perform(post("/api/staff/patrons/{id}/administrative-block", UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "reason": "Reason A"
                        }
                        """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(patronService);
    }

    @Test 
    public void staffClearsBlockedPatron() throws Exception {
        UUID id = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        mockMvc.perform(post("/api/staff/patrons/{id}/administrative-block/clear", id)
                    .header("Authorization", "Bearer staff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "reason": "Reason B"
                        }
                        """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(patronService).clearPatronBlock(id, "Reason B");
    }

    @Test 
    public void clearingUnblockedPatronThrows() throws Exception {
        UUID id = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);
        doThrow(new AdministrativeBlockConflictException("Invalid Clearing operation: patron is not blocked"))
            .when(patronService).clearPatronBlock(id, "Reason B");

        mockMvc.perform(post("/api/staff/patrons/{id}/administrative-block/clear", id)
                    .header("Authorization", "Bearer staff-test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "reason": "Reason B"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Invalid Clearing operation: patron is not blocked"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(content().contentTypeCompatibleWith(
                    MediaType.APPLICATION_PROBLEM_JSON
                ));

        verify(patronService).clearPatronBlock(id, "Reason B");
    }
}