package com.indium.assignment.entity;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

@Entity
@Table(name = "officials")
@Data
public class Official {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer officialId;

    private String officialType;
    private String officialName;

    @ManyToOne
    @JoinColumn(name = "match_number")
    private Match match;

    // Getters and setters
}