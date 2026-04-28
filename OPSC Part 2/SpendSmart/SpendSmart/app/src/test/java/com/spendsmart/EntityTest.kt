package com.spendsmart

import com.spendsmart.data.entities.BudgetGoal
import com.spendsmart.data.entities.Category
import com.spendsmart.data.entities.Expense
import com.spendsmart.data.entities.User
import org.junit.Assert.*
import org.junit.Test

class EntityTest {

    @Test
    fun `user entity created correctly`() {
        val user = User(username = "john", passwordHash = "abc123hash")
        assertEquals("john", user.username)
        assertEquals("abc123hash", user.passwordHash)
        assertEquals(0L, user.id)
    }

    @Test
    fun `category entity created with defaults`() {
        val category = Category(userId = 1L, name = "Food")
        assertEquals("Food", category.name)
        assertEquals(1L, category.userId)
        assertEquals("#4CAF50", category.colorHex)
    }

    @Test
    fun `expense entity stores all fields`() {
        val now = System.currentTimeMillis()
        val expense = Expense(
            userId = 1L,
            categoryId = 2L,
            amount = 150.50,
            description = "Lunch",
            date = now,
            startTime = "12:00",
            endTime = "13:00",
            photoPath = "/path/to/photo.jpg"
        )
        assertEquals(1L, expense.userId)
        assertEquals(2L, expense.categoryId)
        assertEquals(150.50, expense.amount, 0.001)
        assertEquals("Lunch", expense.description)
        assertEquals(now, expense.date)
        assertEquals("12:00", expense.startTime)
        assertEquals("13:00", expense.endTime)
        assertEquals("/path/to/photo.jpg", expense.photoPath)
    }

    @Test
    fun `expense without photo has null photoPath`() {
        val expense = Expense(userId = 1L, categoryId = null, amount = 50.0,
            description = "Coffee", date = System.currentTimeMillis())
        assertNull(expense.photoPath)
        assertNull(expense.categoryId)
    }

    @Test
    fun `budget goal stores min and max`() {
        val goal = BudgetGoal(userId = 1L, minGoal = 500.0, maxGoal = 5000.0, month = 4, year = 2026)
        assertEquals(500.0, goal.minGoal, 0.001)
        assertEquals(5000.0, goal.maxGoal, 0.001)
        assertEquals(4, goal.month)
        assertEquals(2026, goal.year)
    }

    @Test
    fun `expense copy updates fields`() {
        val original = Expense(userId = 1L, categoryId = 1L, amount = 100.0,
            description = "Old", date = System.currentTimeMillis())
        val updated = original.copy(amount = 200.0, description = "New")
        assertEquals(200.0, updated.amount, 0.001)
        assertEquals("New", updated.description)
        assertEquals(original.userId, updated.userId)
    }

    @Test
    fun `category copy updates name`() {
        val cat = Category(userId = 1L, name = "Transport", colorHex = "#2196F3")
        val updated = cat.copy(name = "Travel")
        assertEquals("Travel", updated.name)
        assertEquals("#2196F3", updated.colorHex)
    }
}
