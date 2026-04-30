package com.glassburet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "scores", uniqueConstraints = @UniqueConstraint(columnNames = {"member_name", "game_name", "puzzle_id"}))
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "game_name", nullable = false)
    private GameName gameName;

    @Column(nullable = false)
    private int scoreValue;

    @NotNull
    @Column(name = "game_date", nullable = false)
    private LocalDate gameDate;

    @Column(name = "puzzle_id")
    private Long puzzleId;

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    public Score() {}

    public Long getId() { return id; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public GameName getGameName() { return gameName; }
    public void setGameName(GameName gameName) { this.gameName = gameName; }
    public int getScoreValue() { return scoreValue; }
    public void setScoreValue(int scoreValue) { this.scoreValue = scoreValue; }
    public LocalDate getGameDate() { return gameDate; }
    public void setGameDate(LocalDate gameDate) { this.gameDate = gameDate; }
    public Long getPuzzleId() { return puzzleId; }
    public void setPuzzleId(Long puzzleId) { this.puzzleId = puzzleId; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
