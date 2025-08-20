package com.example.ocrproject.repository;

import com.example.ocrproject.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByTitleContainingIgnoreCase(String title);

    // @Lob(CLOB) 컬럼은 IgnoreCase 파생 메소드가 깨지므로, 캐스트 + lower로 처리
    @Query("""
           select r
           from Recipe r
           where lower(cast(r.ingredients as string)) like lower(concat('%', :ingredient, '%'))
           """)
    List<Recipe> searchByIngredient(@Param("ingredient") String ingredient);
}