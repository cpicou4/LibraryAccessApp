# Team RGB Library Access App

A full-stack library management system with a Kotlin Spring Boot backend and Android frontend.

## Project Description

Library Access App is a comprehensive library management system that allows users to browse books, checkout and return materials, and make reservations. The system includes role-based access control with separate functionality for regular users and administrators.

## Features

### User Features
- User registration and authentication
- Browse book catalog with detailed information
- Checkout available books with customizable loan periods
- Return borrowed books with automatic fine calculation
- Reserve unavailable books with queue management
- View active borrowings and reservations
- Secure logout functionality

### Admin Features
- View all borrowing records
- View all user accounts
- View all reservations
- Access to authors and categories data
- Full administrative oversight of library operations

### Technical Features
- JWT-based authentication
- Role-based authorization (USER/ADMIN)
- Many-to-many relationships (Books ↔ Authors, Books ↔ Categories)
- Automatic data seeding with demo content
- H2 in-memory database with file persistence
- RESTful API architecture
- Material Design 3 UI

## Technology Stack

### Backend
- **Language:** Kotlin
- **Framework:** Spring Boot 3.5.6
- **Database:** H2 Database (file-based)
- **Security:** Spring Security with JWT
- **Build Tool:** Gradle

### Frontend
- **Language:** Kotlin
- **Framework:** Jetpack Compose
- **UI:** Material Design 3
- **HTTP Client:** Retrofit
- **Build Tool:** Gradle

## Prerequisites

Before running this application, ensure you have the following installed:

- **Java Development Kit (JDK) 21** - [Download here](https://www.oracle.com/java/technologies/downloads/#java21)
- **Android Studio** - [Download here](https://developer.android.com/studio)

## Installation & Setup

### 1. Backend Setup

#### Step 1: Navigate to the backend directory
```bash
cd backend
```

#### Step 2: Run the backend with demo profile
```bash
./gradlew bootRun --args='--spring.profiles.active=demo'
```

Or on Windows:
```bash
gradlew.bat bootRun --args='--spring.profiles.active=demo'
```

The backend will start on **http://localhost:8080**

**Note:** The `demo` profile automatically seeds the database with:
- 12 sample books (classics and popular titles)
- An admin account (username: `admin`, password: `password`)

#### Verify Backend is Running
- You should see console output indicating "Started LibraryBackendApplicationKt"

### 2. Frontend Setup

#### Step 1: Open Android Studio
1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to and select the `frontend` folder

#### Step 2: Set up Android Emulator
1. In Android Studio, go to **Tools → Device Manager**
2. Create a new Virtual Device (recommended: Pixel 5 with API 34)
3. Start the emulator

#### Step 3: Run the Application
1. Wait for Gradle sync to complete
2. Click the green "Run" button (▶️) or press `Shift + F10`
3. Select your emulator from the deployment target
4. The app will install and launch automatically

**Alternative:** Run from command line:
```bash
cd frontend
./gradlew installDebug
```

## Default Login Credentials

### Admin Account
- **Username:** `admin`
- **Password:** `password`
- **Capabilities:** Full access to all features including user management, viewing all borrowings, and administrative functions

### Creating a User Account
1. Launch the app
2. Click "Register" on the login screen
3. Fill in the registration form
4. Login with your new credentials

## Troubleshooting

### Backend won't start
- Ensure Java 21 is installed: `java -version`
- Check if port 8080 is already in use
- Delete `backend/data/` folder and restart

### Frontend won't connect to backend
- Verify backend is running on http://localhost:8080
- Check that emulator is running
- Ensure network configuration in `Api.kt` points to correct URL

### Books not loading
- Ensure backend was started with `--spring.profiles.active=demo`
- Check backend console for seed messages
- Verify database exists in `backend/data/`

### Login fails
- Use admin credentials: `admin` / `password`
- Or register a new account
- Check backend logs for authentication errors
