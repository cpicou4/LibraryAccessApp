package com.example.library_backend.borrowings

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BorrowingRecordRepository : JpaRepository<BorrowingRecord, Int> {
    fun findByUser_IdAndStatus(userId: Int, status: String): List<BorrowingRecord>
    fun findByBook_IdAndStatus(bookId: Int, status: String): List<BorrowingRecord>
    fun existsByUser_IdAndBook_IdAndStatus(userId: Int, bookId: Int, status: String): Boolean
}