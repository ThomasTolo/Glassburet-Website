package com.glassburet.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "native_puzzles")
public class NativePuzzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_name", nullable = false)
    private GameName gameName;

    @Column(nullable = false)
    private String createdBy;

    private String title;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_daily")
    private Boolean isDaily = false;

    @Column(name = "puzzle_date")
    private LocalDate puzzleDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    public NativePuzzle() {}

    public Long getId() { return id; }
    public GameName getGameName() { return gameName; }
    public void setGameName(GameName gameName) { this.gameName = gameName; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Boolean getIsDaily() { return isDaily; }
    public void setIsDaily(Boolean isDaily) { this.isDaily = isDaily; }
    public LocalDate getPuzzleDate() { return puzzleDate; }
    public void setPuzzleDate(LocalDate puzzleDate) { this.puzzleDate = puzzleDate; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}
