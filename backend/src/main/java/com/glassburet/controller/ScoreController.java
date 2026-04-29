package com.glassburet.controller;

import com.glassburet.dto.ScoreSubmitDto;
import com.glassburet.dto.LeaderboardEntryDto;
import com.glassburet.model.Score;
import com.glassburet.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @PostMapping
    public ResponseEntity<Score> submitScore(@RequestBody ScoreSubmitDto scoreSubmitDto) {
        return ResponseEntity.ok(scoreService.submit(scoreSubmitDto));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(@RequestParam(defaultValue = "weekly") String period) {
        return ResponseEntity.ok(scoreService.getLeaderboard(period));
    }

    @GetMapping("/daily/{date}")
    public ResponseEntity<List<Score>> getScoresForDate(@PathVariable LocalDate date) {
        return ResponseEntity.ok(scoreService.getScoresForDate(date));
    }
}
