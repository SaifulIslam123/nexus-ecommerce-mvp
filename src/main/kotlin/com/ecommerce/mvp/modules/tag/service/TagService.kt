package com.ecommerce.mvp.modules.tag.service

import com.ecommerce.mvp.common.cache.CacheNames
import com.ecommerce.mvp.common.exception.ResourceAlreadyExistException
import com.ecommerce.mvp.common.exception.ResourceNotFoundException
import com.ecommerce.mvp.modules.product.model.entity.Tag
import com.ecommerce.mvp.modules.product.repository.TagRepository
import com.ecommerce.mvp.modules.tag.dto.TagRequestDto
import com.ecommerce.mvp.modules.tag.dto.TagResponseDto
import com.ecommerce.mvp.modules.tag.dto.toResponseDto
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val tagRepository: TagRepository
) {

    /**
     * Returns all tags in the system.
     */
    @Cacheable(cacheNames = [CacheNames.TAGS], key = "'all'")
    @Transactional(readOnly = true)
    fun getAllTags(): List<TagResponseDto> {
        return tagRepository.findAll().map { it.toResponseDto() }
    }

    @Cacheable(cacheNames = [CacheNames.TAGS], key = "'page_' + #page + '_size_' + #size")
    @Transactional(readOnly = true)
    fun getAllTags(page: Int, size: Int): Page<TagResponseDto> {
        val pageable = PageRequest.of(page, size)
        return tagRepository.findAll(pageable).map { it.toResponseDto() }
    }

    /**
     * Returns a single tag by its ID.
     * Throws [ResourceNotFoundException] if no tag with the given [id] exists.
     */
    @Cacheable(cacheNames = [CacheNames.TAGS_SINGLE], key = "#id")
    @Transactional(readOnly = true)
    fun getTagById(id: Long): TagResponseDto {
        val tag = tagRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Tag not found with id: $id") }
        return tag.toResponseDto()
    }

    /**
     * Creates a new tag.
     * Throws [ResourceAlreadyExistException] if a tag with the same name
     * already exists in the database (name column has a unique constraint).
     */
    @CacheEvict(cacheNames = [CacheNames.TAGS], allEntries = true)
    @Transactional
    fun createTag(request: TagRequestDto): TagResponseDto {
        val trimmedName = request.name.trim()

        tagRepository.findByName(trimmedName).ifPresent {
            throw ResourceAlreadyExistException("Tag with name '$trimmedName' already exists")
        }

        val tag = Tag().apply {
            name = trimmedName
        }

        return tagRepository.save(tag).toResponseDto()
    }

    /**
     * Deletes a tag by ID.
     * Throws [ResourceNotFoundException] if the tag does not exist.
     */
    @Caching(evict = [
        CacheEvict(cacheNames = [CacheNames.TAGS], allEntries = true),
        CacheEvict(cacheNames = [CacheNames.TAGS_SINGLE], key = "#id")
    ])
    @Transactional
    fun deleteTag(id: Long) {
        if (!tagRepository.existsById(id)) {
            throw ResourceNotFoundException("Tag not found with id: $id")
        }
        tagRepository.deleteById(id)
    }
}

