package com.example.library_backend.borrowings

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface BorrowingRecordRepository : JpaRepository<BorrowingRecord, Int> {
    fun findByUser_IdAndStatus(userId: Int, status: String): List<BorrowingRecord>
    fun findByBook_IdAndStatus(bookId: Int, status: String): List<BorrowingRecord>
    fun existsByUser_IdAndBook_IdAndStatus(userId: Int, bookId: Int, status: String): Boolean
    @Query("""
    select min(b.dueDate) from BorrowingRecord b
    where b.book.id = :bookId and b.returnDate is null
""")
    fun findEarliestDueDate(@Param("bookId") bookId: Int): LocalDate?

    @Query("""
    select count(b) from BorrowingRecord b
    where b.book.id = :bookId and b.returnDate is null
""")
    fun countCheckedOut(@Param("bookId") bookId: Int): Long
}