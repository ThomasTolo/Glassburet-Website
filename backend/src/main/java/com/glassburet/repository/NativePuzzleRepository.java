package com.glassburet.repository;

import com.glassburet.model.GameName;
import com.glassburet.model.NativePuzzle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NativePuzzleRepository extends JpaRepository<NativePuzzle, Long> {
    List<NativePuzzle> findByGameNameOrderByCreatedAtDesc(GameName gameName);
    Optional<NativePuzzle> findByGameNameAndPuzzleDateAndIsDailyTrue(GameName gameName, LocalDate puzzleDate);
}
