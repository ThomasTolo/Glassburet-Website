package com.glassburet.repository;

import com.glassburet.model.WordlePuzzle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WordlePuzzleRepository extends JpaRepository<WordlePuzzle, Long> {
    Optional<WordlePuzzle> findTopByOrderByCreatedAtDesc();
}
