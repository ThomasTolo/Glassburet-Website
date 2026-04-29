package com.glassburet.repository;

import com.glassburet.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query("SELECT s.memberName, SUM(s.scoreValue) FROM Score s WHERE s.gameDate = :date GROUP BY s.memberName ORDER BY SUM(s.scoreValue) DESC")
    List<Object[]> findDailyLeaderboard(@Param("date") LocalDate date);

    @Query("SELECT s.memberName, SUM(s.scoreValue) FROM Score s WHERE s.gameDate BETWEEN :start AND :end GROUP BY s.memberName ORDER BY SUM(s.scoreValue) DESC")
    List<Object[]> findLeaderboardBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT s.memberName, SUM(s.scoreValue) FROM Score s GROUP BY s.memberName ORDER BY SUM(s.scoreValue) DESC")
    List<Object[]> findAllTimeLeaderboard();

    List<Score> findByGameDate(LocalDate date);
}
