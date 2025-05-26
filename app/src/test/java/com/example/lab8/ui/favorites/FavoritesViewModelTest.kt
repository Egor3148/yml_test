package com.example.lab8.ui.favorites

import android.app.Application
import app.cash.turbine.test
import com.example.lab8.data.local.AppDatabase
import com.example.lab8.data.local.FavoriteRecipeDao
import com.example.lab8.data.local.FavoriteRecipeEntity
import com.example.lab8.util.MainCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FavoritesViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @RelaxedMockK
    lateinit var mockApplication: Application

    @RelaxedMockK
    lateinit var mockFavoriteRecipeDao: FavoriteRecipeDao

    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setup() = MockKAnnotations.init(this)

    private fun createViewModel() {
        viewModel = FavoritesViewModel(mockApplication, mockFavoriteRecipeDao)
    }

    @Test
    fun `when DAO is empty then emits only emptyList`() =
        runTest(mainCoroutineRule.testDispatcher) {
            every { mockFavoriteRecipeDao.getAllFavorites() } returns flowOf(emptyList())

            createViewModel()

            viewModel.favoriteRecipes.test {
                assertEquals(emptyList<FavoriteRecipeEntity>(), awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `when DAO returns data then emits empty then real list`() =
        runTest(mainCoroutineRule.testDispatcher) {

            val fakeList = listOf(
                FavoriteRecipeEntity("1", "Dish 1", "url1"),
                FavoriteRecipeEntity("2", "Dish 2", "url2")
            )
            every { mockFavoriteRecipeDao.getAllFavorites() } returns flowOf(fakeList)

            createViewModel()

            viewModel.favoriteRecipes.test {
                assertEquals(emptyList<FavoriteRecipeEntity>(), awaitItem())

                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

                assertEquals(fakeList, awaitItem())

                cancelAndConsumeRemainingEvents()
            }
        }
}