package com.library.libraryService.article.service;

import com.library.libraryService.article.dto.ArticleRequestDto;
import com.library.libraryService.article.dto.ArticleResponseDto;
import com.library.libraryService.article.dto.ArticleUpdateRequestDto;

import java.util.List;

public interface ArticleService {
    ArticleResponseDto createArticle(ArticleRequestDto request);
    List<ArticleResponseDto> getAllArticles(String sortType);
    ArticleResponseDto updateArticle(ArticleUpdateRequestDto request);
    void deleteById(String id);
}
