package com.example.library_backend.books

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BookRepository : JpaRepository<Book, Int> {
    fun findByIsbn(isbn: String): Book?
    fun existsByIsbn(isbn: String): Boolean

    @Query("""
    select (b.totalCopies - coalesce((
        select count(br) from BorrowingRecord br
        where br.book.id = :bookId and br.returnDate is null
    ),0))
    from Book b where b.id = :bookId
""")
    fun computedAvailable(@Param("bookId") bookId: Int): Int
}