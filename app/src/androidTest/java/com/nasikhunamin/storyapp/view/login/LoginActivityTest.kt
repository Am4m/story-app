package com.nasikhunamin.storyapp.view.login

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
import com.nasikhunamin.storyapp.JsonConverter
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.data.retrofit.ApiConfig
import com.nasikhunamin.storyapp.utils.EspressoIdlingResource
import com.nasikhunamin.storyapp.view.main.MainActivity
import com.nasikhunamin.storyapp.view.signup.SignupActivity
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

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
    fun login_with_valid_credentials_navigates_to_main() {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("login_success_response.json"))
        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.ed_login_email))
            .perform(clearText(), typeText("test@example.com"), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .perform(clearText(), typeText("Test1234!"), closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(click())

        intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun login_with_empty_form_shows_validation_error() {
        onView(withId(R.id.loginButton)).perform(click())

        onView(withId(R.id.ed_login_email))
            .check(matches(isDisplayed()))

        onView(withId(R.id.loginButton)).check(matches(isDisplayed()))
    }

    @Test
    fun login_with_unregistered_email_shows_error() {
        val mockResponse = MockResponse()
            .setResponseCode(401)
            .setBody("""{"error":true,"message":"Unauthorized"}""")
        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.ed_login_email))
            .perform(clearText(), typeText("notexist@mail.com"), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .perform(clearText(), typeText("Test1234!"), closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(click())

        // LoginActivity still displayed
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()))
    }

    @Test
    fun password_less_than_8_chars_shows_realtime_error() {
        onView(withId(R.id.ed_login_password))
            .perform(clearText(), typeText("Pass1"), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .check(matches(hasErrorText(containsString("8"))))
    }

    @Test
    fun clicking_signup_link_navigates_to_signup() {
        onView(withId(R.id.tv_signup)).perform(click())

        intended(hasComponent(SignupActivity::class.java.name))
    }

    private fun containsString(substring: String) = org.hamcrest.Matchers.containsString(substring)
}
