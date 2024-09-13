package com.indium.assignment.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@ToString(exclude = {"match", "team"})
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int playerId;

    private String playerName;

    @ManyToOne
    @JoinColumn(name = "match_number")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;


}