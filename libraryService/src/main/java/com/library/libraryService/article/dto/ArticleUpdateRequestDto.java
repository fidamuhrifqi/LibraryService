package com.library.libraryService.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleUpdateRequestDto {

    @NotBlank(message = "id tidak boleh kosong")
    @NotNull(message = "id wajib diisi")
    private String id;

    @NotBlank(message = "title tidak boleh kosong")
    @NotNull(message = "AuthorId wajib diisi")
    private String title;

    @NotBlank(message = "Content tidak boleh kosong")
    @NotNull(message = "AuthorId wajib diisi")
    private String content;

    private boolean publicArticle;
    private String authorId;
}
