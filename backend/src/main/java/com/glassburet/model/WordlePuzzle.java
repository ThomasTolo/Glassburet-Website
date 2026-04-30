package com.glassburet.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wordle_puzzles")
public class WordlePuzzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String word;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_daily")
    private Boolean isDaily = false;

    @Column(name = "puzzle_date")
    private LocalDate puzzleDate;

    public WordlePuzzle() {}

    public Long getId() { return id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Boolean getIsDaily() { return isDaily; }
    public void setIsDaily(Boolean isDaily) { this.isDaily = isDaily; }
    public LocalDate getPuzzleDate() { return puzzleDate; }
    public void setPuzzleDate(LocalDate puzzleDate) { this.puzzleDate = puzzleDate; }
}
