package com.nasikhunamin.storyapp.view.signup

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.data.retrofit.ApiConfig
import com.nasikhunamin.storyapp.utils.EspressoIdlingResource
import com.nasikhunamin.storyapp.view.login.LoginActivity
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class SignupActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SignupActivity::class.java)

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start(8080)
        ApiConfig.base_url = "http://127.0.0.1:8080/"
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        Intents.init()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }

    @Test
    fun register_with_valid_data_succeeds() {
        val mockResponse = MockResponse()
            .setResponseCode(201)
            .setBody("""{"error":false,"message":"User Created"}""")
        mockWebServer.enqueue(mockResponse)

        val uniqueEmail = "testuser${System.currentTimeMillis()}@example.com"

        onView(withId(R.id.ed_register_name))
            .perform(clearText(), typeText("Test User"), closeSoftKeyboard())

        onView(withId(R.id.ed_register_email))
            .perform(clearText(), typeText(uniqueEmail), closeSoftKeyboard())

        onView(withId(R.id.ed_register_password))
            .perform(clearText(), typeText("Password8"), closeSoftKeyboard())

        onView(withId(R.id.signupButton)).perform(click())

        // On success, should navigate back to Login
        intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun password_under_8_chars_shows_realtime_custom_view_error() {
        onView(withId(R.id.ed_register_password))
            .perform(clearText(), typeText("Test123"), closeSoftKeyboard())

        onView(withId(R.id.ed_register_password))
            .check(matches(hasErrorText(containsString("8"))))
    }

    private fun containsString(substring: String) = org.hamcrest.Matchers.containsString(substring)
}
