package com.library.libraryService.article.service;

import com.library.libraryService.article.dao.ArticleDao;
import com.library.libraryService.article.dto.ArticleRequestDto;
import com.library.libraryService.article.dto.ArticleResponseDto;
import com.library.libraryService.article.dto.ArticleUpdateRequestDto;
import com.library.libraryService.article.entity.Article;
import com.library.libraryService.audit.service.AuditLogService;
import com.library.libraryService.user.dao.UserDao;
import com.library.libraryService.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleDao articleDao;

    @Mock
    private UserDao userDao;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private void mockLoggedInUser(String username, User user) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        when(userDao.findByUsernameEmail(username)).thenReturn(Optional.of(user));

        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("createArticle VIEWER harus ditolak")
    void createArticle_viewerDenied() {
        User viewer = new User();
        viewer.setId("345345");
        viewer.setUsername("udin");
        viewer.setRole("VIEWER");

        mockLoggedInUser("udin", viewer);

        ArticleRequestDto request = new ArticleRequestDto();
        request.setTitle("test judul");
        request.setContent("test konten");
        request.setAuthorId("11112223333");
        request.setPublicArticle(true);

        assertThrows(AccessDeniedException.class, () -> articleService.createArticle(request));

        verify(articleDao, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any());
    }

    @Test
    @DisplayName("createArticle SUPER_ADMIN boleh override authorId")
    void createArticle_superAdmin() {
        User admin = new User();
        admin.setId("1231312");
        admin.setUsername("fida");
        admin.setRole("SUPER_ADMIN");

        mockLoggedInUser("fida", admin);

        ArticleRequestDto request = new ArticleRequestDto();
        request.setTitle("test judul");
        request.setContent("test konten");
        request.setAuthorId("345345");
        request.setPublicArticle(true);

        when(articleDao.save(any())).thenAnswer(inv -> {
            Article a = inv.getArgument(0);
            a.setId("6767676");
            return a;
        });

        ArticleResponseDto res = articleService.createArticle(request);

        assertThat(res.getAuthorId()).isEqualTo("345345");
        verify(auditLogService).log("CREATE_ARTICLE", "ARTICLE", "6767676");
    }

    @Test
    @DisplayName("createArticle EDITOR pakai authorId dari current user")
    void createArticle_editor() {
        User editor = new User();
        editor.setId("123123");
        editor.setUsername("fida");
        editor.setRole("EDITOR");

        mockLoggedInUser("fida", editor);

        ArticleRequestDto request = new ArticleRequestDto();
        request.setTitle("test judul");
        request.setContent("test konten");
        request.setPublicArticle(false);

        when(articleDao.save(any())).thenAnswer(inv -> {
            Article a = inv.getArgument(0);
            a.setId("777778888");
            return a;
        });

        ArticleResponseDto res = articleService.createArticle(request);

        assertThat(res.getAuthorId()).isEqualTo("123123");
        verify(auditLogService).log("CREATE_ARTICLE", "ARTICLE", "777778888");
    }

    @Test
    @DisplayName("updateArticle SUPER_ADMIN boleh ganti authorId")
    void updateArticle_superAdmin() {
        User admin = new User();
        admin.setId("admin");
        admin.setUsername("fida");
        admin.setRole("SUPER_ADMIN");

        mockLoggedInUser("fida", admin);

        Article a = new Article();
        a.setId("11112222");
        a.setAuthorId("345345");

        when(articleDao.findById("11112222")).thenReturn(a);
        when(articleDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArticleUpdateRequestDto req = new ArticleUpdateRequestDto();
        req.setId("11112222");
        req.setAuthorId("123123");
        req.setTitle("test judul");
        req.setContent("test konten");
        req.setPublicArticle(true);

        ArticleResponseDto res = articleService.updateArticle(req);

        assertThat(res.getAuthorId()).isEqualTo("123123");
        verify(auditLogService).log("UPDATE_ARTICLE", "ARTICLE", "11112222");
    }

    @Test
    @DisplayName("updateArticle SUPER_ADMIN authorId Blank")
    void updateArticle_superAdminAuthorIdBlank() {
        User admin = new User();
        admin.setId("123123");
        admin.setUsername("fida");
        admin.setRole("SUPER_ADMIN");

        mockLoggedInUser("fida", admin);

        Article a = new Article();
        a.setId("11112222");
        a.setAuthorId("345345");

        when(articleDao.findById("11112222")).thenReturn(a);
        when(articleDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArticleUpdateRequestDto req = new ArticleUpdateRequestDto();
        req.setId("11112222");
        req.setAuthorId("");
        req.setTitle("test judul");
        req.setContent("test konten");
        req.setPublicArticle(true);

        ArticleResponseDto res = articleService.updateArticle(req);

        assertThat(res.getAuthorId()).isEqualTo("345345");
        verify(auditLogService).log("UPDATE_ARTICLE", "ARTICLE", "11112222");
    }

    @Test
    @DisplayName("updateArticle SUPER_ADMIN authorId Null")
    void updateArticle_superAdminAuthorIdNull() {
        User admin = new User();
        admin.setId("admin");
        admin.setUsername("fida");
        admin.setRole("SUPER_ADMIN");

        mockLoggedInUser("fida", admin);

        Article a = new Article();
        a.setId("11112222");
        a.setAuthorId("345345");

        when(articleDao.findById("11112222")).thenReturn(a);
        when(articleDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArticleUpdateRequestDto req = new ArticleUpdateRequestDto();
        req.setId("11112222");
        req.setAuthorId(null);
        req.setTitle("test judul");
        req.setContent("test konten");
        req.setPublicArticle(true);

        ArticleResponseDto res = articleService.updateArticle(req);

        assertThat(res.getAuthorId()).isEqualTo("345345");
        verify(auditLogService).log("UPDATE_ARTICLE", "ARTICLE", "11112222");
    }

    @Test
    @DisplayName("updateArticle CONTRIBUTOR artikel sendiri")
    void updateArticle_contributorOwnarticel() {
        User contributor = new User();
        contributor.setId("123123");
        contributor.setUsername("fida");
        contributor.setRole("CONTRIBUTOR");

        mockLoggedInUser("fida", contributor);

        Article a = new Article();
        a.setId("11112222");
        a.setAuthorId("123123");

        when(articleDao.findById("11112222")).thenReturn(a);
        when(articleDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArticleUpdateRequestDto req = new ArticleUpdateRequestDto();
        req.setId("11112222");

        ArticleResponseDto res = articleService.updateArticle(req);

        assertThat(res.getAuthorId()).isEqualTo("123123");
        verify(auditLogService).log("UPDATE_ARTICLE", "ARTICLE", "11112222");
    }

    @Test
    @DisplayName("updateArticle EDITOR artikel sendiri")
    void updateArticle_editorOwnarticel() {
        User editor = new User();
        editor.setId("123123");
        editor.setUsername("fida");
        editor.setRole("EDITOR");

        mockLoggedInUser("fida", editor);

        Article a = new Article();
        a.setId("11112222");
        a.setAuthorId("123123");

        when(articleDao.findById("11112222")).thenReturn(a);
        when(articleDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArticleUpdateRequestDto req = new ArticleUpdateRequestDto();
        req.setId("11112222");

        ArticleResponseDto res = articleService.updateArticle(req);

        assertThat(res.getAuthorId()).isEqualTo("123123");
        verify(auditLogService).log("UPDATE_ARTICLE", "ARTICLE", "11112222");
    }

    @Test
    @DisplayName("updateArticle VIEWER tidak boleh update artikel")
    void updateArticle_viewer() {
        User viewer = new User();
        viewer.setId("123123");
        viewer.setUsername("fida");
        viewer.setRole("VIEWER");

        mockLoggedInUser("fida", viewer);

        Article a = new Article();
        a.setId("11112222");
        a.setAuthorId("321312");

        when(articleDao.findById("11112222")).thenReturn(a);

        ArticleUpdateRequestDto req = new ArticleUpdateRequestDto();
        req.setId("11112222");

        assertThrows(AccessDeniedException.class, () -> articleService.updateArticle(req));
        verify(articleDao, never()).save(any());
    }

    @Test
    @DisplayName("updateArticle EDITOR tidak boleh update artikel orang lain")
    void updateArticle_editor() {
        User editor = new User();
        editor.setId("123123");
        editor.setUsername("fida");
        editor.setRole("EDITOR");

        mockLoggedInUser("fida", editor);

        Article a = new Article();
        a.setId("11112222");
        a.setAuthorId("321312");

        when(articleDao.findById("11112222")).thenReturn(a);

        ArticleUpdateRequestDto req = new ArticleUpdateRequestDto();
        req.setId("11112222");

        assertThrows(AccessDeniedException.class, () -> articleService.updateArticle(req));
        verify(articleDao, never()).save(any());
    }

    @Test
    @DisplayName("getAllArticles VIEWER hanya lihat artikel public")
    void getAllArticles_viewer() {
        User viewer = new User();
        viewer.setId("234234");
        viewer.setUsername("udin");
        viewer.setRole("VIEWER");

        mockLoggedInUser("udin", viewer);

        Article article = new Article();
        article.setId("111111");
        article.setCreatedAt(new Date(System.currentTimeMillis() - 1000));

        Article article2 = new Article();
        article2.setId("222222");
        article2.setCreatedAt(new Date());

        when(articleDao.findByIsPublicTrue()).thenReturn(Arrays.asList(article2, article));

        List<ArticleResponseDto> res = articleService.getAllArticles("asc");

        assertThat(res.get(0).getId()).isEqualTo("111111");
        assertThat(res.get(1).getId()).isEqualTo("222222");

        verify(articleDao).findByIsPublicTrue();
        verify(auditLogService).log("GET_ALL_ARTICLE", "ARTICLE", "ALL");
    }

    @Test
    @DisplayName("getAllArticles non VIEWER lihat semua artikel")
    void getAllArticles_editor() {
        User editor = new User();
        editor.setId("234234");
        editor.setUsername("udin");
        editor.setRole("EDITOR");

        mockLoggedInUser("udin", editor);

        Article article = new Article();
        article.setId("111111");
        article.setCreatedAt(new Date());

        Article article2 = new Article();
        article2.setId("222222");
        article2.setCreatedAt(new Date(System.currentTimeMillis() + 2000));

        when(articleDao.findAll()).thenReturn(Arrays.asList(article, article2));

        List<ArticleResponseDto> res = articleService.getAllArticles("desc");

        assertThat(res.get(1).getId()).isEqualTo("111111");
        assertThat(res.get(0).getId()).isEqualTo("222222");

        verify(articleDao).findAll();
    }

    @Test
    @DisplayName("deleteById SUPER_ADMIN boleh hapus apapun")
    void deleteById_superAdmin() {
        User admin = new User();
        admin.setId("123123");
        admin.setUsername("fida");
        admin.setRole("SUPER_ADMIN");

        mockLoggedInUser("fida", admin);

        Article article = new Article();
        article.setId("11112222");
        article.setAuthorId("123123");

        when(articleDao.findById("11112222")).thenReturn(article);

        articleService.deleteById("11112222");

        verify(articleDao).delete(article);
        verify(auditLogService).log("DELETE_ARTICLE", "ARTICLE", "11112222");
    }

    @Test
    @DisplayName("deleteById SUPER_ADMIN boleh hapus artikel sendiri")
    void deleteById_editor() {
        User editor = new User();
        editor.setId("123123");
        editor.setUsername("fida");
        editor.setRole("EDITOR");

        mockLoggedInUser("fida", editor);

        Article article = new Article();
        article.setId("11112222");
        article.setAuthorId("123123");

        when(articleDao.findById("11112222")).thenReturn(article);

        articleService.deleteById("11112222");

        verify(articleDao).delete(article);
        verify(auditLogService).log("DELETE_ARTICLE", "ARTICLE", "11112222");
    }

    @Test
    @DisplayName("deleteById EDITOR tidak boleh hapus artikel orang lain")
    void deleteById_editorDenied() {
        User editor = new User();
        editor.setId("123123");
        editor.setUsername("fida");
        editor.setRole("EDITOR");

        mockLoggedInUser("fida", editor);

        Article article = new Article();
        article.setId("11112222");
        article.setAuthorId("234234");

        when(articleDao.findById("11112222")).thenReturn(article);

        assertThrows(AccessDeniedException.class, () -> articleService.deleteById("11112222"));
        verify(articleDao, never()).delete(any());
    }

    @Test
    @DisplayName("deleteById VIEWER tidak boleh hapus artikel")
    void deleteById_viewerDenied() {
        User viewer = new User();
        viewer.setId("123123");
        viewer.setUsername("fida");
        viewer.setRole("VIEWER");

        mockLoggedInUser("fida", viewer);

        Article article = new Article();
        article.setId("11112222");
        article.setAuthorId("234234");

        when(articleDao.findById("11112222")).thenReturn(article);

        assertThrows(AccessDeniedException.class, () -> articleService.deleteById("11112222"));
        verify(articleDao, never()).delete(any());
    }

    @Test
    @DisplayName("deleteById artikel tidak ditemukan")
    void deleteById_notFound() {
        User admin = new User();
        admin.setId("123123");
        admin.setUsername("fida");
        admin.setRole("SUPER_ADMIN");

        mockLoggedInUser("fida", admin);

        when(articleDao.findById("1112222")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> articleService.deleteById("1112222"));
        assertThat(ex.getMessage()).isEqualTo("Article tidak ditemukan");
    }
}
