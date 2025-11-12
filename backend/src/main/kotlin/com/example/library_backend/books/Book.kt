package com.example.library_backend.books

import jakarta.persistence.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(
    name = "books",
    indexes = [
        Index(name = "idx_books_isbn", columnList = "isbn")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_books_isbn", columnNames = ["isbn"])
    ])
data class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    val id: Int = 0,

    @Column(name = "isbn", unique = true, length = 13)
    var isbn: String? = null,  // nullable + unique

    @field:NotBlank
    @field:Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    var title: String,

    @field:NotBlank
    @field:Size(max = 255)
    @Column(name = "author", nullable = false, length = 255)
    var author: String,

    @field:Size(max = 100)
    @Column(name = "publisher", length = 100)
    var publisher: String? = null,

    @Column(name ="publication_year")
    var publicationYear: Int? = null,

    @field:Size(max = 50)
    @Column(name = "category", length = 50)
    var category: String? = null,

    @field:Min(0)
    @Column(name = "total_copies", nullable = false)
    var totalCopies: Int = 0, // how many copies are at the library

    @field:Min(0)
    @Column(name = "available_copies", nullable = false)
    var availableCopies: Int = 0, // how many copies are available

    @field:Size(max = 1000)
    @Column(name = "description", columnDefinition = "text")
    var description: String? = null,

    @field:Size(max = 255)
    @Column(name = "cover_image_url", length = 255)
    var coverImageUrl: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant? = null

) {
    @PrePersist
    fun applyDefaults() {
        if (availableCopies == 0 && totalCopies > 0) {
            availableCopies = totalCopies
        }
    }
}