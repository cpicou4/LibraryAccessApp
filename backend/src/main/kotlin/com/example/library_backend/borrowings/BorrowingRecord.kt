package com.example.library_backend.borrowings

import com.example.library_backend.books.Book
import com.example.library_backend.users.User
import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.time.LocalDate


@Entity
@Table(name = "borrowing_records")
data class BorrowingRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    val id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    var book: Book,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "borrow_date", nullable = false)
    var borrowDate: LocalDate = LocalDate.now(),

    @Column(name = "due_date", nullable = false)
    var dueDate: LocalDate = LocalDate.now().plusDays(14),

    @Column(name = "return_date", nullable = true)
    var returnDate: LocalDate? = null,

    @field:Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    var status: String = "ACTIVE",

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    val updatedAt: Instant? = null
)