package com.nasikhunamin.storyapp.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.nasikhunamin.storyapp.data.pref.UserModel
import com.nasikhunamin.storyapp.data.pref.UserPreference
import com.nasikhunamin.storyapp.data.response.ErrorResponse
import com.nasikhunamin.storyapp.data.response.LoginResponse
import com.nasikhunamin.storyapp.data.retrofit.ApiService
import com.nasikhunamin.storyapp.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
){
    fun register(name: String, email: String, password: String): LiveData<Result<ErrorResponse>> =
        liveData{
            emit(Result.Loading)
           try {
                val response = apiService.register(name, email, password)
                emit(Result.Success(response))
            }catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                Log.e(USER_REPOSITORY, "$REGISTRATION_FAILED ${errorBody.message}")
                emit(Result.Error(errorBody.message.toString()))
            }
        }

    fun login(email: String, password: String) : LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        wrapEspressoIdlingResource {
            try {
                val response = apiService.login(email, password)
                userPreference.saveSession(
                    UserModel(
                        email = email,
                        token = response.loginResult?.token.toString(),
                        isLogin = true
                    )
                )
                emit(Result.Success(response))
            }catch (e: HttpException){
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                Log.e(USER_REPOSITORY, "$LOGIN_FAILED ${errorBody.message}")
                emit(Result.Error(errorBody.message.toString()))
            }
        }
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

     fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    companion object {
        const val USER_REPOSITORY = "UserRepository"
        const val REGISTRATION_FAILED = "Registration failed. Error:"
        const val LOGIN_FAILED = "Login failed. Error:"
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference,
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreference)
            }.also { instance = it }
    }
}


















