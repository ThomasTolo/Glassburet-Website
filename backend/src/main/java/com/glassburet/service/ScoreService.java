package com.glassburet.service;

import com.glassburet.dto.LeaderboardEntryDto;
import com.glassburet.dto.ScoreSubmitDto;
import com.glassburet.model.GameName;
import com.glassburet.model.Score;
import com.glassburet.dto.ScoreSubmissionResult;
import org.springframework.dao.DataIntegrityViolationException;
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
    public ScoreSubmissionResult submit(ScoreSubmitDto dto) {
        if (dto.getPuzzleId() != null) {
            var existing = scoreRepository.findByMemberNameAndGameNameAndPuzzleId(
                    dto.getMemberName(), dto.getGameName(), dto.getPuzzleId());
            if (existing.isPresent()) {
                return new ScoreSubmissionResult(existing.get(), false);
            }
        } else {
            var existing = scoreRepository.findByMemberNameAndGameNameAndGameDate(
                    dto.getMemberName(), dto.getGameName(), dto.getGameDate());
            if (existing.isPresent()) {
                return new ScoreSubmissionResult(existing.get(), false);
            }
        }

        Score score = new Score();
        score.setMemberName(dto.getMemberName());
        score.setGameName(dto.getGameName());
        score.setScoreValue(dto.getScoreValue());
        score.setGameDate(dto.getGameDate());
        score.setPuzzleId(dto.getPuzzleId());
        try {
            Score saved = scoreRepository.save(score);
            return new ScoreSubmissionResult(saved, true);
        } catch (DataIntegrityViolationException e) {
            // Handle duplicate key violations for both puzzle-id and game-date scenarios
            if (dto.getPuzzleId() != null) {
                var found = scoreRepository.findByMemberNameAndGameNameAndPuzzleId(
                        dto.getMemberName(), dto.getGameName(), dto.getPuzzleId());
                if (found.isPresent()) return new ScoreSubmissionResult(found.get(), false);
            } else {
                var found = scoreRepository.findByMemberNameAndGameNameAndGameDate(
                        dto.getMemberName(), dto.getGameName(), dto.getGameDate());
                if (found.isPresent()) return new ScoreSubmissionResult(found.get(), false);
            }
            throw e;
        }
    }

    public List<Long> getCompletedPuzzleIds(String memberName, String game) {
        try {
            GameName gameName = GameName.valueOf(game);
            return scoreRepository.findByMemberNameAndGameName(memberName, gameName)
                    .stream()
                    .map(Score::getPuzzleId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public List<LeaderboardEntryDto> getLeaderboard(String period, String game) {
        GameName gameName = null;
        if (game != null && !game.isBlank()) {
            try {
                gameName = GameName.valueOf(game);
            } catch (IllegalArgumentException ignored) {
                return List.of();
            }
        }

        List<Object[]> rows = switch (period) {
            case "weekly" -> {
                LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate sunday = monday.plusDays(6);
                yield gameName == null
                        ? scoreRepository.findLeaderboardBetween(monday, sunday)
                        : scoreRepository.findLeaderboardBetweenByGame(monday, sunday, gameName);
            }
            case "monthly" -> {
                LocalDate first = LocalDate.now().withDayOfMonth(1);
                LocalDate last = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
                yield gameName == null
                        ? scoreRepository.findLeaderboardBetween(first, last)
                        : scoreRepository.findLeaderboardBetweenByGame(first, last, gameName);
            }
            case "alltime" -> gameName == null
                    ? scoreRepository.findAllTimeLeaderboard()
                    : scoreRepository.findAllTimeLeaderboardByGame(gameName);
            default -> gameName == null
                    ? scoreRepository.findDailyLeaderboard(LocalDate.now())
                    : scoreRepository.findDailyLeaderboardByGame(LocalDate.now(), gameName);
        };

        return rows.stream()
                .map(row -> new LeaderboardEntryDto((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<Score> getScoresForDate(LocalDate date) {
        return scoreRepository.findByGameDate(date);
    }
}
