package com.glassburet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "liners")
public class Liner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int number;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private int saidCount = 0;

    private Integer sinceYear;

    private String author;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "liner_likes", joinColumns = @JoinColumn(name = "liner_id"))
    @Column(name = "member_name")
    private Set<String> likes = new HashSet<>();

    public Liner() {}

    public Long getId() { return id; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getSaidCount() { return saidCount; }
    public void setSaidCount(int saidCount) { this.saidCount = saidCount; }
    public Integer getSinceYear() { return sinceYear; }
    public void setSinceYear(Integer sinceYear) { this.sinceYear = sinceYear; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Set<String> getLikes() { return likes; }
    public void setLikes(Set<String> likes) { this.likes = likes; }
}
