package com.library.libraryService.article.service;

import com.library.libraryService.article.dao.ArticleDao;
import com.library.libraryService.article.dto.ArticleRequestDto;
import com.library.libraryService.article.dto.ArticleResponseDto;
import com.library.libraryService.article.dto.ArticleUpdateRequestDto;
import com.library.libraryService.article.entity.Article;
import com.library.libraryService.audit.service.AuditLogService;
import com.library.libraryService.user.dao.UserDao;
import com.library.libraryService.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    ArticleDao articleDao;

    @Autowired
    UserDao  userDao;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @CacheEvict(value = "articles", allEntries = true)
    public ArticleResponseDto createArticle(ArticleRequestDto request) {

        User user = getCurrentUser();
        String role = user.getRole();

        if ("VIEWER".equals(role)) throw new AccessDeniedException("Viewer tidak bisa membuat articles");

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        if ("SUPER_ADMIN".equals(role)){
            article.setAuthorId(request.getAuthorId() != null && !request.getAuthorId().isBlank() ? request.getAuthorId() : user.getId());
        }else{
            article.setAuthorId(user.getId());
        }

        article.setPublic(request.isPublicArticle());
        article.setCreatedAt(new Date());
        article.setUpdatedAt(new Date());


        Article articleSaved = articleDao.save(article);

        auditLogService.log("CREATE_ARTICLE","ARTICLE",articleSaved.getId());

        return responseDtoMapping(articleSaved);
    }

    @Override
    @CacheEvict(value = "articles", allEntries = true)
    public ArticleResponseDto updateArticle(ArticleUpdateRequestDto request) {
        User user = getCurrentUser();
        String role = user.getRole();
        Article article = articleDao.findById(request.getId());

        switch (role) {
            case "SUPER_ADMIN" -> {
                article.setAuthorId(request.getAuthorId() != null && !request.getAuthorId().isBlank() ? request.getAuthorId() : article.getAuthorId());
            }
            case "EDITOR", "CONTRIBUTOR" -> {
                if (!isOwner(article, user)) {
                    throw new AccessDeniedException("Kamu hanya bisa mengubah artikelmu sendiri");
                }
            }
            default -> throw new AccessDeniedException("Kamu tidak bisa mengubah artikel");
        }

        article.setPublic(request.isPublicArticle());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setUpdatedAt(new Date());

        Article articleSaved = articleDao.save(article);

        auditLogService.log("UPDATE_ARTICLE","ARTICLE",articleSaved.getId());

        return responseDtoMapping(articleSaved);
    }

    @Override
    @Cacheable(value = "articles", key = "'all_'+#sortType")
    public List<ArticleResponseDto> getAllArticles(String sortType) {
        User user = getCurrentUser();
        String role = user.getRole();

        ArrayList<ArticleResponseDto> articleResponseDtos;

        if ("VIEWER".equals(role)) {
            articleResponseDtos = articleDao.findByIsPublicTrue().stream()
                    .map(this::responseDtoMapping).collect(Collectors.toCollection(ArrayList::new));
        } else {
            articleResponseDtos = articleDao.findAll().stream()
                    .map(this::responseDtoMapping).collect(Collectors.toCollection(ArrayList::new));
        }

        bubbleSort(articleResponseDtos, sortType);

        auditLogService.log("GET_ALL_ARTICLE","ARTICLE","ALL");

        return articleResponseDtos;
    }

    @Override
    @CacheEvict(value = "articles", allEntries = true)
    public void deleteById(String id) {
        User user = getCurrentUser();
        String role = user.getRole();
        Article article = articleDao.findById(id);
        if (article == null) {
            throw new RuntimeException("Article tidak ditemukan");
        }

        switch (role) {
            case "SUPER_ADMIN" -> {
            }
            case "EDITOR" -> {
                if (!isOwner(article, user)) {
                    throw new AccessDeniedException("Kamu hanya bisa menhapus artikelmu sendiri");
                }
            }
            default -> throw new AccessDeniedException("Kamu tidak bisa menghapus artikel");
        }

        auditLogService.log("DELETE_ARTICLE","ARTICLE",id);

        articleDao.delete(article);
    }

    private ArticleResponseDto responseDtoMapping(Article article) {
        return new ArticleResponseDto(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getAuthorId(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }

    private void bubbleSort(ArrayList<ArticleResponseDto> list, String sortType) {
        int size = list.size();
        boolean swapped;

        if (sortType.equals("asc")) {
            for (int i = 0; i < size - 1; i++) {
                swapped = false;
                for (int j = 0; j < size - 1 - i; j++) {
                    if (list.get(j).getCreatedAt().after(list.get(j + 1).getCreatedAt())) {
                        ArticleResponseDto temp = list.get(j);
                        list.set(j, list.get(j + 1));
                        list.set(j + 1, temp);
                        swapped = true;
                    }
                }
                if (!swapped) break;
            }
        }else if (sortType.equals("desc")) {
            for (int i = 0; i < size - 1; i++) {
                swapped = false;
                for (int j = 0; j < size - 1 - i; j++) {
                    if (list.get(j).getCreatedAt().before(list.get(j + 1).getCreatedAt())) {
                        ArticleResponseDto temp = list.get(j);
                        list.set(j, list.get(j + 1));
                        list.set(j + 1, temp);
                        swapped = true;
                    }
                }
                if (!swapped) break;
            }
        }else{
            throw new RuntimeException("Format Sorting Salah");
        }
    }

    private Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private User getCurrentUser() {
        Authentication auth = getAuth();
        String loginUserName = auth != null ? auth.getName() : null;
        User user = new User();
        if (loginUserName != null) {
            user = userDao.findByUsernameEmail(loginUserName).orElse(null);
        }
        return user;
    }

    private boolean isOwner(Article article, User user) {
        return article.getAuthorId().equals(user.getId());
    }
}
