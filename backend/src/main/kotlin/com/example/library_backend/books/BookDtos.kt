package com.example.library_backend.books

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class BookGetDto(
    val id: Int,
    val isbn: String?,
    val title: String,
    val authors: List<String>,
    val publisher: String?,
    val publicationYear: Int?,
    val categories: List<String>,
    val totalCopies: Int,
    val availableCopies: Int,
    val description: String?,
    val coverImageUrl: String?
)

data class BookCreateDto(

    @field:Size(max = 13)
    val isbn: String?,

    @field:NotBlank
    @field:Size(max = 255)
    val title: String,

    @field:NotBlank
    val authors: List<String>,

    @field:Size(max = 100)
    val publisher: String?,

    val publicationYear: Int?,

    val categories: List<String>,

    @field:Min(0)
    val totalCopies: Int = 0,

    @field:Min(0)
    val availableCopies: Int = 0,

    val description: String?,

    @field:Size(max = 255)
    val coverImageUrl: String?
)

data class BookUpdateDto(

    @field:Size(max = 13)
    val isbn: String?,

    @field:NotBlank
    @field:Size(max = 255)
    val title: String,

    @field:NotBlank
    val authors: List<String>,

    @field:Size(max = 100)
    val publisher: String?,

    val publicationYear: Int?,

    val categories: List<String>,

    @field:Min(0)
    val totalCopies: Int,

    @field:Min(0)
    val availableCopies: Int,

    val description: String?,

    @field:Size(max = 255)
    val coverImageUrl: String?
)