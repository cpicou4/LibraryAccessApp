package com.example.library_backend.books

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/books")
class BookController(private val service: BookService) {

    @GetMapping
    fun getAll(): List<BookGetDto> = service.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Int): BookGetDto = service.getById(id)

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: BookCreateDto): BookGetDto =
        service.create(dto)

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun update(
        @PathVariable id: Int,
        @Valid @RequestBody dto: BookUpdateDto
    ): BookGetDto = service.update(id, dto)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int) = service.delete(id)
}