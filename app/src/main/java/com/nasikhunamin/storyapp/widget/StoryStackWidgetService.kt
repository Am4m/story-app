package com.nasikhunamin.storyapp.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.nasikhunamin.storyapp.data.pref.UserPreference
import com.nasikhunamin.storyapp.data.pref.dataStore
import com.nasikhunamin.storyapp.data.retrofit.ApiConfig

class StoryWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val userPreference = UserPreference.getInstance(applicationContext.dataStore)
        val apiService = ApiConfig.getApiService()
        return StoryStackRemoteViewsFactory(this.applicationContext, userPreference, apiService)
    }
}
