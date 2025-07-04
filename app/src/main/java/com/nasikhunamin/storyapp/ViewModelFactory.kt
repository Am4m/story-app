package com.nasikhunamin.storyapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nasikhunamin.storyapp.data.repository.UserRepository
import com.nasikhunamin.storyapp.data.retrofit.ApiService
import com.nasikhunamin.storyapp.di.Injection
import com.nasikhunamin.storyapp.view.login.LoginViewModel
import com.nasikhunamin.storyapp.view.signup.SignUpViewModel

@Suppress("CAST_NEVER_SUCCEEDS")
class ViewModelFactory(
    private val userRepository: UserRepository,
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                SignUpViewModel(userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context, apiService: ApiService): ViewModelFactory {
            clearInstance()
            synchronized(ViewModelFactory::class.java) {
                if (INSTANCE == null) {
                    val userRepository = Injection.provideRepository(context, apiService)
                    INSTANCE = ViewModelFactory(userRepository)
                }
                return INSTANCE!!
            }
        }

        @JvmStatic
        private fun clearInstance() {
            INSTANCE = null
        }
    }
}
