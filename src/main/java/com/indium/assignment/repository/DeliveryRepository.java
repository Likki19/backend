package com.indium.assignment.repository;

import com.indium.assignment.entity.Delivery;
import com.indium.assignment.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    // Custom query methods (if any) can be added here

}
