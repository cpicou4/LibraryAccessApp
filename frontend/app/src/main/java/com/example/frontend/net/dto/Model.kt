package com.example.frontend.net.dto

import java.math.BigDecimal

//Users/Auth
data class UserGetDto(
    val id: Int,
    val username: String,
    val email: String,
    val fullName: String?,
    val phone: String?,
    val role: String?,
)
data class UserCreateDto(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String?,
    val phone: String?,
    val role: String? = null,
)
data class UserUpdateDto(
    val fullName: String?,
    val phone: String?,
    val role: String? = null,
)
data class LoginRequestDto(
    val usernameOrEmail: String,
    val password: String
)
data class LoginResponseDto(
    val userId: Int,
    val username: String,
    val role: String,
    val message: String = "Login successful",
    val accessToken: String? = null
)

//Books
data class BookGetDto(
    val id: Int,
    val isbn: String?,
    val title: String,
    val author: String,
    val publisher: String?,
    val publicationYear: Int?,
    val category: String?,
    val totalCopies: Int,
    val availableCopies: Int,
    val description: String?,
    val coverImageUrl: String?
)
data class BookCreateUpdateDto(
    val isbn: String?,
    val author: String,
    val publisher: String?,
    val publicationYear: Int?,
    val category: String?,
    val totalCopies: Int = 0,
    val availableCopies: Int = 0,
    val description: String?,
    val coverImageUrl: String?
)

//Borrowing Records
data class BorrowingRecordGetDto(
    val id: Int,
    val bookId: Int,
    val userId: Int,
    val borrowDate: String,
    val dueDate: String,
    val returnDate: String?,
    val status: String,
    val daysLate: Long,
    val currentFine: BigDecimal
)
data class BorrowingRecordCreateDto(
    val bookId: Int,
    val userId: Int,
    //Instead of due date since that can be calculated
    val loanDays: Int = 14
)
data class BorrowingRecordReturnDto(
    val returnDate: String? = null
)