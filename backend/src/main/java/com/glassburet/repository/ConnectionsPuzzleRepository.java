package com.glassburet.repository;

import com.glassburet.model.ConnectionsPuzzle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConnectionsPuzzleRepository extends JpaRepository<ConnectionsPuzzle, Long> {
    Optional<ConnectionsPuzzle> findTopByOrderByCreatedAtDesc();
}
