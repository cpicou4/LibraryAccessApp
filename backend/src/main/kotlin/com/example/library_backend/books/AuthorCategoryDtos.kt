package com.example.library_backend.books

import jakarta.validation.constraints.NotBlank

/* --------- Author DTOs / Mapper --------- */

data class AuthorGetDto(
    val id: Int,
    val name: String
)

data class AuthorCreateDto(
    @field:NotBlank
    val name: String
)

fun Author.toGetDto(): AuthorGetDto =
    AuthorGetDto(
        id = this.id,
        name = this.name
    )

/* --------- Category DTOs / Mapper --------- */

data class CategoryGetDto(
    val id: Int,
    val type: String
)

data class CategoryCreateDto(
    @field:NotBlank
    val type: String
)
fun Category.toGetDto(): CategoryGetDto =
    CategoryGetDto(
        id = this.id,
        type = this.type
    )