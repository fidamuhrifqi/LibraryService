package com.library.libraryService.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleResponseDto {

    private String id;
    private String title;
    private String content;
    private String authorId;
    private Date createdAt;
    private Date updatedAt;
}
