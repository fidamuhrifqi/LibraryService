package com.library.libraryService.article.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "article")
@AllArgsConstructor
@NoArgsConstructor
public class Article{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String title;
    private String content;
    private String authorId;
    private Date createdAt;
    private Date updatedAt;
    private boolean isPublic;
}
