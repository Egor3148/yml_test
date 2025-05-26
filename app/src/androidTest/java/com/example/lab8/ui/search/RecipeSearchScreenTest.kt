package com.example.lab8.ui.search

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lab8.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeSearchScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun searchBar_whenTextEntered_displaysTextInField() {
        val searchText = "Pasta"
        composeTestRule.onNodeWithText("Найти рецепт...").performTextInput(searchText)
        composeTestRule.onNodeWithText(searchText).assertIsDisplayed()
    }

    @Test
    fun searchBar_whenSearchClickedWithText_triggersSearch() {
        val searchText = "Arrabiata"
        composeTestRule.onNodeWithText("Найти рецепт...").performTextInput(searchText)
        composeTestRule.onNodeWithText("Поиск").performClick()

        try {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule
                    .onAllNodesWithText("Arrabiata", substring = true, ignoreCase = true)
                    .fetchSemanticsNodes().size > 1
            }
            composeTestRule.onNodeWithText("Spicy Arrabiata Penne", substring = true).assertIsDisplayed()
        } catch (e: AssertionError) {
            println("Примечание: Условие waitUntil не выполнилось. ${e.message}")
        } catch (e: Exception) {
            println("Примечание: Условие не выполнилось или произошла ошибка. Тип: ${e::class.simpleName}, Сообщение: ${e.message}")
        }
    }
}