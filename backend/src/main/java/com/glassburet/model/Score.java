package com.glassburet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String memberName;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private GameName gameName;

    @Column(nullable = false)
    private int scoreValue;

    @NotNull
    @Column(nullable = false)
    private LocalDate gameDate;

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
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
