package com.indium.assignment.repository;
import com.indium.assignment.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface PlayerRepository extends JpaRepository<Player, Integer> {
    List<Player> findAllByPlayerName(String playerName);
    Optional<Player> findByPlayerName(String playerName);

    // Custom query methods (if any) can be added here
//    @Query("SELECT SUM(d.runsBatter) FROM Delivery d JOIN d.player p WHERE p.playerName = :playerName")
//    Integer findCumulativeScoreByPlayerName(@Param("playerName") String playerName);

}
