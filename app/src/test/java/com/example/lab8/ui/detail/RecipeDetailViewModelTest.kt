package com.example.lab8.ui.detail

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.room.RoomDatabase
import app.cash.turbine.test
import kotlinx.coroutines.test.advanceUntilIdle
import com.example.lab8.data.local.FavoriteRecipeDao
import com.example.lab8.data.local.FavoriteRecipeEntity
import com.example.lab8.data.network.RetrofitClient
import com.example.lab8.data.network.TheMealDbApiService
import com.example.lab8.data.MealDetail
import com.example.lab8.data.MealDetailResponse
import com.example.lab8.data.local.AppDatabase
import com.example.lab8.util.MainCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import io.mockk.coEvery

@ExperimentalCoroutinesApi
class RecipeDetailViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockApplication: Application

    @RelaxedMockK
    private lateinit var mockMealDbService: TheMealDbApiService

    @RelaxedMockK
    private lateinit var mockFavoriteRecipeDao: FavoriteRecipeDao

    @RelaxedMockK
    private lateinit var mockAppDatabase: AppDatabase

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: RecipeDetailViewModel

    private val testMealId = "52771"
    private val fakeMealDetail = MealDetail(
        id = testMealId, name = "Spicy Arrabiata Penne", thumbnailUrl = "url.jpg",
        drinkAlternate = null, category = "Vegetarian", area = "Italian", instructions = "Cook pasta...", tags = null, youtubeUrl = null,
        ingredient1 = "Penne", ingredient2 = "Tomato", ingredient3 = null, ingredient4 = null, ingredient5 = null,
        ingredient6 = null, ingredient7 = null, ingredient8 = null, ingredient9 = null, ingredient10 = null,
        ingredient11 = null, ingredient12 = null, ingredient13 = null, ingredient14 = null, ingredient15 = null,
        ingredient16 = null, ingredient17 = null, ingredient18 = null, ingredient19 = null, ingredient20 = null,
        measure1 = "100g", measure2 = "2", measure3 = null, measure4 = null, measure5 = null,
        measure6 = null, measure7 = null, measure8 = null, measure9 = null, measure10 = null,
        measure11 = null, measure12 = null, measure13 = null, measure14 = null, measure15 = null,
        measure16 = null, measure17 = null, measure18 = null, measure19 = null, measure20 = null,
        sourceUrl = null, imageSource = null, creativeCommonsConfirmed = null, dateModified = null
    )
    private val fakeMealDetailResponse = MealDetailResponse(listOf(fakeMealDetail))
    private val fakeFavoriteEntity = FavoriteRecipeEntity(testMealId, fakeMealDetail.name, fakeMealDetail.thumbnailUrl)


    @Before
    fun setUp() {
        every { mockApplication.applicationContext } returns mockApplication

        mockkStatic(Room::class)
        val mockBuilder = mockk<RoomDatabase.Builder<AppDatabase>>(relaxed = true)

        every { Room.databaseBuilder(any(), AppDatabase::class.java, any()) } returns mockBuilder
        every { mockBuilder.build() } returns mockAppDatabase
        every { mockAppDatabase.favoriteRecipeDao() } returns mockFavoriteRecipeDao

        mockkObject(RetrofitClient)
        every { RetrofitClient.instance } returns mockMealDbService

        savedStateHandle = SavedStateHandle(mapOf("mealId" to testMealId))
    }

    @After
    fun tearDown() {
        unmockkStatic(Room::class)
        unmockkObject(RetrofitClient)
    }


    private fun createViewModel() {
        viewModel = RecipeDetailViewModel(mockApplication, savedStateHandle)
    }

    @Test
    fun `init - when mealId provided - then fetches details and favorite status`() = runTest(mainCoroutineRule.testDispatcher) {
        coEvery { mockMealDbService.getMealDetailsById(testMealId) } returns Response.success(fakeMealDetailResponse)
        coEvery { mockFavoriteRecipeDao.getFavoriteById(testMealId) } returns flowOf(null)

        createViewModel()

        viewModel.isLoading.test {
            assertEquals(false, awaitItem())

            assertEquals(true, awaitItem())

            assertEquals(false, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
        assertEquals(fakeMealDetail, viewModel.mealDetail.value)
        assertFalse(viewModel.isFavorite.value)
        coVerify { mockMealDbService.getMealDetailsById(testMealId) }
        coVerify { mockFavoriteRecipeDao.getFavoriteById(testMealId) }
    }
}