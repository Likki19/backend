package com.indium.assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "powerplays")
@Data
public class Powerplay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer powerplayId;

    private Double fromOver;
    private Double toOver;
    private String type;

    @ManyToOne
    @JoinColumn(name = "match_number")
    private Match match;

    // Getters and setters
}