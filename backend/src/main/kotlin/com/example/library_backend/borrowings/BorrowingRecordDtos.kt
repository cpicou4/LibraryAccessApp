package com.example.library_backend.borrowings

import jakarta.validation.constraints.Min
import java.math.BigDecimal
import java.time.LocalDate

data class BorrowingRecordGetDto(
    val id: Int,
    val bookId: Int,
    val userId: Int,
    val borrowDate: LocalDate,
    val dueDate: LocalDate,
    val returnDate: LocalDate?,
    val status: String,
    val daysLate: Long,              //Computed
    val currentFine: BigDecimal      //Computed
)

data class BorrowingRecordCreateDto(

    val bookId: Int,
    val userId: Int,

    @field:Min(1)
    val loanDays: Int = 14 //Instead of due date since that can be calculated
)

data class BorrowingRecordReturnDto(
    val returnDate: LocalDate? = null
)