package com.nasikhunamin.storyapp.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.data.repository.UserRepository
import com.nasikhunamin.storyapp.data.response.ErrorResponse

class SignUpViewModel(private val repository: UserRepository) : ViewModel() {
    private val _signupResult = MediatorLiveData<Result<ErrorResponse>>()
    val signupResult: LiveData<Result<ErrorResponse>> = _signupResult

    fun register(name: String, email: String, password: String) {
        val liveData = repository.register(name, email, password)
        _signupResult.addSource(liveData) { result ->
            _signupResult.value = result
            if (result !is Result.Loading) {
                _signupResult.removeSource(liveData)
            }
        }
    }
}