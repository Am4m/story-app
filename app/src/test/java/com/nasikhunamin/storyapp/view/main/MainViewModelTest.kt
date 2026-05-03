package com.nasikhunamin.storyapp.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.nasikhunamin.storyapp.DataDummy
import com.nasikhunamin.storyapp.MainDispatcherRule
import com.nasikhunamin.storyapp.data.entity.StoryEntity
import com.nasikhunamin.storyapp.data.pref.UserModel
import com.nasikhunamin.storyapp.data.repository.StoryRepository
import com.nasikhunamin.storyapp.data.repository.UserRepository
import com.nasikhunamin.storyapp.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Test
    fun getStoryShouldNotNullAndReturnData() = runTest {
        val dummyStory = DataDummy.generateDummyStoryResponse()
        val data: PagingData<StoryEntity> = StoryPagingSource.snapshot(dummyStory)
        val expectedQuote = MutableLiveData<PagingData<StoryEntity>>()
        expectedQuote.value = data
        `when`(storyRepository.getAllStory()).thenReturn(expectedQuote)

        val mainViewModel = MainViewModel(userRepository, storyRepository)
        val actualQuote: PagingData<StoryEntity> = mainViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MainAdapter.Companion.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStory.size, differ.snapshot().size)
        assertEquals(dummyStory[0], differ.snapshot()[0])
        assertEquals(dummyStory.last(), differ.snapshot()[dummyStory.lastIndex])
    }

    @Test
    fun getStoryEmptyShouldReturnNoData() = runTest {
        val data: PagingData<StoryEntity> = PagingData.from(emptyList())
        val expectedQuote = MutableLiveData<PagingData<StoryEntity>>()
        expectedQuote.value = data
        `when`(storyRepository.getAllStory()).thenReturn(expectedQuote)

        val mainViewModel = MainViewModel(userRepository, storyRepository)
        val actualQuote: PagingData<StoryEntity> = mainViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MainAdapter.Companion.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        assertEquals(0, differ.snapshot().size)
    }

    @Test
    fun viewModelCreatedShouldGetStoryFromRepository() {
        val expectedStory = MutableLiveData<PagingData<StoryEntity>>()
        expectedStory.value = PagingData.from(emptyList())
        `when`(storyRepository.getAllStory()).thenReturn(expectedStory)

        MainViewModel(userRepository, storyRepository)

        verify(storyRepository).getAllStory()
    }

    @Test
    fun getSessionShouldReturnUserSession() {
        val expectedStory = MutableLiveData<PagingData<StoryEntity>>()
        expectedStory.value = PagingData.from(emptyList())
        val expectedUser = UserModel(
            email = "testerstory@email.com",
            token = "token-abc",
            isLogin = true
        )
        `when`(storyRepository.getAllStory()).thenReturn(expectedStory)
        `when`(userRepository.getSession()).thenReturn(flowOf(expectedUser))

        val mainViewModel = MainViewModel(userRepository, storyRepository)
        val actualUser = mainViewModel.getSession().getOrAwaitValue()

        assertEquals(expectedUser, actualUser)
        verify(userRepository).getSession()
    }

    @Test
    fun logoutCalledShouldCallRepositoryLogout() = runTest {
        val expectedStory = MutableLiveData<PagingData<StoryEntity>>()
        expectedStory.value = PagingData.from(emptyList())
        `when`(storyRepository.getAllStory()).thenReturn(expectedStory)

        val mainViewModel = MainViewModel(userRepository, storyRepository)
        mainViewModel.logout()

        verify(userRepository).logout()
    }

    class StoryPagingSource : PagingSource<Int, StoryEntity>() {
        companion object {
            fun snapshot(items: List<StoryEntity>): PagingData<StoryEntity> {
                return PagingData.from(items)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, StoryEntity>): Int? {
            return null
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryEntity> {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }
    }

    val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}