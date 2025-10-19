package com.example.library_backend.books

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(private val repo: BookRepository) {

    fun getAll(): List<BookGetDto> =
        repo.findAll().map { it.toGetDto() }

    fun getById(id: Int): BookGetDto =
        repo.findByIdOrNull(id)?.toGetDto()
            ?: throw NoSuchElementException("Book $id not found")

    @Transactional
    fun create(dto: BookCreateDto): BookGetDto {
        // Guard becuase isbn has to be unique
        if (dto.isbn != null && repo.existsByIsbn(dto.isbn)) {
            throw IllegalArgumentException("ISBN '${dto.isbn}' already exists")
        }

        val saved = repo.save(
            Book(
                isbn = dto.isbn,
                title = dto.title,
                author = dto.author,
                publisher = dto.publisher,
                publicationYear = dto.publicationYear,
                category = dto.category,
                totalCopies = dto.totalCopies,
                availableCopies = dto.availableCopies,
                description = dto.description,
                coverImageUrl = dto.coverImageUrl
            )
        )
        return saved.toGetDto()
    }

    @Transactional
    fun update(id: Int, dto: BookUpdateDto): BookGetDto {
        val entity = repo.findByIdOrNull(id)
            ?: throw NoSuchElementException("Book $id not found")

        // Guard for unique isbn on update
        if (dto.isbn != null) {
            val conflict = repo.findByIsbn(dto.isbn)
            if (conflict != null && conflict.id != id) {
                throw IllegalArgumentException("ISBN '${dto.isbn}' already exists")
            }
        }

        entity.updateFrom(dto)
        return entity.toGetDto()
    }

    fun delete(id: Int) {
        if (!repo.existsById(id)) throw NoSuchElementException("Book $id not found")
        repo.deleteById(id)
    }
}