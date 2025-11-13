package com.example.library_backend.books

import jakarta.persistence.*

@Entity
@Table(name = "authors")
class Author(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    val id: Int = 0,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @ManyToMany(mappedBy = "authors")
    val books: MutableSet<Book> = mutableSetOf()
)