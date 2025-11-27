package com.library.libraryService.article.controller;

import com.library.libraryService.article.dto.ArticleRequestDto;
import com.library.libraryService.article.dto.ArticleResponseDto;
import com.library.libraryService.article.dto.ArticleUpdateRequestDto;
import com.library.libraryService.article.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/articles")
@Tag(name = "Articles", description = "Main API for managing article by role")
@SecurityRequirement(name = "bearer-jwt")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Operation(summary = "Create new article, author id auto generate form token login except super admin, and isPublic auto false if not filled")
    @PostMapping("/create")
    public ResponseEntity<ArticleResponseDto> createArticle(@Valid @RequestBody ArticleRequestDto request) {
        return ResponseEntity.ok(articleService.createArticle(request));
    }

    @Operation(summary = "Get all article then sorted using bubble sort algorithm")
    @GetMapping("/getAll/{sortType}")
    public ResponseEntity<List<ArticleResponseDto>> getAllArticles(@PathVariable @NotNull(message = "sortType wajib diisi") @NotBlank(message = "sortType tidak boleh kosong") String sortType) {
        return ResponseEntity.ok(articleService.getAllArticles(sortType));
    }

    @Operation(summary = "Update article controled by role, author id auto generate form token login except super admin, and isPublic auto false if not filled")
    @PostMapping("/update")
    public ResponseEntity<ArticleResponseDto> updateArticle(@Valid @RequestBody ArticleUpdateRequestDto request) {
        return ResponseEntity.ok(articleService.updateArticle(request));
    }

    @Operation(summary = "Delete article controled by role")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable @NotNull(message = "ID wajib diisi") @NotBlank(message = "ID tidak boleh kosong") String id) {
        articleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
