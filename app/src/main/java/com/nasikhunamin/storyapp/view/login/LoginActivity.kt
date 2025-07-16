package com.nasikhunamin.storyapp.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.ViewModelFactory
import com.nasikhunamin.storyapp.data.pref.UserModel
import com.nasikhunamin.storyapp.data.retrofit.ApiConfig
import com.nasikhunamin.storyapp.databinding.ActivityLoginBinding
import com.nasikhunamin.storyapp.view.main.MainActivity
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.widget.StoryAppWidget

class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this, ApiConfig.getApiService())
    }

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.languageButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            true
        }

        setupView()
        setupAction()
        playAnimation()
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
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()
                viewModel.login(email, password).observe(this) { result ->
                    when(result) {
                        is Result.Loading -> {
                            showLoading(true)
                            disableLoginButton()
                        }
                        is Result.Success -> {
                            showLoading(false)

                            enableLoginButton()
                            viewModel.saveSession(UserModel(email, result.data.loginResult?.token.toString()))
                            val appWidgetManager = AppWidgetManager.getInstance(this)
                            val componentName = ComponentName(this, StoryAppWidget::class.java)
                            val ids = appWidgetManager.getAppWidgetIds(componentName)
                            Log.d(LOGIN_LOG, "$MESSAGE_LOG ${ids.joinToString()}")
                            appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view)
                            val updateIntent = Intent(this, StoryAppWidget::class.java)
                            updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                            sendBroadcast(updateIntent)
                            navigateToMainActivity()
                        }
                        is Result.Error -> {
                            showLoading(false)
                            enableLoginButton()
                            showError(result.error)
                        }
                    }
                }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun disableLoginButton() {
        binding.loginButton.isEnabled = false
    }

    private fun enableLoginButton() {
        binding.loginButton.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.messageTextView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val animatorSet = AnimatorSet().apply {
            playSequentially(
                ObjectAnimator.ofFloat(binding.textView, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.emailTextViewLogin, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.edLoginEmail, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.passwordTextViewLogin, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.edLoginPassword, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)
            )
            startDelay = 100
        }
        animatorSet.start()
    }

    companion object{
        const val LOGIN_LOG = "LoginActivity"
        const val MESSAGE_LOG = "Widget IDs:"
    }
}