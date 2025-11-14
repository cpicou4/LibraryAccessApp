package com.example.library_backend.reservations

import com.example.library_backend.books.*
import com.example.library_backend.borrowings.*
import com.example.library_backend.users.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

@Service
class ReservationService(
    private val resRepo: ReservationRepository,
    private val bookRepo: BookRepository,
    private val userRepo: UserRepository,
    private val borrowRepo: BorrowingRecordRepository
) {
    private val bufferDays = 2L
    private val windowDays = 3L

    private fun today() = LocalDate.now()

    private fun map(r: Reservation) = ReservationGetDto(
        r.id!!, r.user.id!!, r.book.id!!, r.queuePosition, r.reservationDate, r.expiryDate, r.status
    )

    @Transactional(readOnly = true)
    fun getAll(): List<ReservationGetDto> =
        resRepo.findAll().map { map(it) }

    @Transactional(readOnly = true)
    fun getById(id: Int): ReservationGetDto =
        resRepo.findByIdOrNull(id)?.let { map(it) } ?: throw NoSuchElementException("Reservation $id not found")

    @Transactional(readOnly = true)
    fun getActiveByUser(userId: Int): List<ReservationGetDto> {
        val t = today()
        return resRepo.findActiveByUserOnDate(userId, t).map { map(it) }
    }

    @Transactional(readOnly = true)
    fun getActiveByBook(bookId: Int): List<ReservationGetDto> {
        val t = today()
        return resRepo.findActiveByBookOnDate(bookId, t).map { map(it) }
    }
    @Transactional
    fun create(dto: ReservationCreateDto): ReservationGetDto {
        val user = userRepo.findById(dto.userId).orElseThrow()
        val book = bookRepo.findById(dto.bookId).orElseThrow()

        // Prevent duplicate open reservations
        if (resRepo.findOpenByUserAndBook(dto.userId, dto.bookId).isNotEmpty())
            throw IllegalStateException("You already have an open reservation for this book")

        val t = today()

        // Net availability today (subtract other ACTIVE reservations for the same book)
        val availableNow = try { bookRepo.computedAvailable(book.id!!) } catch (_: Exception) { 0 }
        val activeToday = resRepo.findAllActiveToday(t).count { it.book.id == book.id }
        val netAvailable = availableNow - activeToday

        val (startDate, status) =
            if (netAvailable > 0) t to ReservationStatus.ACTIVE
            else {
                val minDue = borrowRepo.findEarliestDueDate(book.id!!)
                val start = (minDue ?: t).plusDays(bufferDays)
                start to ReservationStatus.PENDING
            }

        val expiry = startDate.plusDays(windowDays - 1)

        val queue = resRepo.findByBookIdAndClosedFlagFalseOrderByQueuePositionAscCreatedAtAsc(book.id!!)
        val nextPos = (queue.maxOfOrNull { it.queuePosition ?: 0 } ?: 0) + 1

        val saved = resRepo.save(
            Reservation(
                user = user,
                book = book,
                status = status,
                reservationDate = startDate,
                expiryDate = expiry,
                queuePosition = nextPos
            )
        )
        return map(saved)
    }

    @Transactional
    fun cancel(reservationId: Int, byUserId: Int? = null): ReservationGetDto {
        val r = resRepo.findById(reservationId).orElseThrow()
        if (byUserId != null && r.user.id != byUserId) error("Cannot cancel another user's reservation")

        if (r.status in setOf(ReservationStatus.CANCELLED, ReservationStatus.COMPLETED, ReservationStatus.EXPIRED))
            return map(r)

        r.status = ReservationStatus.CANCELLED
        r.closedFlag = true
        return map(r)
    }

    @Transactional
    fun expireOverdueWindows(): Int {
        val t = today()
        val toExpire = resRepo.findAllExpiredBefore(t)
        if (toExpire.isEmpty()) return 0
        resRepo.bulkExpire(toExpire.map { it.id!! }, java.time.Instant.now())
        return toExpire.size
    }

    @Transactional
    fun completeIfReserved(userId: Int, bookId: Int) {
        val t = today()
        val candidates = resRepo.findOpenByUserAndBook(userId, bookId)
            .filter { it.status in setOf(ReservationStatus.ACTIVE, ReservationStatus.PENDING) }

        val hit = candidates.firstOrNull { t in it.reservationDate..it.expiryDate } ?: return
        hit.status = ReservationStatus.COMPLETED
        hit.closedFlag = true
    }
}
