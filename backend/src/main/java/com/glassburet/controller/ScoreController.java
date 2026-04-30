package com.glassburet.controller;

import com.glassburet.dto.ScoreSubmitDto;
import com.glassburet.dto.LeaderboardEntryDto;
import com.glassburet.model.Score;
import com.glassburet.realtime.SiteUpdateBroadcaster;
import com.glassburet.service.ScoreService;
import com.glassburet.dto.ScoreSubmissionResult;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private SiteUpdateBroadcaster siteUpdateBroadcaster;

    @PostMapping
    public ResponseEntity<Score> submitScore(@RequestBody ScoreSubmitDto scoreSubmitDto, Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        scoreSubmitDto.setMemberName(authentication.getName());
        try {
            ScoreSubmissionResult result = scoreService.submit(scoreSubmitDto);
            if (result.isCreated()) {
                siteUpdateBroadcaster.publish("scores");
                return ResponseEntity.status(HttpStatus.CREATED).body(result.getScore());
            }
            return ResponseEntity.ok(result.getScore());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(@RequestParam(defaultValue = "weekly") String period, @RequestParam(required = false) String game) {
        return ResponseEntity.ok(scoreService.getLeaderboard(period, game));
    }

    @GetMapping("/daily/{date}")
    public ResponseEntity<List<Score>> getScoresForDate(@PathVariable LocalDate date) {
        return ResponseEntity.ok(scoreService.getScoresForDate(date));
    }

    @GetMapping("/completed")
    public ResponseEntity<List<Long>> getCompleted(@RequestParam String memberName, @RequestParam String gameName, Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(scoreService.getCompletedPuzzleIds(authentication.getName(), gameName));
    }
}
