package com.project.library;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping ("/api")
public class IdentityController {
    
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        return Map.of(
            "subject", jwt.getSubject(),
            "authorities", authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList()
        );
    }

    @GetMapping ("/staff/check")
    public Map<String, String> checkStaffRole() {
        return Map.of("message", "Staff access confirmed");
    }
}