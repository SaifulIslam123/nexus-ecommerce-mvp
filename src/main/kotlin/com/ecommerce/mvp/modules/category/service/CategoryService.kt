package com.ecommerce.mvp.modules.category.service

import com.ecommerce.mvp.common.cache.CacheNames
import com.ecommerce.mvp.common.exception.ResourceAlreadyExistException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.category.dto.CategoryDto
import com.ecommerce.mvp.modules.category.dto.CategoryTreeResponseDto
import com.ecommerce.mvp.modules.category.dto.toDto
import com.ecommerce.mvp.modules.category.dto.toEntity
import com.ecommerce.mvp.modules.category.dto.toTreeDto
import com.ecommerce.mvp.modules.category.entity.Category
import com.ecommerce.mvp.modules.category.repository.CategoryRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    fun findAll(): List<Category> {
        return categoryRepository.findAll()
    }

    @Cacheable(cacheNames = [CacheNames.CATEGORIES], key = "'tree'")
    @Transactional
    fun getCategoryTree(): List<CategoryTreeResponseDto> {
        return categoryRepository.findAllByParentIsNull().map { it.toTreeDto() }
    }

    fun findByName(name: String): Category? {
        return categoryRepository.findByName(name).orElse(null)
    }

    @Cacheable(cacheNames = [CacheNames.CATEGORIES_SINGLE], key = "#id")
    fun findById(id: Long): Category? {
        return categoryRepository.findById(id).orElse(null)
    }

    @Caching(evict = [
        CacheEvict(cacheNames = [CacheNames.CATEGORIES], key = "'tree'"),
        CacheEvict(cacheNames = [CacheNames.CATEGORIES_SINGLE], allEntries = true)
    ])
    @Transactional
    fun createCategory(categoryDto: CategoryDto): CategoryDto {
        // Check if category with same name already exists
        if (categoryRepository.existsByName(categoryDto.name?:"")) {
            throw ResourceAlreadyExistException("Category with name '${categoryDto.name}' already exists")
        }

        val category = categoryDto.toEntity()
        val savedCategory = categoryRepository.save(category)
        return savedCategory.toDto()
    }

    @Caching(evict = [
        CacheEvict(cacheNames = [CacheNames.CATEGORIES], key = "'tree'"),
        CacheEvict(cacheNames = [CacheNames.CATEGORIES_SINGLE], key = "#id")
    ])
    @Transactional
    fun updateCategory(id: Long, categoryDto: CategoryDto): CategoryDto {
        val existingCategory = categoryRepository.findById(id).orElse(null)
            ?: run { throw ResourceNotFoundException("Category with id $id not found") }

        categoryDto.name?.let { name ->
         /*   // Check if updating to a name that already exists (excluding current category)
            if (name != existingCategory.name && categoryRepository.existsByName(name)) {
                throw ResourceAlreadyExistException("Category with name '${name}' already exists")
            }*/
            existingCategory.name = name
        }

        categoryDto.description?.let { description ->
            existingCategory.description = description
        }

        return existingCategory.toDto()
    }

    @Caching(evict = [
        CacheEvict(cacheNames = [CacheNames.CATEGORIES], key = "'tree'"),
        CacheEvict(cacheNames = [CacheNames.CATEGORIES_SINGLE], key = "#id")
    ])
    @Transactional
    fun deleteById(id: Long) {
        if (!categoryRepository.existsById(id)) {
            throw ResourceNotFoundException("Category with id $id not found")
        }
        categoryRepository.deleteById(id)
        //return getCategoryTree()
    }

}
