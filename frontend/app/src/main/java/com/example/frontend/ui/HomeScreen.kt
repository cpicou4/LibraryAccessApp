package com.example.frontend.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.frontend.net.ApiClient
import com.example.frontend.net.TokenStore
import com.example.frontend.net.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var books by remember { mutableStateOf<List<BookGetDto>>(emptyList()) }
    var output by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var bookId by remember { mutableStateOf("") }
    var recordId by remember { mutableStateOf("") }
    var loanDays by remember { mutableStateOf("14") }

    val currentUserId = TokenStore.userId
    val username = TokenStore.username ?: "Guest"
    val userRole = TokenStore.role ?: "USER"
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library Access") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info Card
            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = username,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = userRole,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Books Section
            item {
                SectionHeader(title = "Book Catalog", icon = Icons.Default.Menu)
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val result = withContext(Dispatchers.IO) { ApiClient.api.getBooks() }
                                books = result
                                status = "Found ${result.size} book${if (result.size != 1) "s" else ""}"
                            } catch (e: Exception) {
                                status = "Error: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Load All Books")
                }

                if (status.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (status.startsWith("Error")) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            // Book List
            items(books) { book ->
                BookCard(book = book)
            }

            // My Borrowings Section
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "My Borrowings", icon = Icons.Default.List)
            }

            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            if (currentUserId == null) {
                                output = "Please login first."
                                return@launch
                            }
                            try {
                                val list = withContext(Dispatchers.IO) {
                                    ApiClient.api.getActiveBorrowingsByUser(currentUserId)
                                }
                                output = if (list.isEmpty()) {
                                    "You have no active borrowings."
                                } else {
                                    "Active Borrowings (${list.size}):\n\n" +
                                            list.joinToString("\n") { 
                                                "• Record #${it.id}\n  Due: ${it.dueDate}\n  Status: ${it.status}\n"
                                            }
                                }
                            } catch (e: Exception) {
                                output = "Error: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("View My Active Borrowings")
                }
            }

            // Checkout Section
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "Checkout Book", icon = Icons.Default.Add)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = bookId,
                            onValueChange = { bookId = it },
                            label = { Text("Book ID") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = loanDays,
                            onValueChange = { loanDays = it },
                            label = { Text("Loan Period (Days)") },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    val uid = TokenStore.userId
                                    if (uid == null) {
                                        output = "Please login first."
                                        return@launch
                                    }
                                    if (bookId.isBlank()) {
                                        output = "Please enter a Book ID."
                                        return@launch
                                    }
                                    try {
                                        val dto = BorrowingRecordCreateDto(
                                            bookId = bookId.toInt(),
                                            userId = uid,
                                            loanDays = loanDays.toInt()
                                        )
                                        val rec = withContext(Dispatchers.IO) { ApiClient.api.checkOut(dto) }
                                        output = "✓ Successfully checked out!\n\nRecord ID: #${rec.id}\nDue Date: ${rec.dueDate}"
                                        bookId = ""
                                        loanDays = "14"
                                    } catch (e: Exception) {
                                        output = "Checkout failed: ${e.message}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Checkout Book")
                        }
                    }
                }
            }

            // Return Section
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "Return Book", icon = Icons.Default.Check)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = recordId,
                            onValueChange = { recordId = it },
                            label = { Text("Borrowing Record ID") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    if (recordId.isBlank()) {
                                        output = "Please enter a Record ID."
                                        return@launch
                                    }
                                    try {
                                        val rec = withContext(Dispatchers.IO) {
                                            ApiClient.api.returnBook(recordId.toInt(), BorrowingRecordReturnDto())
                                        }
                                        output = "✓ Book returned successfully!\n\n" +
                                                "Record ID: #${rec.id}\n" +
                                                "Status: ${rec.status}\n" +
                                                "Days Late: ${rec.daysLate ?: 0}\n" +
                                                "Fine: $${rec.currentFine ?: 0.0}"
                                        recordId = ""
                                    } catch (e: Exception) {
                                        output = "Return failed: ${e.message}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Return Book")
                        }
                    }
                }
            }

            // Admin Section (only show if admin)
            if (userRole.contains("ADMIN", ignoreCase = true)) {
                item {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader(title = "Admin Functions", icon = Icons.Default.Settings)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val list = withContext(Dispatchers.IO) { ApiClient.api.getBorrowings() }
                                        output = "All Borrowings (${list.size}):\n\n" +
                                                list.joinToString("\n") { 
                                                    "• Record #${it.id} - User ${it.userId}, Book ${it.bookId}\n  Status: ${it.status}\n"
                                                }
                                    } catch (e: Exception) {
                                        output = "Error: ${e.message}"
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("All Borrowings", maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val list = withContext(Dispatchers.IO) { ApiClient.api.getUsers() }
                                        output = "All Users (${list.size}):\n\n" +
                                                list.joinToString("\n") { 
                                                    "• ${it.username} (ID: ${it.id})\n  ${it.email}\n  Role: ${it.role}\n"
                                                }
                                    } catch (e: Exception) {
                                        output = "Error: ${e.message}"
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("All Users", maxLines = 1)
                        }
                    }
                }
            }

            // Output Section
            if (output.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Result",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { output = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                text = output,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Bottom Spacer
            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun BookCard(book: BookGetDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Author and Publisher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(label = "Author", value = book.author ?: "Unknown")
                InfoChip(label = "Publisher", value = book.publisher ?: "Unknown")
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Category, ISBN, Year
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(book.category ?: "Uncategorized") },
                    leadingIcon = { Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                book.isbn?.let { isbn ->
                    if (isbn.isNotBlank()) {
                        AssistChip(
                            onClick = { },
                            label = { Text("ISBN: $isbn") }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Availability
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (book.availableCopies > 0) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (book.availableCopies > 0) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (book.availableCopies > 0) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${book.availableCopies} of ${book.totalCopies} available",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "Year: ${book.publicationYear}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Description
            book.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = desc.take(150) + if (desc.length > 150) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}