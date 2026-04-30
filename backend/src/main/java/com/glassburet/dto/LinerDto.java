package com.glassburet.dto;

import jakarta.validation.constraints.NotBlank;

public class LinerDto {

    @NotBlank
    private String text;

    private String author;
    private Integer sinceYear;
    private int number;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Integer getSinceYear() { return sinceYear; }
    public void setSinceYear(Integer sinceYear) { this.sinceYear = sinceYear; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
}
