package com.example.library_backend.books

import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Int> {
    fun findByTypeIgnoreCase(type: String): Category?
}