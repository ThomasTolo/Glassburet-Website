package com.glassburet.repository;

import com.glassburet.model.Score;
import com.glassburet.model.GameName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query("SELECT s.memberName, SUM(s.scoreValue) FROM Score s WHERE s.gameDate = :date GROUP BY s.memberName ORDER BY SUM(s.scoreValue) DESC")
    List<Object[]> findDailyLeaderboard(@Param("date") LocalDate date);

    @Query("SELECT s.memberName, SUM(s.scoreValue) FROM Score s WHERE s.gameDate = :date AND s.gameName = :gameName GROUP BY s.memberName ORDER BY SUM(s.scoreValue) DESC")
    List<Object[]> findDailyLeaderboardByGame(@Param("date") LocalDate date, @Param("gameName") GameName gameName);

    @Query("SELECT s.memberName, SUM(s.scoreValue) FROM Score s WHERE s.gameDate BETWEEN :start AND :end GROUP BY s.memberName ORDER BY SUM(s.scoreValue) DESC")
    List<Object[]> findLeaderboardBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT s.memberName, SUM(s.scoreValue) FROM Score s WHERE s.gameDate BETWEEN :start AND :end AND s.gameName = :gameName GROUP BY s.memberName ORDER BY SUM(s.scoreValue) DESC")
    List<Object[]> findLeaderboardBetweenByGame(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("gameName") GameName gameName);

    @Query("SELECT s.memberName, SUM(s.scoreValue) FROM Score s GROUP BY s.memberName ORDER BY SUM(s.scoreValue) DESC")
    List<Object[]> findAllTimeLeaderboard();

    @Query("SELECT s.memberName, SUM(s.scoreValue) FROM Score s WHERE s.gameName = :gameName GROUP BY s.memberName ORDER BY SUM(s.scoreValue) DESC")
    List<Object[]> findAllTimeLeaderboardByGame(@Param("gameName") GameName gameName);

    List<Score> findByGameDate(LocalDate date);

    java.util.Optional<Score> findByMemberNameAndGameNameAndGameDate(String memberName, GameName gameName, LocalDate gameDate);

    java.util.Optional<Score> findByMemberNameAndGameNameAndPuzzleId(String memberName, GameName gameName, Long puzzleId);

    List<Score> findByMemberNameAndGameName(String memberName, GameName gameName);
}
