package com.glassburet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "connections_puzzles")
public class ConnectionsPuzzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false) private String group0Category;
    @Column(nullable = false, columnDefinition = "TEXT") private String group0Words;
    @Column(nullable = false) private String group1Category;
    @Column(nullable = false, columnDefinition = "TEXT") private String group1Words;
    @Column(nullable = false) private String group2Category;
    @Column(nullable = false, columnDefinition = "TEXT") private String group2Words;
    @Column(nullable = false) private String group3Category;
    @Column(nullable = false, columnDefinition = "TEXT") private String group3Words;

    public ConnectionsPuzzle() {}

    public Long getId() { return id; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getGroup0Category() { return group0Category; }
    public void setGroup0Category(String c) { this.group0Category = c; }
    public String getGroup0Words() { return group0Words; }
    public void setGroup0Words(String w) { this.group0Words = w; }

    public String getGroup1Category() { return group1Category; }
    public void setGroup1Category(String c) { this.group1Category = c; }
    public String getGroup1Words() { return group1Words; }
    public void setGroup1Words(String w) { this.group1Words = w; }

    public String getGroup2Category() { return group2Category; }
    public void setGroup2Category(String c) { this.group2Category = c; }
    public String getGroup2Words() { return group2Words; }
    public void setGroup2Words(String w) { this.group2Words = w; }

    public String getGroup3Category() { return group3Category; }
    public void setGroup3Category(String c) { this.group3Category = c; }
    public String getGroup3Words() { return group3Words; }
    public void setGroup3Words(String w) { this.group3Words = w; }
}
