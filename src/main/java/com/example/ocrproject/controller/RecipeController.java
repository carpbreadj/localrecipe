package com.example.ocrproject.controller;

import com.example.ocrproject.entity.Recipe;
import com.example.ocrproject.repository.RecipeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeRepository repo;

    public RecipeController(RecipeRepository repo) {
        this.repo = repo;
    }

    // 목록
    @GetMapping
    public List<Recipe> list() {
        return repo.findAll();
    }

    // 검색: q(제목) 또는 ingredient(재료) 둘 중 하나/둘 다 가능
    @GetMapping("/search")
    public List<Recipe> search(@RequestParam(required=false) String q,
                               @RequestParam(required=false) String ingredient) {
        // 둘 다 비어있으면 전체 리턴
        if ((q == null || q.isBlank()) && (ingredient == null || ingredient.isBlank())) {
            return repo.findAll();
        }
        Set<Recipe> result = new LinkedHashSet<>();
        if (q != null && !q.isBlank()) {
            result.addAll(repo.findByTitleContainingIgnoreCase(q.trim()));
        }
        if (ingredient != null && !ingredient.isBlank()) {
            result.addAll(repo.searchByIngredient(ingredient.trim()));
        }
        return new ArrayList<>(result);
    }

    // 단건
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 생성 (폼 전송: title, ingredients, steps, imageUrl or imageFile)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Recipe> create(@RequestParam String title,
                                         @RequestParam String ingredients,
                                         @RequestParam String steps,
                                         @RequestParam(required=false) String imageUrl,
                                         @RequestParam(required=false, name="imageFile") MultipartFile imageFile) throws Exception {
        Recipe r = new Recipe();
        r.setTitle(title.trim());
        r.setIngredients(ingredients.trim());
        r.setSteps(steps.trim());

        // image 우선 순위: 파일 업로드 > 직접 URL
        if (imageFile != null && !imageFile.isEmpty()) {
            // 내부 업로드 엔드포인트를 재사용해도 되지만, 간단히 여기서 저장
            // (프로덕션에선 중복 로직을 서비스로 빼세요)
            String ext = Optional.ofNullable(imageFile.getOriginalFilename())
                    .filter(fn -> fn.contains("."))
                    .map(fn -> fn.substring(fn.lastIndexOf('.')+1))
                    .orElse("png");
            String name = UUID.randomUUID() + "." + ext;
            java.nio.file.Files.createDirectories(java.nio.file.Path.of("uploads"));
            java.nio.file.Path dest = java.nio.file.Path.of("uploads", name);
            java.nio.file.Files.copy(imageFile.getInputStream(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            r.setImageUrl("/uploads/" + name);
        } else if (imageUrl != null && !imageUrl.isBlank()) {
            r.setImageUrl(imageUrl.trim());
        } else {
            r.setImageUrl(null);
        }

        return ResponseEntity.ok(repo.save(r));
    }

    // 수정(선택사항) – 필요 시 사용
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> update(@PathVariable Long id, @RequestBody Recipe req) {
        return repo.findById(id).map(r -> {
            r.setTitle(req.getTitle());
            r.setIngredients(req.getIngredients());
            r.setSteps(req.getSteps());
            r.setImageUrl(req.getImageUrl());
            return ResponseEntity.ok(repo.save(r));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 삭제(선택)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}