package com.nasikhunamin.storyapp.view.addstory

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.data.repository.StoryRepository
import com.nasikhunamin.storyapp.data.response.ErrorResponse
import com.nasikhunamin.storyapp.getOrAwaitValue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.io.File

class AddStoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var storyRepository: StoryRepository
    private lateinit var addStoryViewModel: AddStoryViewModel
    private lateinit var imageFile: File

    @Before
    fun setUp() {
        storyRepository = mock(StoryRepository::class.java)
        addStoryViewModel = AddStoryViewModel(storyRepository)
        imageFile = File.createTempFile("add_story_test", ".jpg")
    }

    @After
    fun tearDown() {
        imageFile.delete()
    }

    @Test
    fun uploadImageWithoutLocationShouldReturnSuccess() {
        val expectedResponse = ErrorResponse(
            error = false,
            message = "Story created successfully"
        )
        `when`(
            storyRepository.uploadImage(
                imageFile,
                "Deskripsi test",
                null,
                null
            )
        ).thenReturn(createLiveData(Result.Success(expectedResponse)))

        val actualResult = addStoryViewModel.uploadImage(
            imageFile,
            "Deskripsi test",
            null,
            null
        ).getOrAwaitValue()

        assertTrue(actualResult is Result.Success)
        assertEquals(
            "Story created successfully",
            (actualResult as Result.Success).data.message
        )
        verify(storyRepository).uploadImage(imageFile, "Deskripsi test", null, null)
    }

    @Test
    fun uploadImageWithLocationShouldReturnSuccessAndPassCoordinates() {
        val latitude = -6.2000
        val longitude = 106.8167
        val expectedResponse = ErrorResponse(
            error = false,
            message = "Story created successfully"
        )
        `when`(
            storyRepository.uploadImage(
                imageFile,
                "Cerita dengan lokasi",
                latitude,
                longitude
            )
        ).thenReturn(createLiveData(Result.Success(expectedResponse)))

        val actualResult = addStoryViewModel.uploadImage(
            imageFile,
            "Cerita dengan lokasi",
            latitude,
            longitude
        ).getOrAwaitValue()

        assertTrue(actualResult is Result.Success)
        val successResult = actualResult as Result.Success
        assertEquals(false, successResult.data.error)
        assertEquals("Story created successfully", successResult.data.message)
        verify(storyRepository).uploadImage(
            imageFile,
            "Cerita dengan lokasi",
            latitude,
            longitude
        )
    }

    @Test
    fun uploadImageReturnsErrorShouldReturnErrorMessage() {
        `when`(
            storyRepository.uploadImage(
                imageFile,
                "Deskripsi test",
                null,
                null
            )
        ).thenReturn(createLiveData(Result.Error("Unauthorized")))

        val actualResult = addStoryViewModel.uploadImage(
            imageFile,
            "Deskripsi test",
            null,
            null
        ).getOrAwaitValue()

        assertTrue(actualResult is Result.Error)
        assertEquals("Unauthorized", (actualResult as Result.Error).error)
        verify(storyRepository).uploadImage(imageFile, "Deskripsi test", null, null)
    }

    @Test
    fun uploadImageWithLatitudeOnlyShouldPassLatitudeAndNullLongitude() {
        val latitude = -6.2000
        val expectedResponse = ErrorResponse(
            error = false,
            message = "Story created successfully"
        )
        `when`(
            storyRepository.uploadImage(
                imageFile,
                "Cerita dengan latitude saja",
                latitude,
                null
            )
        ).thenReturn(createLiveData(Result.Success(expectedResponse)))

        val actualResult = addStoryViewModel.uploadImage(
            imageFile,
            "Cerita dengan latitude saja",
            latitude,
            null
        ).getOrAwaitValue()

        assertTrue(actualResult is Result.Success)
        verify(storyRepository).uploadImage(
            imageFile,
            "Cerita dengan latitude saja",
            latitude,
            null
        )
    }

    @Test
    fun uploadImageWithLongitudeOnlyShouldPassNullLatitudeAndLongitudeValue() {
        val longitude = 106.8167
        val expectedResponse = ErrorResponse(
            error = false,
            message = "Story created successfully"
        )
        `when`(
            storyRepository.uploadImage(
                imageFile,
                "Cerita dengan longitude saja",
                null,
                longitude
            )
        ).thenReturn(createLiveData(Result.Success(expectedResponse)))

        val actualResult = addStoryViewModel.uploadImage(
            imageFile,
            "Cerita dengan longitude saja",
            null,
            longitude
        ).getOrAwaitValue()

        assertTrue(actualResult is Result.Success)
        verify(storyRepository).uploadImage(
            imageFile,
            "Cerita dengan longitude saja",
            null,
            longitude
        )
    }

    @Test
    fun uploadImageShouldEmitLoadingBeforeSuccess() {
        val source = MutableLiveData<Result<ErrorResponse>>()
        val expectedResponse = ErrorResponse(
            error = false,
            message = "Story created successfully"
        )
        `when`(
            storyRepository.uploadImage(
                imageFile,
                "Deskripsi test",
                null,
                null
            )
        ).thenReturn(source)

        val states = mutableListOf<Result<ErrorResponse>>()
        val observer = Observer<Result<ErrorResponse>> { states.add(it) }
        val resultLiveData = addStoryViewModel.uploadImage(
            imageFile,
            "Deskripsi test",
            null,
            null
        )
        resultLiveData.observeForever(observer)

        try {
            source.value = Result.Loading
            source.value = Result.Success(expectedResponse)

            assertEquals(2, states.size)
            assertTrue(states[0] is Result.Loading)
            assertTrue(states[1] is Result.Success)
            assertEquals(
                "Story created successfully",
                (states[1] as Result.Success).data.message
            )
        } finally {
            resultLiveData.removeObserver(observer)
        }
    }

    @Test
    fun uploadImageShouldEmitLoadingBeforeError() {
        val source = MutableLiveData<Result<ErrorResponse>>()
        `when`(
            storyRepository.uploadImage(
                imageFile,
                "Deskripsi test",
                null,
                null
            )
        ).thenReturn(source)

        val states = mutableListOf<Result<ErrorResponse>>()
        val observer = Observer<Result<ErrorResponse>> { states.add(it) }
        val resultLiveData = addStoryViewModel.uploadImage(
            imageFile,
            "Deskripsi test",
            null,
            null
        )
        resultLiveData.observeForever(observer)

        try {
            source.value = Result.Loading
            source.value = Result.Error("Network error")

            assertEquals(2, states.size)
            assertTrue(states[0] is Result.Loading)
            assertTrue(states[1] is Result.Error)
            assertEquals("Network error", (states[1] as Result.Error).error)
        } finally {
            resultLiveData.removeObserver(observer)
        }
    }

    @Test
    fun uploadImageWithEmptyDescriptionShouldStillDelegateToRepository() {
        val expectedResponse = ErrorResponse(
            error = false,
            message = "Story created successfully"
        )
        `when`(
            storyRepository.uploadImage(
                imageFile,
                "",
                null,
                null
            )
        ).thenReturn(createLiveData(Result.Success(expectedResponse)))

        val actualResult = addStoryViewModel.uploadImage(
            imageFile,
            "",
            null,
            null
        ).getOrAwaitValue()

        assertTrue(actualResult is Result.Success)
        verify(storyRepository).uploadImage(imageFile, "", null, null)
    }

    @Test
    fun uploadImageCalledShouldReturnRepositoryLiveDataInstance() {
        val expectedLiveData = createLiveData(Result.Loading)
        `when`(
            storyRepository.uploadImage(
                imageFile,
                "Deskripsi test",
                null,
                null
            )
        ).thenReturn(expectedLiveData)

        val actualLiveData = addStoryViewModel.uploadImage(
            imageFile,
            "Deskripsi test",
            null,
            null
        )

        assertSame(expectedLiveData, actualLiveData)
    }

    private fun createLiveData(result: Result<ErrorResponse>): MutableLiveData<Result<ErrorResponse>> {
        return MutableLiveData<Result<ErrorResponse>>().apply {
            value = result
        }
    }
}