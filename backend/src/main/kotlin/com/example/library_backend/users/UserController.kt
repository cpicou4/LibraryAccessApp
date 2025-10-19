package com.example.library_backend.users

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val service: UserService) {

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getAll(): List<UserGetDto> = service.getAll()

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    fun getById(@PathVariable id: Int): UserGetDto = service.getById(id)

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    fun update(
        @PathVariable id: Int,
        @Valid @RequestBody dto: UserUpdateDto
    ): UserGetDto = service.update(id, dto)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int) = service.delete(id)
}