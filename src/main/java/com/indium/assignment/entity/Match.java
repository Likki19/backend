package com.indium.assignment.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"teams", "players", "deliveries", "officials", "powerplays"})
@Table(name = "matches")
public class Match {
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
    private List<Team> teams;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Player> players;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Delivery> deliveries;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Official> officials;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Powerplay> powerplays;
}

