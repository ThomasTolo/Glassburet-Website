package com.glassburet.dto;

import jakarta.validation.constraints.NotBlank;

public class QuoteDto {

    @NotBlank
    private String text;

    @NotBlank
    private String author;

    // Optional createdAt in ISO_LOCAL_DATE_TIME format (e.g. 2026-04-30T14:30)
    private String createdAt;

    private boolean featured;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
