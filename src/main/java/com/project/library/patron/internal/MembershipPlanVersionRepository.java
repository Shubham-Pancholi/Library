package com.project.library.patron.internal;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPlanVersionRepository extends JpaRepository<MembershipPlanVersion, UUID> {
}