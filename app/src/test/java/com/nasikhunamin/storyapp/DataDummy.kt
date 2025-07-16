package com.nasikhunamin.storyapp

import com.nasikhunamin.storyapp.data.entity.StoryEntity

object DataDummy {

    fun generateDummyStoryResponse(): List<StoryEntity> {
        val items: MutableList<StoryEntity> = arrayListOf()
        for (i in 0..100) {
            val story = StoryEntity(
                i.toString(),
                "Name $i",
                "https://story-api.dicoding.dev/images/stories/photos-$i-.png",
                "2025-07-14T07:0$i:02.00Z",
                "Description $i",
                "0.456$i".toDouble(),
                "201.44$i".toDouble()
            )
            items.add(story)
        }
        return items
    }
}