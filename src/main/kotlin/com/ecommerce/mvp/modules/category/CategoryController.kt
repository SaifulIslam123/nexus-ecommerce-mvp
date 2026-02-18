package com.ecommerce.mvp.modules.category

import com.ecommerce.mvp.modules.category.dto.CategoryDto
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
    fun getAllCategories(): ResponseEntity<List<CategoryDto>> {
        val categories = categoryService.findAll()
        val categoryDtos = categories.map { it.toDto() }
        return ResponseEntity.ok(categoryDtos)
    }

    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): ResponseEntity<CategoryDto> {
        val category = categoryService.findById(id)
        return if (category != null) {
            ResponseEntity.ok(category.toDto())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createCategory(@Valid @RequestBody categoryDto: CategoryDto): ResponseEntity<CategoryDto> {
        return try {
            val createdCategory = categoryService.createCategory(categoryDto)
            ResponseEntity.status(HttpStatus.CREATED).body(createdCategory.toDto())
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @Valid @RequestBody categoryDto: CategoryDto
    ): ResponseEntity<CategoryDto> {
        return try {
            val updatedCategory = categoryService.updateCategory(id, categoryDto)
            ResponseEntity.ok(updatedCategory.toDto())
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            categoryService.deleteById(id)
            ResponseEntity.noContent().build()
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}