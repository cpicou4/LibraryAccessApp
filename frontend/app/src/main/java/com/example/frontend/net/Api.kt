package com.example.frontend.net

import com.example.frontend.net.dto.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

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

    // Public
    @GET("api/books") suspend fun getBooks(): List<BookGetDto>
    @GET("api/borrowings/user/{userId}/active") suspend fun getActiveBorrowingsByUser(@Path("userId") userId: Int): List<BorrowingRecordGetDto>
    @POST("api/borrowings") suspend fun checkOut(@Body dto: BorrowingRecordCreateDto): BorrowingRecordGetDto
    @PUT("api/borrowings/{id}/return") suspend fun returnBook(@Path("id") id: Int, @Body dto: BorrowingRecordReturnDto): BorrowingRecordGetDto

    // Admin protected
    @GET("api/borrowings") suspend fun getBorrowings(): List<BorrowingRecordGetDto>
    @GET("api/users") suspend fun getUsers(): List<UserGetDto>
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