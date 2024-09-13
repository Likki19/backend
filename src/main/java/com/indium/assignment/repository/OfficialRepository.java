package com.indium.assignment.repository;

import com.indium.assignment.entity.Official;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficialRepository extends JpaRepository<Official, Integer> {
    // Custom query methods (if any) can be added here
}
