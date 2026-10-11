package com.hostelmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String content;

    @Column(length = 100)
    private String postedBy;

    private LocalDate postedDate;

    private boolean important;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }
    public LocalDate getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }
    public boolean isImportant() { return important; }
    public void setImportant(boolean important) { this.important = important; }
}
