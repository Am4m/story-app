package com.nasikhunamin.storyapp.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.data.pref.UserPreference
import com.nasikhunamin.storyapp.data.response.ListStoryItem
import com.nasikhunamin.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StoryStackRemoteViewsFactory(val context: Context, val userPreference: UserPreference, val apiService: ApiService): RemoteViewsService.RemoteViewsFactory{
    private val listData = arrayListOf<ListStoryItem>()
    private val storiesBitmap = arrayListOf<Bitmap>()

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        runBlocking {
            try {
                val token = userPreference.getSession().first().token
                val response = apiService.getStories("Bearer $token")
                val bitmap = response.listStory.map {
                    Glide.with(context)
                        .asBitmap()
                        .load(it.photoUrl)
                        .submit()
                        .get()
                }
                storiesBitmap.clear()
                listData.clear()
                storiesBitmap.addAll(bitmap)
                listData.addAll(response.listStory)
            }catch (e: Exception) {
                listData.clear()
            }
        }
    }

    override fun onDestroy() {
        listData.clear()
    }

    override fun getCount(): Int = listData.size

    override fun getViewAt(position: Int): RemoteViews? {
        val rv = RemoteViews(context.packageName, R.layout.widget_item).apply{
            setImageViewBitmap(R.id.imageView, storiesBitmap[position])
        }
        val extras = bundleOf(
            StoryAppWidget.EXTRA_ITEM to listData[position].id
        )
        val fillInIntent = Intent().apply {
            putExtras(extras)
        }

        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(p0: Int): Long = 0

    override fun hasStableIds(): Boolean = false
}