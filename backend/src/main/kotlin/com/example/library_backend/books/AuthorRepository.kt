package com.example.library_backend.books

import org.springframework.data.jpa.repository.JpaRepository

interface AuthorRepository : JpaRepository<Author, Int> {
    fun findByNameIgnoreCase(name: String): Author?
}