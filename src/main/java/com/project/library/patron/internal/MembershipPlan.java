package com.project.library.patron.internal;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "membership_plan", schema = "patron")
public class MembershipPlan {

    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (columnDefinition = "text",nullable = false)
    private String name;

    protected MembershipPlan() {
    }

    public MembershipPlan(String aName) {
        if (aName == null || aName.isBlank()) {
            throw new IllegalArgumentException("Invalid Membership Plan field(s)");
        }
        name = aName;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}