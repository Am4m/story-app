package com.nasikhunamin.storyapp.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasikhunamin.storyapp.data.repository.UserRepository
import com.nasikhunamin.storyapp.data.pref.UserModel
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.data.response.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _loginResult = MediatorLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    fun login(email: String, password: String) {
        val liveData = repository.login(email, password)
        _loginResult.addSource(liveData) { result ->
            _loginResult.value = result
            if (result !is Result.Loading) {
                _loginResult.removeSource(liveData)
            }
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }
}