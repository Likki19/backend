package com.indium.assignment.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"match", "players"})
@Table(name = "teams")
public class Team implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int teamId;

    private String teamName;

    @ManyToOne
    @JoinColumn(name = "match_number")
//    @JsonIgnore
    @JsonBackReference
    private Match match;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @JsonManagedReference  // Forward reference serialization
    private List<Player> players;

    @JsonIgnoreProperties("team")
    public List<Player> getPlayers() {
        return players;
    }
}