package com.indium.assignment.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(name = "deliveries")
@Data
public class Delivery implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deliveryId;

    private Integer overNumber;
    private Integer ballNumber;
    private String batter;
    private String bowler;
    private String nonStriker;
    private Integer runsBatter;
    private Integer runsExtras;
    private Integer runsTotal;


    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "match_number")
    @JsonBackReference
    private Match match;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "player_id")
    @JsonBackReference
    private Player player;
    // Getters and setters

}
