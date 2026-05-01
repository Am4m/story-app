package com.nasikhunamin.storyapp.view.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.ViewModelFactory
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.data.response.ListStoryItem
import com.nasikhunamin.storyapp.data.retrofit.ApiConfig
import com.nasikhunamin.storyapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel by viewModels<DetailViewModel> {
        ViewModelFactory.getInstance(this, ApiConfig.getApiService())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        val storyId = intent.getStringExtra(STORY_ID)
        if (storyId.isNullOrEmpty()) {
            Toast.makeText(this, MESSAGE_FAILED_GET_STORY, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        getDetailStory(storyId)
    }

    private fun getDetailStory(storyId: String) {
        viewModel.getStoryById(storyId).observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val item = result.data
                    showStoryDetail(item)
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "$MESSAGE_FAILED_LOAD ${result.error}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showStoryDetail(story: ListStoryItem) {
        binding.tvItemName.text = story.name
        binding.tvItemDescription.text = story.description
        binding.toolbarLayout.title = story.name

        Glide.with(this)
            .load(story.photoUrl)
            .placeholder(R.drawable.ic_place_holder)
            .into(binding.ivHeroPhoto)
    }

    companion object {
        const val STORY_ID = "STORY_ID"
        const val MESSAGE_FAILED_GET_STORY = "Story ID tidak ditemukan"
        const val MESSAGE_FAILED_LOAD = "Gagal memuat:"
    }
}