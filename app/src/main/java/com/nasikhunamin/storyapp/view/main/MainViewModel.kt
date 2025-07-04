package com.nasikhunamin.storyapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.nasikhunamin.storyapp.data.repository.UserRepository
import com.nasikhunamin.storyapp.data.pref.UserModel
import com.nasikhunamin.storyapp.data.repository.StoryRepository
import kotlinx.coroutines.launch
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.data.response.StoryAllResponse

class MainViewModel(private val userRepository: UserRepository, private val storyRepository: StoryRepository) : ViewModel(){
    private val refresh = MutableLiveData<Unit>()
    init {
        refreshData()
    }

    fun  getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    fun getStories() : LiveData<Result<StoryAllResponse>>{
        return storyRepository.getStories()
    }

    fun refreshData() {
        refresh.value = Unit
    }
}