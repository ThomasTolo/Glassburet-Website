package com.glassburet.repository;

import com.glassburet.model.ConnectionsPuzzle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConnectionsPuzzleRepository extends JpaRepository<ConnectionsPuzzle, Long> {
    Optional<ConnectionsPuzzle> findTopByOrderByCreatedAtDesc();
    List<ConnectionsPuzzle> findAllByOrderByCreatedAtDesc();
    Optional<ConnectionsPuzzle> findByPuzzleDateAndIsDailyTrue(LocalDate puzzleDate);
}
