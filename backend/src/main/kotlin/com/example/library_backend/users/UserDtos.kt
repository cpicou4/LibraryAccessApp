package com.example.library_backend.users

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserGetDto(
    val id: Int,
    val username: String,
    val email: String,
    val fullName: String?,
    val phone: String?,
    val role: String?,
)

data class UserCreateDto(

    @field:NotBlank
    @field:Size(max = 50)
    val username: String,

    @field:NotBlank
    @field:Size(max = 100)
    val email: String,

    @field:NotBlank
    @field:Size(min = 6, max = 255)
    val password: String,

    @field:Size(max = 100)
    val fullName: String?,

    @field:Size(max = 20)
    val phone: String?,

    @field:Size(max = 20)
    val role: String? = null,
)

data class UserUpdateDto(

    // More complicated to do right now
//    @field:NotBlank
//    @field:Size(max = 50)
//    val username: String,
//
//    @field:NotBlank
//    @field:Size(max = 100)
//    val email: String,

    @field:Size(max = 100)
    val fullName: String?,

    @field:Size(max = 20)
    val phone: String?,

    @field:Size(max = 20)
    val role: String? = null,
)

//Login
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