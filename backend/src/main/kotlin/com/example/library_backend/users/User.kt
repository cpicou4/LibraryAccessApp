package com.example.library_backend.users

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_users_username", columnNames = ["username"]),
        UniqueConstraint(name = "uk_users_email", columnNames = ["email"])
    ])
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val id: Int = 0,

    @field:NotBlank
    @field:Size(max = 50)
    @Column(name = "username", nullable = false, unique = true, length = 50)
    var username: String,

    @field:NotBlank
    @field:Size(max = 100)
    @Column(name = "email", nullable = false, unique = true, length = 100)
    var email: String,

    @field:NotBlank
    @field:Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String,

    @field:Size(max = 100)
    @Column(name = "full_name", length = 100)
    var fullName: String? = null,

    @field:Size(max = 20)
    @Column(name = "phone", length = 20)
    var phone: String? = null,

    @field:Size(max = 20)
    @Column(name = "account_status", nullable = false, length = 20)
    var role: String = "USER",

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    val updatedAt: Instant? = null

)