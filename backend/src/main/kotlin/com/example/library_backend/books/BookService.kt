package com.example.library_backend.books

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val repo: BookRepository,
    private val authorRepo: AuthorRepository,
    private val categoryRepo: CategoryRepository
) {

    private fun resolveAuthors(names: List<String>): MutableSet<Author> =
        names.mapNotNull { raw ->
            val name = raw.trim()
            if (name.isBlank()) null
            else authorRepo.findByNameIgnoreCase(name)
                ?: authorRepo.save(Author(name = name))
        }.toMutableSet()

    private fun resolveCategories(types: List<String>): MutableSet<Category> =
        types.mapNotNull { raw ->
            val type = raw.trim()
            if (type.isBlank()) null
            else categoryRepo.findByTypeIgnoreCase(type)
                ?: categoryRepo.save(Category(type = type))
        }.toMutableSet()

    fun getAll(): List<BookGetDto> =
        repo.findAll().map { it.toGetDto() }

    fun getById(id: Int): BookGetDto =
        repo.findByIdOrNull(id)?.toGetDto()
            ?: throw NoSuchElementException("Book $id not found")

    @Transactional
    fun create(dto: BookCreateDto): BookGetDto {
        val authors = resolveAuthors(dto.authors)
        val categories = resolveCategories(dto.categories)
        // Guard becuase isbn has to be unique
        if (dto.isbn != null && repo.existsByIsbn(dto.isbn)) {
            throw IllegalArgumentException("ISBN '${dto.isbn}' already exists")
        }

        val saved = repo.save(
            Book(
                isbn = dto.isbn,
                title = dto.title,
                authors = authors,
                publisher = dto.publisher,
                publicationYear = dto.publicationYear,
                categories = categories,
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

        dto.isbn?.let { newIsbn ->
            val conflict = repo.findByIsbn(newIsbn)
            if (conflict != null && conflict.id != id) {
                throw IllegalArgumentException("ISBN '$newIsbn' already exists")
            }
        }

        // Only resolve if client actually sent lists
        val resolvedAuthors = dto.authors?.let { resolveAuthors(it) }
        val resolvedCats = dto.categories?.let { resolveCategories(it) }

        entity.updateFrom(dto, resolvedAuthors, resolvedCats)

        return entity.toGetDto()
    }

    fun delete(id: Int) {
        if (!repo.existsById(id)) throw NoSuchElementException("Book $id not found")
        repo.deleteById(id)
    }
}