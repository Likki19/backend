package com.indium.assignment.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString(exclude = {"match", "team"})
@Table(name = "players")
@JsonIgnoreProperties({"match", "team"})
public class Player implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int playerId;

    private String playerName;

    @ManyToOne
    @JoinColumn(name = "match_number")

    private Match match;

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonBackReference  // This breaks the recursive serialization on the "team" side
    private Team team;

    @JsonIgnoreProperties("players")
    public Match getMatch() {
        return match;
    }


}