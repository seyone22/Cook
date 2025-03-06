package com.seyone22.cook.helper

import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class AuthRequest(val username: String, val password: String)
@Serializable
data class AuthResponse(val accessToken: String, val refreshToken: String)

interface BlueskyAuthApi {
    @POST("https://bsky.social/auth/login")
    fun login(@Body request: AuthRequest): Call<AuthResponse>

    @POST("https://bsky.social/auth/register")
    fun register(@Body request: AuthRequest): Call<AuthResponse>

    @POST("https://bsky.social/auth/reset-password")
    fun resetPassword(@Body request: AuthRequest): Call<Void>
}
