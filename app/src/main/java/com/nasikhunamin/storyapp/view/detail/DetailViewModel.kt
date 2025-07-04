package com.nasikhunamin.storyapp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.data.repository.StoryRepository
import com.nasikhunamin.storyapp.data.response.ListStoryItem

class DetailViewModel(private val storyRepository: StoryRepository) : ViewModel(){
    fun getStoryById(storyId: String) : LiveData<Result<ListStoryItem>> {
        return storyRepository.getStoryById(storyId)
    }
}