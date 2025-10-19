package com.example.library_backend.config

import com.example.library_backend.books.Book
import com.example.library_backend.books.BookRepository
import com.example.library_backend.users.User
import com.example.library_backend.users.UserRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@Profile("demo")
class SeedConfig {

    @Bean
    fun seedBooks(repo: BookRepository) = ApplicationRunner {
        if (repo.count() == 0L) {
            repo.saveAll(
                listOf(
                    Book(
                        isbn = "9787868386552",
                        title = "The Lightning Thief (Percy Jackson and the Olympians, Book 1)",
                        author = "Rick Riordan",
                        publisher = "Disney-Hyperion",
                        publicationYear = 2006,
                        category = "Fantasy",
                        totalCopies = 5,
                        availableCopies = 5,
                        description = "Twelve-year-old Percy Jackson is on the most dangerous quest of his life. " +
                                      "With the help of a satyr and a daughter of Athena, Percy must journey across the United " +
                                      "States to catch a thief who has stolen the original weapon of mass destruction — Zeus’ " +
                                      "master bolt. Along the way, he must face a host of mythological enemies determined to stop him. " +
                                      "Most of all, he must come to terms with a father he has never known, and an Oracle that has " +
                                      "warned him of betrayal by a friend.",
                        coverImageUrl = "https://images.isbndb.com/covers/14659213484989.jpg"
                    ),
                    Book(
                        isbn = "9781423103349",
                        title = "The Sea of Monsters (Percy Jackson and the Olympians, Book 2)",
                        author = "Rick Riordan",
                        publisher = "Disney-Hyperion",
                        publicationYear = 2007,
                        category = "Fantasy",
                        totalCopies = 5,
                        availableCopies = 5,
                        description = "When Thalia’s tree is mysteriously poisoned, the magical borders of Camp Half-Blood " +
                                      "begin to fail. Now Percy and his friends have just days to find the only magic item " +
                                      "powerful to save the camp before it is overrun by monsters. The catch: they must sail " +
                                      "into the Sea of Monsters to find it. Along the way, Percy must stage a daring rescue operation " +
                                      "to save his old friend Grover, and he learns a terrible secret about his own family, which makes " +
                                      "him question whether being the son of Poseidon is an honor or a curse",
                        coverImageUrl = "https://images.isbndb.com/covers/18715233482694.jpg"
                    )
                )
            )
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