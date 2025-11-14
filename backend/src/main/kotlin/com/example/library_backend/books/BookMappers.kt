package com.example.library_backend.books

fun Book.toGetDto() = BookGetDto(
    id = id,
    isbn = isbn,
    title = title,
    authors = authors.map { it.name }.sorted(),
    publisher = publisher,
    publicationYear = publicationYear,
    categories = categories.map { it.type }.sorted(),
    totalCopies = totalCopies,
    availableCopies = availableCopies,
    description = description,
    coverImageUrl = coverImageUrl

)

fun Book.updateFrom(
    dto: BookUpdateDto,
    resolvedAuthors: Set<Author>? = null,
    resolvedCategories: Set<Category>? = null
) {
    dto.isbn?.let { this.isbn = it }
    dto.title?.let { this.title = it }

    // Replace authors only if caller provided a new set
    resolvedAuthors?.let { newAuthors ->
        this.authors.clear()
        this.authors.addAll(newAuthors)
    }
    dto.publisher?.let { this.publisher = it }
    dto.publicationYear?.let { this.publicationYear = it }

    // Replace categories only if caller provided a new set
    resolvedCategories?.let { newCats ->
        this.categories.clear()
        this.categories.addAll(newCats)
    }
    dto.totalCopies?.let { this.totalCopies = it }
    dto.availableCopies?.let { this.availableCopies = it }
    dto.description?.let { this.description = it }
    dto.coverImageUrl?.let { this.coverImageUrl = it }

}