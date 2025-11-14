package com.example.library_backend.borrowings

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/borrowings")
class BorrowingRecordController(private val service: BorrowingRecordService) {

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getAll(): List<BorrowingRecordGetDto> = service.getAll()

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getById(@PathVariable id: Int): BorrowingRecordGetDto = service.getById(id)

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun checkOut(@Valid @RequestBody dto: BorrowingRecordCreateDto): BorrowingRecordGetDto =
        service.checkOut(dto)

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    fun returnBook(
        @PathVariable id: Int,
        @Valid @RequestBody dto: BorrowingRecordReturnDto
    ): BorrowingRecordGetDto =
        service.returnBook(id, dto)

    @GetMapping("/user/{userId}/active")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    fun getActiveByUser(@PathVariable userId: Int): List<BorrowingRecordGetDto> =
        service.getActiveByUser(userId)

    @GetMapping("/book/{bookId}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getActiveByBook(@PathVariable bookId: Int): List<BorrowingRecordGetDto> =
        service.getActiveByBook(bookId)
}