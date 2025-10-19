package com.example.library_backend.security

import com.example.library_backend.users.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val repo: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val u = repo.findByUsername(username) ?: repo.findByEmail(username)
        ?: throw org.springframework.security.core.userdetails.UsernameNotFoundException("Not found")
        // Spring expects ROLE_* prefix
        val auth = listOf(SimpleGrantedAuthority("ROLE_${u.role}"))
        return User(u.username, u.passwordHash, auth)
    }
}