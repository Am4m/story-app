package com.nasikhunamin.storyapp.view.main

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.data.pref.UserModel
import com.nasikhunamin.storyapp.data.pref.UserPreference
import com.nasikhunamin.storyapp.data.pref.dataStore
import com.nasikhunamin.storyapp.data.retrofit.ApiConfig
import com.nasikhunamin.storyapp.utils.EspressoIdlingResource
import com.nasikhunamin.storyapp.view.detail.DetailActivity
import com.nasikhunamin.storyapp.view.welcome.WelcomeActivity
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start(8080)
        ApiConfig.base_url = "http://127.0.0.1:8080/"

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val userPreference = UserPreference.getInstance(context.dataStore)
        runBlocking {
            userPreference.saveSession(UserModel("test@gmail.com", "token", true))
        }

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        Intents.init()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val userPreference = UserPreference.getInstance(context.dataStore)
        runBlocking {
            userPreference.logout()
        }
    }

    @Test
    fun story_list_displays() {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"error":false,"message":"success","listStory":[{"id":"1","name":"User 1","description":"Story 1","photoUrl":"https://story-api.dicoding.dev/images/stories/photos-1.jpg","createdAt":"2023-01-01T00:00:00Z","lat":-6.2,"lon":106.8}]}""")
        mockWebServer.enqueue(mockResponse)

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_story_feed))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun clicking_story_item_opens_detail() {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"error":false,"message":"success","listStory":[{"id":"1","name":"User 1","description":"Story 1","photoUrl":"https://story-api.dicoding.dev/images/stories/photos-1.jpg","createdAt":"2023-01-01T00:00:00Z","lat":-6.2,"lon":106.8}]}""")
        mockWebServer.enqueue(mockResponse)

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_story_feed))
                .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(0, click()))

            intended(hasComponent(DetailActivity::class.java.name))

            onView(withId(R.id.iv_hero_photo))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun logout_redirects_to_welcome() {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"error":false,"message":"Stories fetched successfully","listStory":[]}""")
        mockWebServer.enqueue(mockResponse)

        ActivityScenario.launch(MainActivity::class.java).use {
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)

            onView(withText(R.string.log_out)).perform(click())

            intended(hasComponent(WelcomeActivity::class.java.name))
        }
    }
}
