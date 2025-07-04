package com.nasikhunamin.storyapp.view.main

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.ViewModelFactory
import com.nasikhunamin.storyapp.data.retrofit.ApiConfig
import com.nasikhunamin.storyapp.databinding.ActivityMainBinding
import com.nasikhunamin.storyapp.view.welcome.WelcomeActivity
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.view.addstory.AddStoryActivity
import com.nasikhunamin.storyapp.widget.StoryAppWidget

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this, ApiConfig.getApiService())
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.list_story)

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        binding.fab.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddStoryActivity::class.java))
        }

        mainAdapter = MainAdapter()
        setUpRecyclerView()
        setupObservers()
        setupView()
        viewModel.getStories()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.localization -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.action_logout -> {
                val appWidgetManager = AppWidgetManager.getInstance(this)
                val componentName = ComponentName(this, StoryAppWidget::class.java)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                Log.d(LOGIN_LOG, "$MESSAGE_LOG_LOGIN ${ids.joinToString()}")
                appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view)
                val updateIntent = Intent(this, StoryAppWidget::class.java)
                updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                sendBroadcast(updateIntent)
                viewModel.logout()
                Log.d(MAIN_LOG, MAIN_MESSAGE)
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
                true

            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun setUpRecyclerView() {

       binding.recyclerView.apply {
           layoutManager = LinearLayoutManager(this@MainActivity)
           setHasFixedSize(true)
           adapter = mainAdapter
       }
    }

    private fun setupObservers() {
            viewModel.getStories().observe(this) { result ->
                when (result) {
                    is Result.Loading -> {
                        showLoading(true)
                        binding.recyclerView.visibility = View.GONE
                    }
                    is Result.Success -> {
                        showLoading(false)
                        binding.recyclerView.visibility = View.VISIBLE
                        mainAdapter.submitList(result.data.listStory)
                    }
                    is Result.Error -> {
                        showLoading(false)
                        binding.recyclerView.visibility = View.GONE
                    }
                }
            }

    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object{
        const val LOGIN_LOG = "LoginActivity"
        const val MAIN_LOG = "MainActivity"
        const val MESSAGE_LOG_LOGIN = "Widget IDs:"
        const val MAIN_MESSAGE = "LogOut"
    }

}

