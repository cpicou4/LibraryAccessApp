package com.example.library_backend.reservations

import jakarta.validation.constraints.Min
import java.time.LocalDate

data class ReservationGetDto(
    val id: Int,
    val bookId: Int,
    val userId: Int,
    val queuePosition: Int?,
    val reservationDate: LocalDate,
    val expiryDate: LocalDate,
    val status: ReservationStatus,
)

data class ReservationCreateDto(

    val bookId: Int,
    val userId: Int,

    @field:Min(1)
    val holdLimitDays: Int = 7 //Instead of expiry date since that can be calculated
)
