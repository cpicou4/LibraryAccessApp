package com.example.library_backend.books

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookRepository : JpaRepository<Book, Int> {
    fun findByIsbn(isbn: String): Book?
    fun existsByIsbn(isbn: String): Boolean
}