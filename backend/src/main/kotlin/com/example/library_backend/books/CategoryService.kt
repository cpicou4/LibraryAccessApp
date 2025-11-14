package com.example.library_backend.books

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepo: CategoryRepository
) {

    fun getAll(): List<CategoryGetDto> =
        categoryRepo.findAll()
            .sortedBy { it.type.lowercase() }
            .map { it.toGetDto() }

    fun getById(id: Int): CategoryGetDto =
        categoryRepo.findByIdOrNull(id)?.toGetDto()
            ?: throw NoSuchElementException("Category $id not found")

    @Transactional
    fun create(dto: CategoryCreateDto): CategoryGetDto {
        val type = dto.type.trim()
        if (type.isEmpty()) {
            throw IllegalArgumentException("Category type cannot be blank")
        }

        val entity = Category(
            type = type
        )

        val saved = categoryRepo.save(entity)
        return saved.toGetDto()
    }

    @Transactional
    fun delete(id: Int) {
        val category = categoryRepo.findByIdOrNull(id)
            ?: throw NoSuchElementException("Category $id not found")

        if (category.books.isNotEmpty()) {
            throw IllegalStateException(
                "Cannot delete category still linked to ${category.books.size} book(s)"
            )
        }

        categoryRepo.delete(category)
    }
}