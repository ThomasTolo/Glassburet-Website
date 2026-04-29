package com.glassburet.service;

import com.glassburet.dto.LeaderboardEntryDto;
import com.glassburet.dto.ScoreSubmitDto;
import com.glassburet.model.Score;
import com.glassburet.repository.ScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;

    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    @Transactional
    public Score submit(ScoreSubmitDto dto) {
        Score score = new Score();
        score.setMemberName(dto.getMemberName());
        score.setGameName(dto.getGameName());
        score.setScoreValue(dto.getScoreValue());
        score.setGameDate(dto.getGameDate());
        return scoreRepository.save(score);
    }

    public List<LeaderboardEntryDto> getLeaderboard(String period) {
        List<Object[]> rows = switch (period) {
            case "weekly" -> {
                LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate sunday = monday.plusDays(6);
                yield scoreRepository.findLeaderboardBetween(monday, sunday);
            }
            case "monthly" -> {
                LocalDate first = LocalDate.now().withDayOfMonth(1);
                LocalDate last = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
                yield scoreRepository.findLeaderboardBetween(first, last);
            }
            case "alltime" -> scoreRepository.findAllTimeLeaderboard();
            default -> scoreRepository.findDailyLeaderboard(LocalDate.now());
        };

        return rows.stream()
                .map(row -> new LeaderboardEntryDto((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<Score> getScoresForDate(LocalDate date) {
        return scoreRepository.findByGameDate(date);
    }
}
