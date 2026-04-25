package com.ecommerce.mvp.modules.tag

import com.ecommerce.mvp.modules.tag.dto.TagRequestDto
import com.ecommerce.mvp.modules.tag.dto.TagResponseDto
import com.ecommerce.mvp.modules.tag.service.TagService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/tags")
class TagController(
    private val tagService: TagService
) {

    /**
     * GET /api/tags
     * Returns all available tags.
     */
    @GetMapping
    fun getAllTags(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<TagResponseDto> {
        return tagService.getAllTags(page, size)
    }

    /**
     * GET /api/tags/{id}
     * Returns a single tag by ID. Responds 404 if not found.
     */
    @GetMapping("/{id}")
    fun getTagById(@PathVariable id: Long): TagResponseDto {
        return tagService.getTagById(id)
    }

    /**
     * POST /api/tags
     * Creates a new tag. Responds 409 if a tag with the same name already exists.
     *
     * Request body: { "name": "electronics" }
     */
    @PostMapping
    fun createTag(@Valid @RequestBody request: TagRequestDto): ResponseEntity<TagResponseDto> {
        val created = tagService.createTag(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    /**
     * DELETE /api/tags/{id}
     * Deletes a tag by ID. Responds 404 if not found.
     */
    @DeleteMapping("/{id}")
    fun deleteTag(@PathVariable id: Long): ResponseEntity<Unit> {
        tagService.deleteTag(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}

