package com.project.library.catalog.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.library.SecurityConfig;
import com.project.library.catalog.BookDetails;

@WebMvcTest (StaffBookController.class)
@Import (SecurityConfig.class)
public class StaffBookControllerTests {

    @Autowired 
    MockMvc mockMvc;

    @MockitoBean 
    CatalogService catalogService;

    @MockitoBean 
    JwtDecoder jwtDecoder;

    @Test 
    public void staffCreatesBook() throws Exception{
        UUID bookId = UUID.randomUUID();
        String title = "The Hobbit";

        BookDetails book = new BookDetails(bookId, title);

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
            .header("alg", "RS256")
            .subject("staff-test-subject")
            .claim("scope", "openid")
            .claim("realm_access", Map.of("roles", List.of("STAFF")))
            .build();
        
        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        when(catalogService.createBook(title)).thenReturn(book);

        mockMvc.perform(post("/api/staff/books")
                .header("Authorization", "Bearer staff-test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"The Hobbit"}
                        """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/books/" + bookId))
            .andExpect(jsonPath("$.id").value(bookId.toString()))
            .andExpect(jsonPath("$.title").value(title));

        verify(catalogService).createBook("The Hobbit");
    }

    @Test 
    public void nonStaffCannotCreateBook() throws Exception {
        Jwt readerJwt = Jwt.withTokenValue("reader-test-token")
            .header("alg", "RS256")
            .subject("reader-test-subject")
            .claim("scope", "openid")
            .claim("realm_access", Map.of("roles", List.of()))
            .build();

        when(jwtDecoder.decode("reader-test-token")).thenReturn(readerJwt);

        mockMvc.perform(post("/api/staff/books")
                .header("Authorization", "Bearer reader-test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"The Hobbit"}
                        """))
            .andExpect(status().isForbidden());

        verifyNoInteractions(catalogService);
    }

    @Test 
    public void requestWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/staff/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"The Hobbit"}
                        """))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(catalogService, jwtDecoder);
    }

    @Test 
    public void staffCannotCreateBookWithBlankTitle() throws Exception{

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
            .header("alg", "RS256")
            .subject("staff-test-subject")
            .claim("scope", "openid")
            .claim("realm_access", Map.of("roles", List.of("STAFF")))
            .build();
        
        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        mockMvc.perform(post("/api/staff/books")
                .header("Authorization", "Bearer staff-test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"  "}
                        """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(catalogService);
    }

    @Test 
    public void staffCannotCreateBookWithoutTitle() throws Exception {
        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
            .header("alg", "RS256")
            .subject("staff-test-subject")
            .claim("scope", "openid")
            .claim("realm_access", Map.of("roles", List.of("STAFF")))
            .build();
        
        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        mockMvc.perform(post("/api/staff/books")
                .header("Authorization", "Bearer staff-test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {}
                        """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(catalogService);
    }
}