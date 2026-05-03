package com.nasikhunamin.storyapp.view.addstory

import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.provider.MediaStore
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
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nasikhunamin.storyapp.JsonConverter
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.ViewModelFactory
import com.nasikhunamin.storyapp.data.retrofit.ApiService
import com.nasikhunamin.storyapp.utils.EspressoIdlingResource
import com.nasikhunamin.storyapp.view.addstory.maps.MapsPickedLocationActivity
import com.nasikhunamin.storyapp.view.main.MainActivity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matcher
import org.hamcrest.Matchers.anyOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Field

@RunWith(AndroidJUnit4::class)
class AddStoryActivityTest {

    private val mockWebServer = MockWebServer()
    private lateinit var testApiService: ApiService
    private var originalViewModelFactoryInstance: ViewModelFactory? = null
    private lateinit var viewModelFactoryInstanceField: Field

    @Before
    fun setUp() {
        mockWebServer.start(8080)

        val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
        testApiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)

        viewModelFactoryInstanceField = ViewModelFactory::class.java.getDeclaredField("INSTANCE")
        viewModelFactoryInstanceField.isAccessible = true
        originalViewModelFactoryInstance = viewModelFactoryInstanceField.get(null) as? ViewModelFactory

        viewModelFactoryInstanceField.set(null, null)

        val testFactory = ViewModelFactory.getInstance(ApplicationProvider.getApplicationContext(), testApiService)
        viewModelFactoryInstanceField.set(null, testFactory)

        Intents.init()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        Intents.release()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)

        if (::viewModelFactoryInstanceField.isInitialized) {
            viewModelFactoryInstanceField.set(null, originalViewModelFactoryInstance)
        }
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

        val resultData = Intent().apply {
            data = testUri
            clipData = ClipData.newUri(appContext.contentResolver, "test_image", testUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        intending(galleryPickerIntentMatcher()).respondWith(result)

        val locationResultData = Intent().apply {
            putExtra(MapsPickedLocationActivity.EXTRA_LATITUDE, -6.2000)
            putExtra(MapsPickedLocationActivity.EXTRA_LONGITUDE, 106.8167)
        }
        intending(hasComponent(MapsPickedLocationActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, locationResultData))

        val scenario = ActivityScenario.launch(AddStoryActivity::class.java)
        delayForPreview()

        scenario.onActivity { activity ->
            activity.grantUriPermission(
                activity.packageName,
                testUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        onView(withId(R.id.galleryButton)).perform(click())
        delayForPreview()

        onView(withId(R.id.previewImageView)).check(matches(isDisplayed()))
        delayForPreview()

        onView(withId(R.id.ed_add_description)).perform(
            typeText("Deskripsi tes dari galeri"),
            closeSoftKeyboard()
        )
        delayForPreview()

        onView(withId(R.id.locationCheckBox)).perform(click())
        delayForPreview()

        onView(withId(R.id.locationButton)).perform(click())
        delayForPreview()

        onView(withId(R.id.tv_latitude_value))
            .check(matches(withText("Lat: -6.2000, Lon: 106.8167")))
        delayForPreview()

        val expectedApiResponse = MockResponse()
            .setResponseCode(201)
            .setBody(JsonConverter.readStringFromFile("add_story_success_response.json"))
        mockWebServer.enqueue(expectedApiResponse)

        onView(withId(R.id.uploadButton)).perform(click())
        delayForPreview()

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

    private fun galleryPickerIntentMatcher(): Matcher<Intent> = anyOf(
        hasAction(MediaStore.ACTION_PICK_IMAGES),
        hasAction(ACTION_SYSTEM_FALLBACK_PICK_IMAGES),
        hasAction(Intent.ACTION_OPEN_DOCUMENT),
        hasAction(Intent.ACTION_GET_CONTENT),
        hasAction(Intent.ACTION_PICK)
    )

    private fun delayForPreview() {
        Thread.sleep(TEST_STEP_DELAY_MS)
    }

    private companion object {
        const val TEST_STEP_DELAY_MS = 1_000L
        const val ACTION_SYSTEM_FALLBACK_PICK_IMAGES =
            "androidx.activity.result.contract.action.PICK_IMAGES"
    }
}
