package com.indium.assignment.repository;
import com.indium.assignment.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    // Custom query methods (if any) can be added here
}