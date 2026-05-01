package com.nasikhunamin.storyapp.view.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.data.entity.StoryEntity
import com.nasikhunamin.storyapp.databinding.ItemCardStoryBinding
import com.nasikhunamin.storyapp.view.detail.DetailActivity

class MainAdapter : PagingDataAdapter<StoryEntity, MainAdapter.MainViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val binding = ItemCardStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    class MainViewHolder(private val binding: ItemCardStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryEntity) {
            binding.tvUsername.text = story.name
            binding.tvItemName.text = story.name
            binding.tvItemDescription.text = story.description
            binding.tvTime.text = story.createdAt

            Glide.with(itemView.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_place_holder)
                .into(binding.imgItemPhoto)

            Glide.with(itemView.context)
                .load(story.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(binding.ivAvatar)

            itemView.setOnClickListener {
                val context = it.context
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.STORY_ID, story.id)
                }
                context.startActivity(intent)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(
                oldItem: StoryEntity,
                newItem: StoryEntity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: StoryEntity,
                newItem: StoryEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}