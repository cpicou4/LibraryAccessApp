package com.example.frontend.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.frontend.net.ApiClient
import com.example.frontend.net.TokenStore
import com.example.frontend.net.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen() {
    var books by remember { mutableStateOf<List<BookGetDto>>(emptyList()) }
    var output by remember { mutableStateOf("Ready.") }
    var status by remember { mutableStateOf("") }

    var bookId by remember { mutableStateOf("") }
    var recordId by remember { mutableStateOf("") }
    var loanDays by remember { mutableStateOf("14") }

    val currentUserId = TokenStore.userId
    val userLabel = TokenStore.username?.let { u -> "$u (${TokenStore.role ?: ""})" } ?: "Not logged in"
    val scope = rememberCoroutineScope()

    //Temp Ui
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        //Header
        item {
            Text("Logged in: $userLabel", style = MaterialTheme.typography.titleMedium)
        }

        //The books section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    scope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) { ApiClient.api.getBooks() }
                            books = result
                            status = "Fetched ${result.size} books"
                        } catch (e: Exception) {
                            status = "Error loading books: ${e.message}"
                        }
                    }
                }) { Text("Get All Books") }

                Text(status, style = MaterialTheme.typography.bodyMedium)
            }
        }

        //List of books each as an item
        items(books) { book ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("${book.title}", style = MaterialTheme.typography.titleMedium)
                    Text("Author: ${book.author}")
                    Text("Publisher: ${book.publisher}")
                    Text("Category: ${book.category}")
                    Text("ISBN: ${book.isbn ?: "—"}")
                    Text("Available: ${book.availableCopies}/${book.totalCopies}")
                    Text("Year: ${book.publicationYear}")
                    val desc = book.description?.takeIf { it.isNotBlank() } ?: "No description"
                    Text("Description: ${desc.take(200)}${if (desc.length > 200) "…" else ""}")
                }
            }
        }

        //[ADMIN] Get all borrowings
        item {
            Button(onClick = {
                scope.launch {
                    try {
                        val list = withContext(Dispatchers.IO) { ApiClient.api.getBorrowings() }
                        output = "All borrowings (${list.size})\n" +
                                list.joinToString("\n") { "#${it.id} u${it.userId} b${it.bookId} ${it.status}" }
                    } catch (e: Exception) {
                        output = "Get borrowings (ADMIN) error: ${e.message}"
                    }
                }
            }) { Text("Get Borrowings (ADMIN)") }
        }

        //My active borrowings
        item {
            Button(onClick = {
                scope.launch {
                    if (currentUserId == null) {
                        output = "Please login first."
                        return@launch
                    }
                    try {
                        val list = withContext(Dispatchers.IO) {
                            ApiClient.api.getActiveBorrowingsByUser(currentUserId)
                        }
                        output = "Active for user $currentUserId (${list.size})\n" +
                                list.joinToString("\n") { "#${it.id} due ${it.dueDate} (${it.status})" }
                    } catch (e: Exception) {
                        output = "My borrowings error: ${e.message}"
                    }
                }
            }) { Text("My Active Borrowings") }
        }

        //checkout
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bookId, onValueChange = { bookId = it },
                    label = { Text("Book ID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = loanDays, onValueChange = { loanDays = it },
                    label = { Text("Loan Days") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    scope.launch {
                        val uid = TokenStore.userId
                        if (uid == null) { output = "Please login first."; return@launch }
                        try {
                            val dto = BorrowingRecordCreateDto(
                                bookId = bookId.toInt(),
                                userId = uid,
                                loanDays = loanDays.toInt()
                            )
                            val rec = withContext(Dispatchers.IO) { ApiClient.api.checkOut(dto) }
                            output = "Checked out: record #${rec.id}, due ${rec.dueDate}"
                        } catch (e: Exception) {
                            output = "Checkout error: ${e.message}"
                        }
                    }
                }) { Text("Checkout Book (as me)") }
            }
        }

        //Return a book
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = recordId, onValueChange = { recordId = it },
                    label = { Text("Borrow Record ID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    scope.launch {
                        try {
                            val rec = withContext(Dispatchers.IO) {
                                ApiClient.api.returnBook(recordId.toInt(), BorrowingRecordReturnDto())
                            }
                            output = "Returned: record #${rec.id}, status=${rec.status}, daysLate=${rec.daysLate}, fine=${rec.currentFine}"
                        } catch (e: Exception) {
                            output = "Return error: ${e.message}"
                        }
                    }
                }) { Text("Return Book") }
            }
        }

        //[ADMIN] get all users
        item {
            Button(onClick = {
                scope.launch {
                    try {
                        val list = withContext(Dispatchers.IO) { ApiClient.api.getUsers() }
                        output = "Users (${list.size})\n" +
                                list.joinToString("\n") { "${it.id}: ${it.username} <${it.email}> [${it.role}]" }
                    } catch (e: Exception) {
                        output = "Get users (ADMIN) error: ${e.message}"
                    }
                }
            }) { Text("Get Users (ADMIN)") }
        }

        //Output area
        item {
            Divider()
            Spacer(Modifier.height(8.dp))
            Text(output)
        }
    }
}
