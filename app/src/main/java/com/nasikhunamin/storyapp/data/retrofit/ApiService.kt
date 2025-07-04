package com.nasikhunamin.storyapp.data.retrofit

import com.nasikhunamin.storyapp.data.response.ErrorResponse
import com.nasikhunamin.storyapp.data.response.LoginResponse
import com.nasikhunamin.storyapp.data.response.StoryAllResponse
import com.nasikhunamin.storyapp.data.response.StoryDetailResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): ErrorResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String
    ): StoryAllResponse

    @GET("stories/{id}")
    suspend fun getDetailStoryById(
        @Header("Authorization") token: String,
        @Path("id") storyId: String
    ): StoryDetailResponse

    @Multipart
    @POST("stories")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): ErrorResponse
}