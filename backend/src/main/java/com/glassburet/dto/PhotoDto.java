package com.glassburet.dto;

import jakarta.validation.constraints.NotBlank;

public class PhotoDto {

    @NotBlank
    private String imageUrl;

    private String caption;
    private String album;
    private String category;
    private String uploadedBy;

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}
