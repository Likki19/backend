package com.indium.assignment.repository;

import com.indium.assignment.entity.Powerplay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PowerplayRepository extends JpaRepository<Powerplay, Integer> {
    // Custom query methods (if any) can be added here
}