package com.example.library_backend.borrowings

import com.example.library_backend.books.*
import com.example.library_backend.reservations.ReservationService
import com.example.library_backend.users.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class FinePolicy(
    val dailyRate: BigDecimal = BigDecimal("0.25"), // $0.25/day
    val graceDays: Long = 0,
    val maxCap: BigDecimal? = null
)

@Service
class BorrowingRecordService(
    private val repo: BorrowingRecordRepository,
    private val bookRepo: BookRepository,
    private val userRepo: UserRepository,
    private val reservationService: ReservationService
) {
    private val policy = FinePolicy()

    fun getAll(): List<BorrowingRecordGetDto> =
        repo.findAll().map { it.toGetDto(policy) }

    fun getById(id: Int): BorrowingRecordGetDto =
        repo.findByIdOrNull(id)?.toGetDto(policy)
            ?: throw NoSuchElementException("Record $id not found")

    @Transactional
    fun checkOut(dto: BorrowingRecordCreateDto): BorrowingRecordGetDto {

        val book = bookRepo.findByIdOrNull(dto.bookId)
            ?: throw NoSuchElementException("Book ${dto.bookId} not found")
        val user = userRepo.findByIdOrNull(dto.userId)
            ?: throw NoSuchElementException("User ${dto.userId} not found")

        if (book.availableCopies <= 0) {
            throw IllegalStateException("There are no available copies of ${book.title}")
        }
        //Check so someone doesn't check out multiple of the same book.
        if (repo.existsByUser_IdAndBook_IdAndStatus(user.id, book.id, "ACTIVE")) {
            throw IllegalArgumentException("User ${user.id} already has an active borrowing for book ${book.id}")
        }

        val record = BorrowingRecord(
            book = book,
            user = user,
            borrowDate = LocalDate.now(),
            dueDate = LocalDate.now().plusDays(dto.loanDays.toLong()),
            returnDate = null,
            status = "ACTIVE",
        )

        book.availableCopies = book.availableCopies - 1

        reservationService.completeIfReserved(dto.userId,dto.bookId)

        val saved = repo.save(record)
        return saved.toGetDto(policy)
    }

    @Transactional
    fun returnBook(id: Int, dto: BorrowingRecordReturnDto): BorrowingRecordGetDto {
        val record = repo.findByIdOrNull(id)
            ?: throw NoSuchElementException("Borrowing record $id not found")

        if (record.returnDate != null) {
            //Returned: The book has already returned
            return record.toGetDto(policy)
        }

        record.returnDate = dto.returnDate ?: LocalDate.now()
        record.status = "RETURNED"

        val book = record.book
        book.availableCopies = book.availableCopies + 1

        return record.toGetDto(policy)
    }

    fun getActiveByUser(userId: Int): List<BorrowingRecordGetDto> =
        repo.findByUser_IdAndStatus(userId, "ACTIVE").map { it.toGetDto(policy) }

    fun getActiveByBook(bookId: Int): List<BorrowingRecordGetDto> =
        repo.findByBook_IdAndStatus(bookId, "ACTIVE").map { it.toGetDto(policy) }
}

private fun BorrowingRecord.toGetDto(policy: FinePolicy): BorrowingRecordGetDto {
    val (daysLate, fine, derivedStatus) = computeLateAndFine(policy)
    return BorrowingRecordGetDto(
        id = id,
        bookId = book.id,
        userId = user.id,
        borrowDate = borrowDate,
        dueDate = dueDate,
        returnDate = returnDate,
        status = derivedStatus,
        daysLate = daysLate,
        currentFine = fine
    )
}

private fun BorrowingRecord.computeLateAndFine(policy: FinePolicy): Triple<Long, BigDecimal, String> {
    val end = returnDate ?: LocalDate.now()
    val base = dueDate.plusDays(policy.graceDays)
    val daysLate = ChronoUnit.DAYS.between(base, end).coerceAtLeast(0)
    val raw = policy.dailyRate.multiply(BigDecimal(daysLate))
    val fine = policy.maxCap?.let { raw.min(it) } ?: raw

    val derivedStatus = when {
        returnDate != null -> "RETURNED"
        end.isAfter(dueDate) -> "OVERDUE"
        else -> "ACTIVE"
    }
    return Triple(daysLate, fine, derivedStatus)
}