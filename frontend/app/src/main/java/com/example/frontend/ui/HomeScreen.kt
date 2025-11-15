package com.example.frontend.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.frontend.net.ApiClient
import com.example.frontend.net.TokenStore
import com.example.frontend.net.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit = {}) {
    var books by remember { mutableStateOf<List<BookGetDto>>(emptyList()) }
    var output by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loanDays by remember { mutableStateOf("14") }
    val currentUserId = TokenStore.userId
    val username = TokenStore.username ?: "Guest"
    val userRole = TokenStore.role ?: "USER"
    val scope = rememberCoroutineScope()

    // State for dropdowns
    var activeBorrowings by remember { mutableStateOf<List<BorrowingRecordGetDto>>(emptyList()) }
    var selectedBookId by remember { mutableStateOf<Int?>(null) }
    var selectedRecordId by remember { mutableStateOf<Int?>(null) }
    var expandedBookDropdown by remember { mutableStateOf(false) }
    var expandedRecordDropdown by remember { mutableStateOf(false) }

    // Load books automatically on first composition
    LaunchedEffect(Unit) {
        try {
            val result = withContext(Dispatchers.IO) { ApiClient.api.getBooks() }
            books = result
            status = "Found ${result.size} book${if (result.size != 1) "s" else ""}"
        } catch (e: Exception) {
            status = "Error loading books: ${e.message}"
        }
    }

    // Load active borrowings when user is logged in
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            try {
                activeBorrowings =
                        withContext(Dispatchers.IO) {
                            ApiClient.api.getActiveBorrowingsByUser(currentUserId)
                        }
            } catch (e: Exception) {
                // Silently fail, user can manually refresh if needed
            }
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Library Access") },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor =
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                        actions = {
                            if (currentUserId != null) {
                                TextButton(
                                        onClick = {
                                            // Clear token and user info
                                            TokenStore.token = null
                                            TokenStore.userId = null
                                            TokenStore.username = null
                                            TokenStore.role = null
                                            // Navigate back to auth screen
                                            onLogout()
                                        }
                                ) {
                                    Text(
                                            "Logout",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                )
            }
    ) { paddingValues ->
        LazyColumn(
                modifier =
                        Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info Card
            item {
                Spacer(Modifier.height(8.dp))
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor =
                                                MaterialTheme.colorScheme.secondaryContainer
                                )
                ) {
                    Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                                    color =
                                            MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                    alpha = 0.7f
                                            )
                            )
                        }
                    }
                }
            }

            // Books Section
            item { SectionHeader(title = "Book Catalog", icon = Icons.Default.Menu) }

            if (status.isNotEmpty()) {
                item {
                    Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                    if (status.startsWith("Error")) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                    )
                }
            }

            // Book List
            items(books) { book ->
                BookCard(
                        book = book,
                        onReserve = { bookId ->
                            scope.launch {
                                val uid = TokenStore.userId
                                if (uid == null) {
                                    output = "Please login first."
                                    return@launch
                                }
                                try {
                                    val dto = ReservationCreateDto(userId = uid, bookId = bookId)
                                    val reservation =
                                            withContext(Dispatchers.IO) {
                                                ApiClient.api.createReservation(dto)
                                            }
                                    output =
                                            "✓ Reservation created!\n\n" +
                                                    "Reservation #${reservation.id}\n" +
                                                    "Queue Position: ${reservation.queuePosition ?: "Next"}\n" +
                                                    "Valid until: ${reservation.expiryDate}"
                                } catch (e: Exception) {
                                    output = "Reservation failed: ${e.message}"
                                }
                            }
                        }
                )
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
                                    val list =
                                            withContext(Dispatchers.IO) {
                                                ApiClient.api.getActiveBorrowingsByUser(
                                                        currentUserId
                                                )
                                            }
                                    activeBorrowings = list
                                    output =
                                            if (list.isEmpty()) {
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

            // My Reservations Section
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "My Reservations", icon = Icons.Default.DateRange)
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
                                    val list =
                                            withContext(Dispatchers.IO) {
                                                ApiClient.api.getActiveReservationsByUser(
                                                        currentUserId
                                                )
                                            }
                                    output =
                                            if (list.isEmpty()) {
                                                "You have no active reservations."
                                            } else {
                                                "Active Reservations (${list.size}):\n\n" +
                                                        list.joinToString("\n") { r ->
                                                            val book =
                                                                    books.find { it.id == r.bookId }
                                                            "• ${book?.title ?: "Book #${r.bookId}"}\n" +
                                                                    "  Reservation #${r.id}\n" +
                                                                    "  Queue Position: ${r.queuePosition ?: "Next"}\n" +
                                                                    "  Valid: ${r.reservationDate} to ${r.expiryDate}\n" +
                                                                    "  Status: ${r.status}\n"
                                                        }
                                            }
                                } catch (e: Exception) {
                                    output = "Error: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("View My Active Reservations")
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
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Book Dropdown
                        ExposedDropdownMenuBox(
                                expanded = expandedBookDropdown,
                                onExpandedChange = { expandedBookDropdown = !expandedBookDropdown }
                        ) {
                            OutlinedTextField(
                                    value =
                                            selectedBookId?.let { id ->
                                                books.find { it.id == id }?.let { book ->
                                                    "${book.title} (ID: $id)"
                                                }
                                            }
                                                    ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Book") },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Expand") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Info, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors()
                            )

                            ExposedDropdownMenu(
                                    expanded = expandedBookDropdown,
                                    onDismissRequest = { expandedBookDropdown = false }
                            ) {
                                // Filter to only show available books
                                val availableBooks = books.filter { it.availableCopies > 0 }

                                if (availableBooks.isEmpty()) {
                                    DropdownMenuItem(
                                            text = { Text("No available books") },
                                            onClick = {},
                                            enabled = false
                                    )
                                } else {
                                    availableBooks.forEach { book ->
                                        DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(
                                                                text = book.title,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                        Text(
                                                                text =
                                                                        "ID: ${book.id} • ${book.availableCopies} available",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodySmall,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface.copy(
                                                                                alpha = 0.6f
                                                                        )
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedBookId = book.id
                                                    expandedBookDropdown = false
                                                }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                                value = loanDays,
                                onValueChange = { loanDays = it },
                                label = { Text("Loan Period (Days)") },
                                leadingIcon = {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                },
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Number),
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
                                        if (selectedBookId == null) {
                                            output = "Please select a book."
                                            return@launch
                                        }
                                        try {
                                            val dto =
                                                    BorrowingRecordCreateDto(
                                                            bookId = selectedBookId!!,
                                                            userId = uid,
                                                            loanDays = loanDays.toInt()
                                                    )
                                            val rec =
                                                    withContext(Dispatchers.IO) {
                                                        ApiClient.api.checkOut(dto)
                                                    }
                                            output =
                                                    "✓ Successfully checked out!\n\nRecord ID: #${rec.id}\nDue Date: ${rec.dueDate}"
                                            selectedBookId = null
                                            loanDays = "14"

                                            // Refresh books and borrowings
                                            books =
                                                    withContext(Dispatchers.IO) {
                                                        ApiClient.api.getBooks()
                                                    }
                                            activeBorrowings =
                                                    withContext(Dispatchers.IO) {
                                                        ApiClient.api.getActiveBorrowingsByUser(uid)
                                                    }
                                        } catch (e: Exception) {
                                            output = "Checkout failed: ${e.message}"
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectedBookId != null
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
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Borrowing Record Dropdown
                        ExposedDropdownMenuBox(
                                expanded = expandedRecordDropdown,
                                onExpandedChange = {
                                    expandedRecordDropdown = !expandedRecordDropdown
                                }
                        ) {
                            OutlinedTextField(
                                    value =
                                            selectedRecordId?.let { id ->
                                                activeBorrowings.find { it.id == id }?.let { record
                                                    ->
                                                    val book = books.find { it.id == record.bookId }
                                                    "Record #$id: ${book?.title ?: "Book ${record.bookId}"}"
                                                }
                                            }
                                                    ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Borrowing Record") },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Expand") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors()
                            )

                            ExposedDropdownMenu(
                                    expanded = expandedRecordDropdown,
                                    onDismissRequest = { expandedRecordDropdown = false }
                            ) {
                                if (activeBorrowings.isEmpty()) {
                                    DropdownMenuItem(
                                            text = { Text("No active borrowings") },
                                            onClick = {},
                                            enabled = false
                                    )
                                } else {
                                    activeBorrowings.forEach { record ->
                                        val book = books.find { it.id == record.bookId }
                                        DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(
                                                                text = book?.title
                                                                                ?: "Book ID: ${record.bookId}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                        Text(
                                                                text =
                                                                        "Record #${record.id} • Due: ${record.dueDate}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodySmall,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface.copy(
                                                                                alpha = 0.6f
                                                                        )
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedRecordId = record.id
                                                    expandedRecordDropdown = false
                                                }
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                                onClick = {
                                    scope.launch {
                                        val uid = TokenStore.userId
                                        if (uid == null) {
                                            output = "Please login first."
                                            return@launch
                                        }
                                        if (selectedRecordId == null) {
                                            output = "Please select a borrowing record."
                                            return@launch
                                        }
                                        try {
                                            val rec =
                                                    withContext(Dispatchers.IO) {
                                                        ApiClient.api.returnBook(
                                                                selectedRecordId!!,
                                                                BorrowingRecordReturnDto()
                                                        )
                                                    }
                                            output =
                                                    "✓ Book returned successfully!\n\n" +
                                                            "Record ID: #${rec.id}\n" +
                                                            "Status: ${rec.status}\n" +
                                                            "Days Late: ${rec.daysLate ?: 0}\n" +
                                                            "Fine: $${rec.currentFine ?: 0.0}"
                                            selectedRecordId = null

                                            // Refresh books and borrowings
                                            books =
                                                    withContext(Dispatchers.IO) {
                                                        ApiClient.api.getBooks()
                                                    }
                                            activeBorrowings =
                                                    withContext(Dispatchers.IO) {
                                                        ApiClient.api.getActiveBorrowingsByUser(uid)
                                                    }
                                        } catch (e: Exception) {
                                            output = "Return failed: ${e.message}"
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectedRecordId != null,
                                colors =
                                        ButtonDefaults.buttonColors(
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
                                            val list =
                                                    withContext(Dispatchers.IO) {
                                                        ApiClient.api.getBorrowings()
                                                    }
                                            output =
                                                    "All Borrowings (${list.size}):\n\n" +
                                                            list.joinToString("\n") {
                                                                "• Record #${it.id} - User ${it.userId}, Book ${it.bookId}\n  Status: ${it.status}\n"
                                                            }
                                        } catch (e: Exception) {
                                            output = "Error: ${e.message}"
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                        ) { Text("All Borrowings", maxLines = 1) }

                        OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val list =
                                                    withContext(Dispatchers.IO) {
                                                        ApiClient.api.getUsers()
                                                    }
                                            output =
                                                    "All Users (${list.size}):\n\n" +
                                                            list.joinToString("\n") {
                                                                "• ${it.username} (ID: ${it.id})\n  ${it.email}\n  Role: ${it.role}\n"
                                                            }
                                        } catch (e: Exception) {
                                            output = "Error: ${e.message}"
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                        ) { Text("All Users", maxLines = 1) }
                    }
                }

                // Admin data viewing
                item {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader(title = "Data Overview", icon = Icons.Default.List)
                }

                item {
                    Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val list =
                                                        withContext(Dispatchers.IO) {
                                                            ApiClient.api.getReservations()
                                                        }
                                                output =
                                                        if (list.isEmpty())
                                                                "No reservations in system."
                                                        else
                                                                "All Reservations (${list.size}):\n\n" +
                                                                        list.joinToString("\n") { r
                                                                            ->
                                                                            "• #${r.id} - User ${r.userId}, Book ${r.bookId}\n  Queue: ${r.queuePosition ?: "-"} | ${r.status}\n  ${r.reservationDate} to ${r.expiryDate}\n"
                                                                        }
                                            } catch (e: Exception) {
                                                output = "Error: ${e.message}"
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                            ) { Text("All Reservations") }
                        }

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val authors =
                                                        withContext(Dispatchers.IO) {
                                                            ApiClient.api.getAuthors()
                                                        }
                                                output =
                                                        if (authors.isEmpty()) {
                                                            "No authors in system."
                                                        } else {
                                                            "Authors (${authors.size}):\n\n" +
                                                                    authors.joinToString("\n") { a
                                                                        ->
                                                                        "• #${a.id} - ${a.name}"
                                                                    }
                                                        }
                                            } catch (e: Exception) {
                                                output = "Error: ${e.message}"
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                            ) { Text("All Authors") }

                            OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val categories =
                                                        withContext(Dispatchers.IO) {
                                                            ApiClient.api.getCategories()
                                                        }
                                                output =
                                                        if (categories.isEmpty()) {
                                                            "No categories in system."
                                                        } else {
                                                            "Categories (${categories.size}):\n\n" +
                                                                    categories.joinToString("\n") {
                                                                            c ->
                                                                        "• #${c.id} - ${c.type}"
                                                                    }
                                                        }
                                            } catch (e: Exception) {
                                                output = "Error: ${e.message}"
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                            ) { Text("All Categories") }
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
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor =
                                                    MaterialTheme.colorScheme.surfaceVariant
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
            item { Spacer(Modifier.height(16.dp)) }
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
fun BookCard(book: BookGetDto, onReserve: (Int) -> Unit = {}) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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

            // Author(s) and Publisher
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val authorLabel =
                        when {
                            book.authors.isEmpty() -> "Unknown"
                            book.authors.size == 1 -> book.authors.first()
                            else -> book.authors.joinToString(", ")
                        }
                InfoChip(label = "Author(s)", value = authorLabel)
                InfoChip(label = "Publisher", value = book.publisher ?: "Unknown")
            }
            Spacer(Modifier.height(8.dp))

            // Categories, ISBN, Year
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categoryLabel =
                        when {
                            book.categories.isEmpty() -> "Uncategorized"
                            else -> book.categories.joinToString(", ")
                        }
                AssistChip(
                        onClick = {},
                        label = { Text(categoryLabel) },
                        leadingIcon = {
                            Icon(
                                    Icons.Default.Menu,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                            )
                        }
                )
                book.isbn?.let { isbn ->
                    if (isbn.isNotBlank()) {
                        AssistChip(onClick = {}, label = { Text("ISBN: $isbn") })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            // Availability card
            Card(
                    colors =
                            CardDefaults.cardColors(
                                    containerColor =
                                            if (book.availableCopies > 0) {
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
                            imageVector =
                                    if (book.availableCopies > 0) Icons.Default.CheckCircle
                                    else Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint =
                                    if (book.availableCopies > 0) {
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
                            text = "Year: ${book.publicationYear ?: "-"}",
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

            // Reserve button for unavailable books
            if (book.availableCopies == 0) {
                Spacer(Modifier.height(12.dp))
                Button(
                        onClick = { onReserve(book.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reserve This Book")
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
