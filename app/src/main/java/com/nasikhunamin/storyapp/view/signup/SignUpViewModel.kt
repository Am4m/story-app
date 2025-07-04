package com.nasikhunamin.storyapp.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.data.repository.UserRepository
import com.nasikhunamin.storyapp.data.response.ErrorResponse

class SignUpViewModel(private val repository: UserRepository) : ViewModel() {
    fun register(name: String, email: String, password: String) : LiveData<Result<ErrorResponse>> {
        return  repository.register(name, email, password)
    }
}