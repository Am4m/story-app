package com.nasikhunamin.storyapp.view.addstory

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.provider.MediaStore
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiSelector
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nasikhunamin.storyapp.JsonConverter
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.data.retrofit.ApiConfig
import com.nasikhunamin.storyapp.utils.EspressoIdlingResource
import com.nasikhunamin.storyapp.view.main.MainActivity
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class AddStoryActivityTest {

    private val mockWebServer = MockWebServer()
    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        mockWebServer.start(8080)
        ApiConfig.base_url = "http://127.0.0.1:8080/"
        Intents.init()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        Intents.release()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun addStory_Success_fromGallery() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val testFile = createTestImageFile(appContext)
        val testUri = FileProvider.getUriForFile(
            appContext,
            "com.nasikhunamin.storyapp.fileprovider",
            testFile
        )

        val resultData = Intent().setData(testUri)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        intending(hasAction(MediaStore.ACTION_PICK_IMAGES)).respondWith(result)

        val scenario = ActivityScenario.launch(AddStoryActivity::class.java)
        scenario.onActivity { activity ->
            activity.grantUriPermission(
                activity.packageName,
                testUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        onView(withId(R.id.galleryButton)).perform(click())

        onView(withId(R.id.previewImageView)).check(matches(isDisplayed()))

        onView(withId(R.id.ed_add_description)).perform(
            typeText("Deskripsi tes dari galeri"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.locationCheckBox)).perform(click())
        onView(withId(R.id.locationButton)).perform(click())

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val permissionButtonText = context.getString(R.string.maps_permission)
        val allowButton = device.findObject(UiSelector().text(permissionButtonText))
        if (allowButton.waitForExists(5000)) {
            allowButton.click()
        }

        onView(withId(R.id.map)).perform(click())

        onView(withId(R.id.action_succes)).perform(click())

        val expectedApiResponse = MockResponse()
            .setResponseCode(201)
            .setBody(JsonConverter.readStringFromFile("add_story_success_response.json"))
        mockWebServer.enqueue(expectedApiResponse)

        onView(withId(R.id.uploadButton)).perform(click())
        intended(hasComponent(MainActivity::class.java.name))
    }

    private fun createTestImageFile(context: Context): File {
        val file = File(context.cacheDir, "test_image.jpg")
        file.createNewFile()

        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLUE)

        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()

        return file
    }
}