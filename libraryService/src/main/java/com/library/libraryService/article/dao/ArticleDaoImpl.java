package com.library.libraryService.article.dao;

import com.library.libraryService.article.entity.Article;
import com.library.libraryService.article.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ArticleDaoImpl implements ArticleDao {
    
    @Autowired
    ArticleRepository articleRepository;

    @Override
    public Article save(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Override
    public Article findById(String id) {
        return articleRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Article article) {
        articleRepository.delete(article);
    }

    @Override
    public List<Article> findByIsPublicTrue() {
        return articleRepository.findByIsPublicTrue();
    }
}
