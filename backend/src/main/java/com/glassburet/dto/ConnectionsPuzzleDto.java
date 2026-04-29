package com.glassburet.dto;

import java.util.List;

public class ConnectionsPuzzleDto {

    private String createdBy;
    private List<GroupDto> groups;

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public List<GroupDto> getGroups() { return groups; }
    public void setGroups(List<GroupDto> groups) { this.groups = groups; }

    public static class GroupDto {
        private String category;
        private List<String> words;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public List<String> getWords() { return words; }
        public void setWords(List<String> words) { this.words = words; }
    }
}
