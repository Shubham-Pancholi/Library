package com.project.library;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration 
public class SecurityConfig {

    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        httpSecurity
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(request -> request
                .requestMatchers("/api/staff/**").hasRole("STAFF")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
            ))
            .csrf(csrf -> csrf.disable());
        return httpSecurity.build();
    }

    @Bean 
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jGAC = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter jAC = new JwtAuthenticationConverter();

        jAC.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
            Collection<GrantedAuthority> defaultAuthorities = jGAC.convert(jwt);
            List<GrantedAuthority> authorities = defaultAuthorities != null ? new ArrayList<>(defaultAuthorities) : new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
                if (roles.contains("STAFF")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
                }
            }
            return authorities;
        });

        return jAC;
    }
}