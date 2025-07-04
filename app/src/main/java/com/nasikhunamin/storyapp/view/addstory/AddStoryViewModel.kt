package com.nasikhunamin.storyapp.view.addstory

import androidx.lifecycle.ViewModel
import com.nasikhunamin.storyapp.data.repository.StoryRepository
import java.io.File

class AddStoryViewModel (private val repository: StoryRepository) : ViewModel(){
    fun uploadImage(file: File, description: String) = repository.uploadImage(file, description)
}