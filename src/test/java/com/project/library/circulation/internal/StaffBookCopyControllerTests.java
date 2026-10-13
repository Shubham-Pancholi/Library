package com.project.library.circulation.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

@WebMvcTest (StaffBookCopyController.class)
@Import (
    {
        SecurityConfig.class,
        CirculationExceptionHandler.class
    }
)
public class StaffBookCopyControllerTests {
    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean 
    private BookCopyService bookCopyService;

    @MockitoBean 
    private JwtDecoder jwtDecoder;

    @Test 
    public void staffCreatesAndFindsCopy() throws Exception {
        UUID copyId = UUID.randomUUID();
        UUID catalogBookId = UUID.randomUUID();

        BookCopyDetails bookCopy = new BookCopyDetails(copyId, catalogBookId, "ACC-001", "BC-001", CirculationType.CIRCULATING, CopyAvailabilityStatus.AVAILABLE);

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);
        when(bookCopyService.createCopy(catalogBookId, "ACC-001", "BC-001", CirculationType.CIRCULATING)).thenReturn(bookCopy);

        mockMvc.perform(
                post("/api/staff/copies")
                .header("Authorization", "Bearer staff-test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "catalogBookId": "%s",
                            "accessionNumber": "ACC-001",
                            "barcode": "BC-001",
                            "circulationType": "CIRCULATING"
                        }
                        """.formatted(catalogBookId))
            )
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/staff/copies/" + copyId))
            .andExpect(jsonPath("$.id").value(bookCopy.id().toString()))
            .andExpect(jsonPath("$.catalogBookId").value(bookCopy.catalogBookId().toString()))
            .andExpect(jsonPath("$.accessionNumber").value(bookCopy.accessionNumber()))
            .andExpect(jsonPath("$.barcode").value(bookCopy.barcode()))
            .andExpect(jsonPath("$.circulationType").value(bookCopy.circulationType().toString()))
            .andExpect(jsonPath("$.availabilityStatus").value(bookCopy.availabilityStatus().toString()));

        verify(bookCopyService).createCopy(catalogBookId, "ACC-001", "BC-001", CirculationType.CIRCULATING);

        when(bookCopyService.findCopy(copyId)).thenReturn(Optional.of(bookCopy));

        mockMvc.perform(
                get("/api/staff/copies/" + copyId)
                .header("Authorization", "Bearer staff-test-token")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(bookCopy.id().toString()))
            .andExpect(jsonPath("$.catalogBookId").value(bookCopy.catalogBookId().toString()))
            .andExpect(jsonPath("$.accessionNumber").value(bookCopy.accessionNumber()))
            .andExpect(jsonPath("$.barcode").value(bookCopy.barcode()))
            .andExpect(jsonPath("$.circulationType").value(bookCopy.circulationType().toString()))
            .andExpect(jsonPath("$.availabilityStatus").value(bookCopy.availabilityStatus().toString()));

        verify(bookCopyService).findCopy(copyId);
    }

    @Test 
    public void staffCreatesCopyWithBarcodeNull() throws Exception {
        UUID copyId = UUID.randomUUID();
        UUID catalogBookId = UUID.randomUUID();
        String accesstionNumber = "ACC-001";
        CirculationType circulationType = CirculationType.ARCHIVE;
        CopyAvailabilityStatus copyAvailabilityStatus = CopyAvailabilityStatus.AVAILABLE;

        BookCopyDetails bookCopy = new BookCopyDetails(copyId, catalogBookId, accesstionNumber, null, circulationType, copyAvailabilityStatus);

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);
        when(bookCopyService.createCopy(catalogBookId, accesstionNumber, null, circulationType)).thenReturn(bookCopy);

        mockMvc.perform(
                post("/api/staff/copies")
                .header("Authorization", "Bearer staff-test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "catalogBookId": "%s",
                            "accessionNumber": "ACC-001",
                            "circulationType": "ARCHIVE"
                        }
                        """.formatted(catalogBookId))
            )
            .andExpect(status().isCreated());

        verify(bookCopyService).createCopy(catalogBookId, accesstionNumber, null, circulationType);
    }

    @Test 
    public void blankBarcodeIsRejected() throws Exception{
        UUID catalogBookId = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);

        mockMvc.perform(
                post("/api/staff/copies")
                .header("Authorization", "Bearer staff-test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "catalogBookId": "%s",
                            "accessionNumber": "ACC-001",
                            "barcode": " ",
                            "circulationType": "ARCHIVE"
                        }
                        """.formatted(catalogBookId))
            )
            .andExpect(status().isBadRequest());
        
        verifyNoInteractions(bookCopyService);
    }

    @Test 
    public void unknownCatalogBookIsRejected() throws Exception{
        UUID catalogBookId = UUID.randomUUID();
        String accesstionNumber = "ACC-001";
        String barcode = "BC-001";
        CirculationType circulationType = CirculationType.ARCHIVE;

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);
        when(bookCopyService.createCopy(catalogBookId, accesstionNumber, barcode, circulationType)).thenThrow(new CatalogBookNotFoundException(catalogBookId));

        mockMvc.perform(
                post("/api/staff/copies")
                .header("Authorization", "Bearer staff-test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "catalogBookId": "%s",
                            "accessionNumber": "ACC-001",
                            "barcode": "BC-001",
                            "circulationType": "ARCHIVE"
                        }
                        """.formatted(catalogBookId))
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Catalog book not found: " + catalogBookId))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_PROBLEM_JSON
            ));
    }

    @Test 
    public void unknownCopyReturns404() throws Exception{
        UUID id = UUID.randomUUID();

        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        when(jwtDecoder.decode("staff-test-token")).thenReturn(staffJwt);
        when(bookCopyService.findCopy(id)).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/api/staff/copies/" + id)
                .header("Authorization", "Bearer staff-test-token")
            )
            .andExpect(status().isNotFound());
    }
}