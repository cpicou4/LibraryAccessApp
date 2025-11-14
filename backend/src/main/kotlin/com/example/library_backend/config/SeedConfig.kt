package com.example.library_backend.config

import com.example.library_backend.books.Book
import com.example.library_backend.books.BookCreateDto
import com.example.library_backend.books.BookRepository
import com.example.library_backend.books.BookService
import com.example.library_backend.users.User
import com.example.library_backend.users.UserRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.security.crypto.password.PasswordEncoder
import org.apache.commons.csv.CSVFormat
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Configuration
@Profile("demo")
class SeedConfig {

    @Bean
    fun seedBooks(
        bookRepo: BookRepository,
        bookService: BookService
    ) = ApplicationRunner {

        try {
            val resource = ClassPathResource("seed/books.csv")
            if (!resource.exists()) {
                println("Seed WARNING: seed/books.csv not found, skipping book seeding.")
                return@ApplicationRunner
            }

            resource.inputStream.use { input ->
                InputStreamReader(input, StandardCharsets.UTF_8).use { reader ->
                    val csvFormat = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .build()

                    val parser = csvFormat.parse(reader)

                    var created = 0
                    var skippedExisting = 0
                    var skippedInvalid = 0

                    for (record in parser) {
                        val title = record.get("title").trim()
                        if (title.isBlank()) {
                            println("Seed: skipping row with blank title: $record")
                            skippedInvalid++
                            continue
                        }

                        val rawIsbn = record.get("isbn").trim()
                        if (rawIsbn.isBlank()) {
                            println("Seed: skipping row with blank ISBN for title '$title'")
                            skippedInvalid++
                            continue
                        }

                        val existing = bookRepo.findByIsbn(rawIsbn)
                        if (existing != null) {
                            skippedExisting++
                            continue
                        }

                        val publisher = record.get("publisher").trim().ifBlank { null }
                        val yearStr = record.get("publicationYear").trim()
                        val totalStr = record.get("totalCopies").trim()
                        val desc = record.get("description").trim().ifBlank { null }
                        val coverImageUrl = record.get("coverImageUrl").trim().ifBlank { null }
                        val authorsStr = record.get("authors").trim()
                        val categoriesStr = record.get("categories").trim()

                        val publicationYear = yearStr.toIntOrNull()
                        val totalCopies = totalStr.toIntOrNull() ?: 1

                        val authors = if (authorsStr.isBlank()) {
                            emptyList()
                        } else {
                            authorsStr.split(';')
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                        }

                        val categories = if (categoriesStr.isBlank()) {
                            emptyList()
                        } else {
                            categoriesStr.split(';')
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                        }

                        val dto = BookCreateDto(
                            isbn = rawIsbn,
                            title = title,
                            publisher = publisher,
                            publicationYear = publicationYear,
                            totalCopies = totalCopies,
                            description = desc,
                            coverImageUrl = coverImageUrl,
                            authors = authors,
                            categories = categories
                        )

                        bookService.create(dto)
                        created++
                    }

                    println(
                        "Seed: successfully processed CSV. " +
                                "Created=$created, SkippedExisting=$skippedExisting, SkippedInvalid=$skippedInvalid."
                    )
                }
            }
        } catch (ex: Exception) {
            println("Seed ERROR: failed to seed books from CSV")
            println("  Type   : ${ex::class.qualifiedName}")
            println("  Message: ${ex.message}")
            ex.printStackTrace()
            throw ex
        }
    }

    @Bean
    fun seedAdminUser(userRepo: UserRepository, encoder: PasswordEncoder) = ApplicationRunner {
        if (!userRepo.existsByUsername("admin")) {
            val admin = User(
                username = "admin",
                email = "admin@example.com",
                passwordHash = encoder.encode("password"),
                fullName = "Admin User",
                role = "ADMIN"
            )
            userRepo.save(admin)
        }
    }
}