package com.indium.assignment.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"teams", "players", "deliveries", "officials", "powerplays"})
@Table(name = "matches")
public class Match implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id

    private int matchNumber;

    private String city;
    private LocalDateTime dates;
    //private Integer matchNumber;
    private String eventName;
    private String matchType;
    private String gender;
    private String season;
    private String tossWinner;
    private String tossDecision;
    private String winner;
    private Integer outcomeByWickets;
    private Integer overs;
    private String playerOfMatch;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    @JsonManagedReference  // Forward reference serialization
    private List<Team> teams;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Player> players;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Delivery> deliveries;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Official> officials;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Powerplay> powerplays;

    @JsonIgnoreProperties("match")
    public List<Team> getTeams() {
        return teams;
    }

    @JsonIgnoreProperties("match")
    public List<Player> getPlayers() {
        return players;
    }
}

