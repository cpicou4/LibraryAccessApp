package com.example.library_backend.books

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorService(
    private val authorRepo: AuthorRepository
) {

    fun getAll(): List<AuthorGetDto> =
        authorRepo.findAll()
            .sortedBy { it.name.lowercase() }
            .map { it.toGetDto() }

    fun getById(id: Int): AuthorGetDto =
        authorRepo.findByIdOrNull(id)?.toGetDto()
            ?: throw NoSuchElementException("Author $id not found")

    @Transactional
    fun create(dto: AuthorCreateDto): AuthorGetDto {
        val name = dto.name.trim()
        if (name.isEmpty()) {
            throw IllegalArgumentException("Author name cannot be blank")
        }

        val entity = Author(
            name = name
        )

        val saved = authorRepo.save(entity)
        return saved.toGetDto()
    }

    @Transactional
    fun delete(id: Int) {
        val author = authorRepo.findByIdOrNull(id)
            ?: throw NoSuchElementException("Author $id not found")

        if (author.books.isNotEmpty()) {
            throw IllegalStateException(
                "Cannot delete author still linked to ${author.books.size} book(s)"
            )
        }

        authorRepo.delete(author)
    }
}