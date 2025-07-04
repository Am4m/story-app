package com.nasikhunamin.storyapp.data.repository

import androidx.datastore.core.IOException
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.nasikhunamin.storyapp.data.pref.UserPreference
import com.nasikhunamin.storyapp.data.response.ErrorResponse
import com.nasikhunamin.storyapp.data.response.ListStoryItem
import com.nasikhunamin.storyapp.data.response.StoryAllResponse
import com.nasikhunamin.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference,
) {
    fun getStories(): LiveData<Result<StoryAllResponse>> = liveData {
        emit(Result.Loading)
        try {
            val token = userPreference.getSession().first().token
            val response = apiService.getStories("Bearer $token")
            print(response)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: e.message()
            emit(Result.Error(errorMessage))
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("Unexpected error: ${e.message}"))
        }
    }

    fun getStoryById(storyId: String): LiveData<Result<ListStoryItem>> = liveData {
        emit(Result.Loading)
        try {
            val token = userPreference.getSession().first().token
            val response = apiService.getDetailStoryById("Bearer $token", storyId)
            val result = response.story
            emit(Result.Success(result))
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: e.message()
            emit(Result.Error("Server error: $errorMessage"))
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("Unexpected error: ${e.message}"))
        }
    }

    fun uploadImage(imageFile: File, description: String) = liveData {
        emit(Result.Loading)
        val token = userPreference.getSession().first().token
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )
        try {
            val successResponse = apiService.uploadImage(
                "Bearer $token",
                multipartBody,
                requestBody
            )
            emit(Result.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(Result.Error(errorResponse.message.toString()))
        }
    }

    companion object {
        fun getInstance(apiService: ApiService, userPreference: UserPreference): StoryRepository {
            return StoryRepository(apiService, userPreference)
        }
    }
}
