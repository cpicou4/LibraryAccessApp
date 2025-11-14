package com.example.library_backend.reservations

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ReservationRepository : JpaRepository<Reservation, Int> {

    @Query("""
        select r from Reservation r
        where r.user.id = :userId and r.book.id = :bookId and r.closedFlag = false
    """)
    fun findOpenByUserAndBook(@Param("userId") userId: Int, @Param("bookId") bookId: Int): List<Reservation>

    fun findByBookIdAndClosedFlagFalseOrderByQueuePositionAscCreatedAtAsc(bookId: Int): List<Reservation>

    @Query("""
        select r from Reservation r
        where r.status in ('PENDING','ACTIVE')
          and r.reservationDate <= :today and r.expiryDate >= :today
    """)
    fun findAllActiveToday(@Param("today") today: LocalDate): List<Reservation>

    @Query("""
        select r from Reservation r
        where r.status in ('PENDING','ACTIVE')
          and r.expiryDate < :today
    """)
    fun findAllExpiredBefore(@Param("today") today: LocalDate): List<Reservation>

    @Query("""
        select r from Reservation r
        where r.user.id = :userId
          and r.status in ('PENDING','ACTIVE')
          and r.reservationDate <= :on and r.expiryDate >= :on
        order by r.reservationDate asc, r.createdAt asc
    """)
    fun findActiveByUserOnDate(@Param("userId") userId: Int, @Param("on") on: LocalDate): List<Reservation>

    @Query("""
        select r from Reservation r
        where r.book.id = :bookId
          and r.status in ('PENDING','ACTIVE')
          and r.reservationDate <= :on and r.expiryDate >= :on
        order by r.reservationDate asc, r.createdAt asc
    """)

    fun findActiveByBookOnDate(@Param("bookId") bookId: Int, @Param("on") on: LocalDate): List<Reservation>
    @Modifying
    @Query("""
    update Reservation r
       set r.status = 'EXPIRED',
           r.closedFlag = true,
           r.updatedAt = :now
     where r.id in :ids
""")
    fun bulkExpire(
        @Param("ids") ids: List<Int>,
        @Param("now") now: java.time.Instant
    ): Int
}