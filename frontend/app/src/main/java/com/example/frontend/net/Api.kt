package com.example.frontend.net

import com.example.frontend.net.dto.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Response

object TokenStore {
    var token: String? = null
    var userId: Int? = null
    var username: String? = null
    var role: String? = null
}

//Adds Authorization: Bearer {token} only if we have one
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val req = TokenStore.token?.let { t ->
            chain.request().newBuilder().addHeader("Authorization", "Bearer $t").build()
        } ?: chain.request()
        return chain.proceed(req)
    }
}

interface ApiService {
    // Auth
    @POST("api/auth/register") suspend fun register(@Body dto: UserCreateDto): Any
    @POST("api/auth/login") suspend fun login(@Body dto: LoginRequestDto): LoginResponseDto

    /* ----------------------------- Public ------------------------------------*/
    //Books
    @GET("api/books") suspend fun getBooks(): List<BookGetDto>
    @GET("/api/books/{id}") suspend fun getBookById(@Path("id") id: Int): BookGetDto

    //Borrowings Records
    @GET("api/borrowings/user/{userId}/active") suspend fun getActiveBorrowingsByUser(@Path("userId") userId: Int): List<BorrowingRecordGetDto>
    @POST("api/borrowings") suspend fun checkOut(@Body dto: BorrowingRecordCreateDto): BorrowingRecordGetDto
    @PUT("api/borrowings/{id}/return") suspend fun returnBook(@Path("id") id: Int, @Body dto: BorrowingRecordReturnDto): BorrowingRecordGetDto

    //Reservations
    @POST("/api/reservations") suspend fun createReservation(@Body dto: ReservationCreateDto): ReservationGetDto
    @PUT("/api/reservations/{id}/cancel") suspend fun cancelReservation(@Path("id") reservationId: Int, @Query("userId") userId: Int? = null): ReservationGetDto
    @GET("/api/reservations/user/{userId}/active") suspend fun getActiveReservationsByUser(@Path("userId") userId: Int): List<ReservationGetDto>

    //Users
    @GET("/api/users/{id}") suspend fun getUserById(@Path("id") id: Int): UserGetDto
    @PUT("/api/users/{id}") suspend fun updateUser(@Path("id") id: Int, @Body dto: UserUpdateDto): UserGetDto

    /* ----------------------------- Admin protected ------------------------------------*/
    //Books
    @POST("/api/books") suspend fun createBook(@Body dto: BookCreateUpdateDto): BookGetDto
    @PUT("/api/books/{id}") suspend fun updateBook(@Path("id") id: Int, @Body dto: BookCreateUpdateDto): BookGetDto
    @DELETE("/api/books/{id}") suspend fun deleteBook(@Path("id") id: Int): Response<Unit>

    //Borrowing Records
    @GET("api/borrowings") suspend fun getBorrowings(): List<BorrowingRecordGetDto>
    @GET("/api/borrowings/{id}") suspend fun getBorrowingById(@Path("id") id: Int): BorrowingRecordGetDto
    @GET("/api/borrowings/book/{bookId}/active") suspend fun getActiveBorrowingsByBook(@Path("bookId") bookId: Int): List<BorrowingRecordGetDto>

    //Reservations
    @GET("/api/reservations") suspend fun getReservations(): List<ReservationGetDto>
    @GET("/api/reservations/{id}") suspend fun getReservationById(@Path("id") id: Int): ReservationGetDto
    @GET("/api/reservations/book/{bookId}/active") suspend fun getActiveReservationsByBook(@Path("bookId") bookId: Int): List<ReservationGetDto>

    //Users
    @GET("api/users") suspend fun getUsers(): List<UserGetDto>
    @DELETE("/api/users/{id}") suspend fun deleteUser(@Path("id") id: Int): Response<Unit>
}

object ApiClient {
    // Emulator so we can host the localhost
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val api: ApiService by lazy {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(log)
            .addInterceptor(AuthInterceptor())
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL) // must end with /
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}