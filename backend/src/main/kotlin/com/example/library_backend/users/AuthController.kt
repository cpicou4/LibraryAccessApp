package com.example.library_backend.users

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val service: UserService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody dto: UserCreateDto): UserGetDto =
        service.register(dto)

    @GetMapping("/whoami")
    @PreAuthorize("isAuthenticated()")
    fun whoami(auth: org.springframework.security.core.Authentication) =
        mapOf("username" to auth.name, "roles" to auth.authorities.map { it.authority })

    @PostMapping("/login")
    fun login(@RequestBody dto: LoginRequestDto): LoginResponseDto =
        service.login(dto)
}