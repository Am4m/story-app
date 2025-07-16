package com.nasikhunamin.storyapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nasikhunamin.storyapp.data.entity.StoryEntity
import com.nasikhunamin.storyapp.data.repository.UserRepository
import com.nasikhunamin.storyapp.data.pref.UserModel
import com.nasikhunamin.storyapp.data.repository.StoryRepository
import kotlinx.coroutines.launch

class MainViewModel(private val userRepository: UserRepository, storyRepository: StoryRepository) : ViewModel(){
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
    val story: LiveData<PagingData<StoryEntity>> = storyRepository.getAllStory().cachedIn(viewModelScope)

    fun refreshData() {
        refresh.value = Unit
    }
}