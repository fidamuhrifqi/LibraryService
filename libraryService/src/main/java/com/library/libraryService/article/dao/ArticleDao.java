package com.library.libraryService.article.dao;

import com.library.libraryService.article.entity.Article;

import java.util.List;

public interface ArticleDao  {
    Article save(Article entity);
    List<Article> findAll();
    Article findById(String id);
    void delete(Article article);
    List<Article> findByIsPublicTrue();
}
