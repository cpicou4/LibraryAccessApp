package com.example.library_backend.reservations

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservations")
class ReservationController(
    private val service: ReservationService
) {
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getAll(): List<ReservationGetDto> = service.getAll()

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getById(@PathVariable id: Int): ReservationGetDto = service.getById(id)

    @GetMapping("/user/{userId}/active")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    fun getActiveByUser(@PathVariable userId: Int): List<ReservationGetDto> =
        service.getActiveByUser(userId)

    @GetMapping("/book/{bookId}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getActiveByBook(@PathVariable bookId: Int): List<ReservationGetDto> =
        service.getActiveByBook(bookId)

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: ReservationCreateDto): ReservationGetDto =
        service.create(dto)

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    fun cancel(
        @PathVariable id: Int,
        @RequestParam(required = false) userId: Int?
    ): ReservationGetDto = service.cancel(id, userId)
}