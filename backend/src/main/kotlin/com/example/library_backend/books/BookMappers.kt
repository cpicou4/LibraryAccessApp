package com.example.library_backend.books

fun Book.toGetDto() = BookGetDto(
    id = id,
    isbn = isbn,
    title = title,
    author = author,
    publisher = publisher,
    publicationYear = publicationYear,
    category = category,
    totalCopies = totalCopies,
    availableCopies = availableCopies,
    description = description,
    coverImageUrl = coverImageUrl

)

fun Book.updateFrom(dto: BookUpdateDto) {
    isbn = dto.isbn
    title = dto.title
    author = dto.author
    publisher = dto.publisher
    publicationYear = dto.publicationYear
    category = dto.category
    totalCopies = dto.totalCopies
    availableCopies = dto.availableCopies
    description = dto.description
    coverImageUrl = dto.coverImageUrl
}