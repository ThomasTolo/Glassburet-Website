package com.glassburet.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ConnectionsPuzzleResponse {

    private Long id;
    private String createdBy;
    private String title;
    private LocalDateTime createdAt;
    private List<Group> groups;
    private boolean isDaily;
    private LocalDate puzzleDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Group> getGroups() { return groups; }
    public void setGroups(List<Group> groups) { this.groups = groups; }
    public boolean isIsDaily() { return isDaily; }
    public void setIsDaily(boolean isDaily) { this.isDaily = isDaily; }
    public LocalDate getPuzzleDate() { return puzzleDate; }
    public void setPuzzleDate(LocalDate puzzleDate) { this.puzzleDate = puzzleDate; }

    public static class Group {
        private final String category;
        private final List<String> words;
        private final int difficulty;

        public Group(String category, List<String> words, int difficulty) {
            this.category = category;
            this.words = words;
            this.difficulty = difficulty;
        }

        public String getCategory() { return category; }
        public List<String> getWords() { return words; }
        public int getDifficulty() { return difficulty; }
    }
}
