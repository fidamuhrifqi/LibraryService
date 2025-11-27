package com.library.libraryService.article.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.libraryService.article.dto.ArticleRequestDto;
import com.library.libraryService.article.dto.ArticleResponseDto;
import com.library.libraryService.article.dto.ArticleUpdateRequestDto;
import com.library.libraryService.article.service.ArticleService;
import com.library.libraryService.security.jwt.JwtUtil;
import com.library.libraryService.security.ratelimit.RateLimiterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ArticleController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /articles/create sukses create article")
    void createArticle_success() throws Exception {
        ArticleRequestDto requestDto = new ArticleRequestDto();
        requestDto.setTitle("artikel fida");
        requestDto.setContent("Konten fida");
        requestDto.setPublicArticle(true);

        ArticleResponseDto responseDto = new ArticleResponseDto();
        responseDto.setId("123123");
        responseDto.setTitle("artikel fida");
        responseDto.setContent("Konten fida");

        Mockito.when(articleService.createArticle(any(ArticleRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(
                        post("/articles/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value("123123"));
    }

    @Test
    @DisplayName("GET /articles/getAll/{sortType} sukses get semua article")
    void getAllArticles_success() throws Exception {
        ArticleResponseDto article1 = new ArticleResponseDto();
        ArticleResponseDto article2 = new ArticleResponseDto();
        article1.setId("1");
        article2.setId("2");

        List<ArticleResponseDto> responseList = List.of(article1, article2);

        Mockito.when(articleService.getAllArticles("asc"))
                .thenReturn(responseList);

        mockMvc.perform(
                        get("/articles/getAll/{sortType}", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /articles/getAll/{sortType} validasi sortType kosong error 400")
    void getAllArticles_validationError_blankSortType() throws Exception {
        mockMvc.perform(
                        get("/articles/getAll/{sortType}", " ")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /articles/update sukses update article")
    void updateArticle_success() throws Exception {

        ArticleUpdateRequestDto requestDto = new ArticleUpdateRequestDto();

        requestDto.setId("article-1");
        requestDto.setTitle("Judul Baru");
        requestDto.setContent("Konten Baru");

        ArticleResponseDto responseDto = new ArticleResponseDto();
        responseDto.setId("article-1");
        responseDto.setTitle("Judul Baru");
        responseDto.setContent("Konten Baru");

        Mockito.when(articleService.updateArticle(any(ArticleUpdateRequestDto.class)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(
                        post("/articles/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /articles/delete/{id} sukses delete article")
    void deleteArticle_validationError_blankId() throws Exception {
        mockMvc.perform(
                        delete("/articles/delete/{id}", "123123")
                )
                .andExpect(status().isNoContent());
    }
}
