package com.nasikhunamin.storyapp.view.login

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.nasikhunamin.storyapp.JsonConverter
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.data.retrofit.ApiConfig
import com.nasikhunamin.storyapp.utils.EspressoIdlingResource
import okhttp3.mockwebserver.MockResponse


@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        ActivityScenario.launch(LoginActivity::class.java)
        mockWebServer.start(8080)
        ApiConfig.base_url = "http://127.0.0.1:8080/"
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun login_Success() {

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("login_success_response.json"))
        mockWebServer.enqueue(mockResponse)

        Intents.init()

        onView(withId(R.id.ed_login_email))
            .perform(typeText("masbro2@gmail.com"), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .perform(typeText("12345678"), closeSoftKeyboard())

        onView(withId(R.id.loginButton))
            .perform(click())

        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

        onView(withText(R.string.log_out)).perform(click())
    }
}

