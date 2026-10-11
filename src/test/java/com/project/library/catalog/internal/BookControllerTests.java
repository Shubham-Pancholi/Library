package com.project.library.catalog.internal;

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
import com.project.library.catalog.BookDetails;
import com.project.library.catalog.CatalogApi;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest (BookController.class)
@Import (SecurityConfig.class)
public class BookControllerTests {
    
    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean 
    private CatalogApi catalogApi;

    @MockitoBean 
    private JwtDecoder jwtDecoder;

    @Test 
    public void authenticatedReaderGetsBook() throws Exception{

        UUID bookId = UUID.randomUUID();        
        BookDetails book = new BookDetails(bookId, "The Hobbit");

        Jwt readerJwt = Jwt.withTokenValue("reader-test-token")
            .header("alg", "RS256")
            .subject("reader-test-subject")
            .claim("scope", "openid")
            .claim("realm_access", Map.of("roles", List.of()))
            .build();

        when(jwtDecoder.decode("reader-test-token")).thenReturn(readerJwt);

        when(catalogApi.findBook(bookId)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/api/books/{bookId}", bookId)
                .header("Authorization", "Bearer reader-test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(bookId.toString()))
            .andExpect(jsonPath("$.title").value("The Hobbit"));
    }

    @Test 
    public void authenticatedReaderGets404ForUnknownBook() throws Exception{
        UUID bookId = UUID.randomUUID();

        Jwt readerJwt = Jwt.withTokenValue("reader-test-token")
            .header("alg", "RS256")
            .subject("reader-test-subject")
            .claim("scope", "openid")
            .claim("realm_access", Map.of("roles", List.of()))
            .build();

        when(jwtDecoder.decode("reader-test-token")).thenReturn(readerJwt);

        when(catalogApi.findBook(bookId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/{bookId}", bookId)
                .header("Authorization", "Bearer reader-test-token"))
            .andExpect(status().isNotFound());
    }

    @Test 
    public void requestWithoutTokenReturns401() throws Exception {
        UUID bookId = UUID.randomUUID();        

        mockMvc.perform(get("/api/books/{bookId}", bookId))
        .andExpect(status().isUnauthorized());

        verifyNoInteractions(catalogApi, jwtDecoder);
    }

    @Test 
    public void authenticatedReaderGets400ForInvalidBookId() throws Exception {
        
        Jwt readerJwt = Jwt.withTokenValue("reader-test-token")
            .header("alg", "RS256")
            .subject("reader-test-subject")
            .claim("scope", "openid")
            .claim("realm_access", Map.of("roles", List.of()))
            .build();

        when(jwtDecoder.decode("reader-test-token")).thenReturn(readerJwt);

        mockMvc.perform(get("/api/books/{bookId}", "not-a-uuid")
                .header("Authorization", "Bearer reader-test-token"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(catalogApi);
    }
}