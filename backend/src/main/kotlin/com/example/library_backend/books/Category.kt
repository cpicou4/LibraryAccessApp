package com.example.library_backend.books

import jakarta.persistence.*

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    val id: Int = 0,

    @Column(name = "type", nullable = false, length = 100)
    val type: String,

    @ManyToMany(mappedBy = "categories")
    val books: MutableSet<Book> = mutableSetOf()
)