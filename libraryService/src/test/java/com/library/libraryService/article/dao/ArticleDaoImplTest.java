package com.library.libraryService.article.dao;

import com.library.libraryService.article.entity.Article;
import com.library.libraryService.article.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ArticleDaoImplTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleDaoImpl articleDao;

    @Test
    @DisplayName("save() sukses get dao save")
    void save_shouldCallRepositorySave() {
        Article article = new Article();
        Mockito.when(articleRepository.save(any(Article.class))).thenReturn(article);

        Article result = articleDao.save(article);

        assertThat(result).isSameAs(article);
        Mockito.verify(articleRepository).save(eq(article));
    }

    @Test
    @DisplayName("findAll() sukses get dao findAll")
    void findAll_shouldReturnListFromRepository() {
        Article a1 = new Article();
        Article a2 = new Article();
        List<Article> list = List.of(a1, a2);

        Mockito.when(articleRepository.findAll()).thenReturn(list);

        List<Article> result = articleDao.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(a1, a2);
        Mockito.verify(articleRepository).findAll();
    }

    @Test
    @DisplayName("findById() sukses get dao findById")
    void findById_shouldReturnArticle_whenPresent() {
        String id = "article-1";
        Article article = new Article();

        Mockito.when(articleRepository.findById(id)).thenReturn(Optional.of(article));

        Article result = articleDao.findById(id);

        assertThat(result).isSameAs(article);
        Mockito.verify(articleRepository).findById(id);
    }

    @Test
    @DisplayName("findById() return null")
    void findById_shouldReturnNull_whenNotPresent() {
        String id = "not-found";

        Mockito.when(articleRepository.findById(id)).thenReturn(Optional.empty());

        Article result = articleDao.findById(id);

        assertThat(result).isNull();
        Mockito.verify(articleRepository).findById(id);
    }

    @Test
    @DisplayName("delete() sukses get dao delete")
    void delete_shouldCallRepositoryDelete() {
        Article article = new Article();

        articleDao.delete(article);

        Mockito.verify(articleRepository).delete(article);
    }

    @Test
    @DisplayName("findByIsPublicTrue() sukses get dao findByIsPublicTrue")
    void findByIsPublicTrue_shouldReturnListFromRepository() {
        Article a1 = new Article();
        Article a2 = new Article();
        List<Article> list = List.of(a1, a2);

        Mockito.when(articleRepository.findByIsPublicTrue()).thenReturn(list);

        List<Article> result = articleDao.findByIsPublicTrue();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(a1, a2);
        Mockito.verify(articleRepository).findByIsPublicTrue();
    }
}
