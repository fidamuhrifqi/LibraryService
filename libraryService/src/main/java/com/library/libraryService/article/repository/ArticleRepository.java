package com.library.libraryService.article.repository;

import com.library.libraryService.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, String> {
    List<Article> findByIsPublicTrue();
}
