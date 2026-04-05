package com.ecommerce.mvp.modules.category

import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.category.dto.CategoryDto
import com.ecommerce.mvp.modules.category.dto.CategoryTreeResponseDto
import com.ecommerce.mvp.modules.category.dto.toDto
import com.ecommerce.mvp.modules.category.service.CategoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getAllCategories(): List<CategoryTreeResponseDto> {
        return categoryService.getCategoryTree()
    }

    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): CategoryDto {

        return categoryService.findById(id)?.toDto() ?: throw ResourceNotFoundException("Category with id $id not found")
    }

    @PostMapping("/create")
    fun createCategory(@Valid @RequestBody categoryDto: CategoryDto): CategoryDto {
        return categoryService.createCategory(categoryDto)
    }

    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @RequestBody categoryDto: CategoryDto
    ): CategoryDto {
        return categoryService.updateCategory(id, categoryDto)
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): List<CategoryTreeResponseDto> {
        return categoryService.deleteById(id)
    }
}