package com.glassburet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "photos")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String caption;

    @NotBlank
    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String album = "Glassburet";

    @Column(nullable = false)
    private String category = "Alt";

    private String uploadedBy;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "photo_likes", joinColumns = @JoinColumn(name = "photo_id"))
    @Column(name = "member_name")
    private Set<String> likes = new HashSet<>();

    public Photo() {}

    public Long getId() { return id; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public Set<String> getLikes() { return likes; }
    public void setLikes(Set<String> likes) { this.likes = likes; }
}
