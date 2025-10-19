package com.example.library_backend.users

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val repo: UserRepository,
    private val encoder: PasswordEncoder,
    private val jwtService: com.example.library_backend.security.JwtService
) {

    fun getAll(): List<UserGetDto> =
        repo.findAll().map { it.toGetDto() }

    fun getById(id: Int): UserGetDto =
        repo.findByIdOrNull(id)?.toGetDto()
            ?: throw NoSuchElementException("User $id not found")

    @Transactional
    fun register(dto: UserCreateDto): UserGetDto {
        // Guard becuase username and email has to be unique
        if (dto.username != null && repo.existsByUsername(dto.username)) {
            throw IllegalArgumentException("Username '${dto.username}' already exists")
        } else if (dto.email != null && repo.existsByEmail(dto.email)) {
            throw IllegalArgumentException("An account with the '${dto.email}' already exists")
        }

        val saved = repo.save(
            User(
                username = dto.username,
                email = dto.email,
                passwordHash = encoder.encode(dto.password),
                fullName = dto.fullName,
                phone = dto.phone,
                role = dto.role ?: "USER"
            )
        )
        return saved.toGetDto()
    }

    @Transactional
    fun update(id: Int, dto: UserUpdateDto): UserGetDto {
        val entity = repo.findByIdOrNull(id)
            ?: throw NoSuchElementException("User $id not found")

//        // Guard for unique username and email on update
//        if (dto.username != null) {
//            val conflict = repo.findByUsername(dto.username)
//            if (conflict != null && conflict.id != id) {
//                throw IllegalArgumentException("Username '${dto.username}' already exists")
//            }
//        } else if (dto.email != null) {
//            val conflict = repo.findByEmail(dto.email)
//            if (conflict != null && conflict.id != id) {
//                throw IllegalArgumentException("An account with the '${dto.email}' already exists")
//            }
//        }

        entity.updateFrom(dto)
        return entity.toGetDto()
    }

    fun delete(id: Int) {
        if (!repo.existsById(id)) throw NoSuchElementException("User $id not found")
        repo.deleteById(id)
    }
    fun login(dto: LoginRequestDto): LoginResponseDto {
        val user = repo.findByUsername(dto.usernameOrEmail)
            ?: repo.findByEmail(dto.usernameOrEmail)
            ?: throw IllegalArgumentException("Invalid username/email or password")

        if (!encoder.matches(dto.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid username/email or password")
        }

        val token = jwtService.generate(user.username, user.role)

        return LoginResponseDto(
            userId = user.id,
            username = user.username,
            role = user.role,
            accessToken = token
        )
    }
}