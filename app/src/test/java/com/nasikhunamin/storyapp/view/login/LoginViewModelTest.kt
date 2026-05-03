package com.nasikhunamin.storyapp.view.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nasikhunamin.storyapp.MainDispatcherRule
import com.nasikhunamin.storyapp.data.pref.UserModel
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.data.repository.UserRepository
import com.nasikhunamin.storyapp.data.response.LoginResponse
import com.nasikhunamin.storyapp.data.response.LoginResult
import com.nasikhunamin.storyapp.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userRepository: UserRepository
    private lateinit var loginViewModel: LoginViewModel

    @Before
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        loginViewModel = LoginViewModel(userRepository)
    }

    @Test
    fun loginWithValidCredentialsShouldReturnSuccessWithToken() {
        val email = "testerstory@email.com"
        val password = "testing123"
        val expectedResponse = LoginResponse(
            loginResult = LoginResult(
                userId = "user-1",
                name = "Tester",
                token = "token-abc"
            ),
            error = false,
            message = "success"
        )
        `when`(userRepository.login(email, password))
            .thenReturn(createLiveData(Result.Success(expectedResponse)))

        val actualResult = loginViewModel.loginResult.getOrAwaitValue {
            loginViewModel.login(email, password)
        }

        assertTrue(actualResult is Result.Success)
        assertEquals("token-abc", (actualResult as Result.Success).data.loginResult?.token)
        verify(userRepository).login(email, password)
    }

    @Test
    fun loginWithWrongPasswordShouldReturnError() {
        val email = "testerstory@email.com"
        val password = "passwordsalah"
        `when`(userRepository.login(email, password))
            .thenReturn(createLiveData(Result.Error("Invalid password")))

        val actualResult = loginViewModel.loginResult.getOrAwaitValue {
            loginViewModel.login(email, password)
        }

        assertTrue(actualResult is Result.Error)
        assertEquals("Invalid password", (actualResult as Result.Error).error)
        verify(userRepository).login(email, password)
    }

    @Test
    fun loginWithUnregisteredEmailShouldReturnError() {
        val email = "tidakterdaftar@email.com"
        val password = "somepassword"
        `when`(userRepository.login(email, password))
            .thenReturn(createLiveData(Result.Error("User not found")))

        val actualResult = loginViewModel.loginResult.getOrAwaitValue {
            loginViewModel.login(email, password)
        }

        assertTrue(actualResult is Result.Error)
        assertEquals("User not found", (actualResult as Result.Error).error)
        verify(userRepository).login(email, password)
    }

    @Test
    fun repositoryReturnsNetworkErrorShouldReturnError() {
        val email = "testerstory@email.com"
        val password = "testing123"
        `when`(userRepository.login(email, password))
            .thenReturn(createLiveData(Result.Error("No internet connection")))

        val actualResult = loginViewModel.loginResult.getOrAwaitValue {
            loginViewModel.login(email, password)
        }

        assertTrue(actualResult is Result.Error)
        assertEquals("No internet connection", (actualResult as Result.Error).error)
    }

    @Test
    fun loginCalledShouldEmitLoadingBeforeFinalResult() {
        val email = "testerstory@email.com"
        val password = "testing123"
        val source = MutableLiveData<Result<LoginResponse>>()
        val expectedResponse = LoginResponse(
            loginResult = LoginResult(token = "token-abc"),
            error = false,
            message = "success"
        )
        `when`(userRepository.login(email, password)).thenReturn(source)

        val states = mutableListOf<Result<LoginResponse>>()
        val observer = Observer<Result<LoginResponse>> { states.add(it) }
        loginViewModel.loginResult.observeForever(observer)

        try {
            loginViewModel.login(email, password)
            source.value = Result.Loading
            source.value = Result.Success(expectedResponse)

            assertEquals(2, states.size)
            assertTrue(states[0] is Result.Loading)
            assertTrue(states[1] is Result.Success)
            assertEquals("token-abc", (states[1] as Result.Success).data.loginResult?.token)
        } finally {
            loginViewModel.loginResult.removeObserver(observer)
        }
    }

    @Test
    fun loginCalledShouldEmitLoadingBeforeErrorResult() {
        val email = "testerstory@email.com"
        val password = "wrong-password"
        val source = MutableLiveData<Result<LoginResponse>>()
        `when`(userRepository.login(email, password)).thenReturn(source)

        val states = mutableListOf<Result<LoginResponse>>()
        val observer = Observer<Result<LoginResponse>> { states.add(it) }
        loginViewModel.loginResult.observeForever(observer)

        try {
            loginViewModel.login(email, password)
            source.value = Result.Loading
            source.value = Result.Error("Invalid password")

            assertEquals(2, states.size)
            assertTrue(states[0] is Result.Loading)
            assertTrue(states[1] is Result.Error)
            assertEquals("Invalid password", (states[1] as Result.Error).error)
        } finally {
            loginViewModel.loginResult.removeObserver(observer)
        }
    }

    @Test
    fun loginSourceEmitsFinalResultShouldIgnoreLaterSourceChanges() {
        val email = "testerstory@email.com"
        val password = "testing123"
        val source = MutableLiveData<Result<LoginResponse>>()
        val expectedResponse = LoginResponse(
            loginResult = LoginResult(token = "token-abc"),
            error = false,
            message = "success"
        )
        `when`(userRepository.login(email, password)).thenReturn(source)

        val states = mutableListOf<Result<LoginResponse>>()
        val observer = Observer<Result<LoginResponse>> { states.add(it) }
        loginViewModel.loginResult.observeForever(observer)

        try {
            loginViewModel.login(email, password)
            source.value = Result.Success(expectedResponse)
            source.value = Result.Error("Late error")

            assertEquals(1, states.size)
            assertTrue(states[0] is Result.Success)
            assertEquals("token-abc", (states[0] as Result.Success).data.loginResult?.token)
        } finally {
            loginViewModel.loginResult.removeObserver(observer)
        }
    }

    @Test
    fun loginSuccessResponseWithEmptyLoginResultShouldStillReturnSuccess() {
        val email = "testerstory@email.com"
        val password = "testing123"
        val expectedResponse = LoginResponse(
            loginResult = null,
            error = false,
            message = "success"
        )
        `when`(userRepository.login(email, password))
            .thenReturn(createLiveData(Result.Success(expectedResponse)))

        val actualResult = loginViewModel.loginResult.getOrAwaitValue {
            loginViewModel.login(email, password)
        }

        assertTrue(actualResult is Result.Success)
        assertNull((actualResult as Result.Success).data.loginResult)
    }

    @Test
    fun saveSessionCalledShouldCallRepositorySaveSession() = runTest {
        val user = UserModel(
            email = "testerstory@email.com",
            token = "token-abc",
            isLogin = true
        )

        loginViewModel.saveSession(user)
        verify(userRepository).saveSession(user)
    }

    private fun <T> createLiveData(result: Result<T>): MutableLiveData<Result<T>> {
        return MutableLiveData<Result<T>>().apply {
            value = result
        }
    }
}