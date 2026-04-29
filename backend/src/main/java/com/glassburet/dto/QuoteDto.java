package com.glassburet.dto;

import jakarta.validation.constraints.NotBlank;

public class QuoteDto {

    @NotBlank
    private String text;

    @NotBlank
    private String author;

    private boolean featured;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
}
