package com.glassburet.repository;

import com.glassburet.model.WordlePuzzle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WordlePuzzleRepository extends JpaRepository<WordlePuzzle, Long> {
    Optional<WordlePuzzle> findTopByOrderByCreatedAtDesc();
    List<WordlePuzzle> findAllByOrderByCreatedAtDesc();
    Optional<WordlePuzzle> findByPuzzleDateAndIsDailyTrue(LocalDate puzzleDate);
}
