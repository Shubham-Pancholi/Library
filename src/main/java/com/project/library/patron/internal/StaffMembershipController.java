package com.project.library.patron.internal;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping ("/api/staff/memberships")
public class StaffMembershipController {

    private final MembershipService membershipService;

    public StaffMembershipController(
        MembershipService aMembershipService
    ) {
        membershipService = aMembershipService;
    }

    @GetMapping ("/{id}")
    public ResponseEntity<MembershipDetails> findMembership(
        @PathVariable UUID id
    ) {
        return ResponseEntity.of(membershipService.findMembership(id));
    }
}