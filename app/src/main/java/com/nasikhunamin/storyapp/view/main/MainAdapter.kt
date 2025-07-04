package com.nasikhunamin.storyapp.view.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nasikhunamin.storyapp.data.response.ListStoryItem
import com.nasikhunamin.storyapp.databinding.ItemCardStoryBinding
import com.nasikhunamin.storyapp.view.detail.DetailActivity

class MainAdapter : ListAdapter<ListStoryItem, MainAdapter.MainViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val binding = ItemCardStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story)

        holder.itemView.setOnClickListener {
            val context = it.context
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra(DetailActivity.STORY_ID, story.id)
            }
            context.startActivity(intent)
        }
    }

    class MainViewHolder(private val binding: ItemCardStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            binding.tvItemName.text = story.name
            binding.tvItemDescription.text = story.description
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(binding.imgItemPhoto)
        }
    }

    companion object{
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}

