package com.nasikhunamin.storyapp.view.maps

import androidx.lifecycle.ViewModel
import com.nasikhunamin.storyapp.data.repository.StoryRepository

class MapsViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStoryWithMaps() = repository.getStoryWithMaps()
}