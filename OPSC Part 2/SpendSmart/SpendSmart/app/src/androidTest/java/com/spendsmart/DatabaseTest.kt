package com.spendsmart

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spendsmart.data.dao.CategoryDao
import com.spendsmart.data.dao.ExpenseDao
import com.spendsmart.data.dao.UserDao
import com.spendsmart.data.dao.BudgetGoalDao
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.data.entities.BudgetGoal
import com.spendsmart.data.entities.Category
import com.spendsmart.data.entities.Expense
import com.spendsmart.data.entities.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: SpendSmartDatabase
    private lateinit var userDao: UserDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var expenseDao: ExpenseDao
    private lateinit var goalDao: BudgetGoalDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SpendSmartDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        categoryDao = db.categoryDao()
        expenseDao = db.expenseDao()
        goalDao = db.budgetGoalDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveUser() = runBlocking {
        val user = User(username = "testuser", passwordHash = "hashedpwd")
        val id = userDao.insertUser(user)
        assertTrue(id > 0)

        val retrieved = userDao.getUserById(id)
        assertNotNull(retrieved)
        assertEquals("testuser", retrieved!!.username)
    }

    @Test
    fun loginWithCorrectCredentials() = runBlocking {
        val user = User(username = "alice", passwordHash = "hash123")
        userDao.insertUser(user)

        val loggedIn = userDao.login("alice", "hash123")
        assertNotNull(loggedIn)
        assertEquals("alice", loggedIn!!.username)
    }

    @Test
    fun loginWithWrongPassword() = runBlocking {
        val user = User(username = "bob", passwordHash = "correcthash")
        userDao.insertUser(user)

        val loggedIn = userDao.login("bob", "wronghash")
        assertNull(loggedIn)
    }

    @Test
    fun insertAndRetrieveCategory() = runBlocking {
        val userId = userDao.insertUser(User(username = "u1", passwordHash = "h1"))
        val cat = Category(userId = userId, name = "Food", colorHex = "#FF5722")
        val catId = categoryDao.insertCategory(cat)
        assertTrue(catId > 0)

        val cats = categoryDao.getCategoriesByUser(userId).first()
        assertEquals(1, cats.size)
        assertEquals("Food", cats[0].name)
    }

    @Test
    fun deleteCategory() = runBlocking {
        val userId = userDao.insertUser(User(username = "u2", passwordHash = "h2"))
        val cat = Category(userId = userId, name = "Transport")
        val catId = categoryDao.insertCategory(cat)

        val insertedCat = categoryDao.getCategoryById(catId)!!
        categoryDao.deleteCategory(insertedCat)

        val cats = categoryDao.getCategoriesByUser(userId).first()
        assertTrue(cats.isEmpty())
    }

    @Test
    fun insertAndRetrieveExpense() = runBlocking {
        val userId = userDao.insertUser(User(username = "u3", passwordHash = "h3"))
        val catId = categoryDao.insertCategory(Category(userId = userId, name = "Food"))

        val now = System.currentTimeMillis()
        val expense = Expense(
            userId = userId,
            categoryId = catId,
            amount = 250.0,
            description = "Groceries",
            date = now
        )
        val expId = expenseDao.insertExpense(expense)
        assertTrue(expId > 0)

        val retrieved = expenseDao.getExpenseById(expId)
        assertNotNull(retrieved)
        assertEquals(250.0, retrieved!!.amount, 0.001)
        assertEquals("Groceries", retrieved.description)
    }

    @Test
    fun getTotalSpending() = runBlocking {
        val userId = userDao.insertUser(User(username = "u4", passwordHash = "h4"))
        val now = System.currentTimeMillis()
        val start = now - 1000L
        val end = now + 1000L

        expenseDao.insertExpense(Expense(userId = userId, categoryId = null, amount = 100.0,
            description = "Item 1", date = now))
        expenseDao.insertExpense(Expense(userId = userId, categoryId = null, amount = 200.0,
            description = "Item 2", date = now))

        val total = expenseDao.getTotalSpending(userId, start, end)
        assertEquals(300.0, total!!, 0.001)
    }

    @Test
    fun getSpendingByCategory() = runBlocking {
        val userId = userDao.insertUser(User(username = "u5", passwordHash = "h5"))
        val catId = categoryDao.insertCategory(Category(userId = userId, name = "Food"))
        val now = System.currentTimeMillis()

        expenseDao.insertExpense(Expense(userId = userId, categoryId = catId, amount = 50.0,
            description = "Lunch", date = now))
        expenseDao.insertExpense(Expense(userId = userId, categoryId = catId, amount = 75.0,
            description = "Dinner", date = now))

        val result = expenseDao.getSpendingByCategory(userId, now - 5000L, now + 5000L)
        assertEquals(1, result.size)
        assertEquals(125.0, result[0].totalAmount, 0.001)
    }

    @Test
    fun insertAndRetrieveBudgetGoal() = runBlocking {
        val userId = userDao.insertUser(User(username = "u6", passwordHash = "h6"))
        val goal = BudgetGoal(userId = userId, minGoal = 1000.0, maxGoal = 5000.0, month = 4, year = 2026)
        goalDao.insertGoal(goal)

        val retrieved = goalDao.getGoalForMonth(userId, 4, 2026)
        assertNotNull(retrieved)
        assertEquals(1000.0, retrieved!!.minGoal, 0.001)
        assertEquals(5000.0, retrieved.maxGoal, 0.001)
    }

    @Test
    fun updateExpense() = runBlocking {
        val userId = userDao.insertUser(User(username = "u7", passwordHash = "h7"))
        val expId = expenseDao.insertExpense(
            Expense(userId = userId, categoryId = null, amount = 100.0,
                description = "Original", date = System.currentTimeMillis()))

        val original = expenseDao.getExpenseById(expId)!!
        expenseDao.updateExpense(original.copy(amount = 999.0, description = "Updated"))

        val updated = expenseDao.getExpenseById(expId)
        assertEquals(999.0, updated!!.amount, 0.001)
        assertEquals("Updated", updated.description)
    }

    @Test
    fun expensesFilteredByDateRange() = runBlocking {
        val userId = userDao.insertUser(User(username = "u8", passwordHash = "h8"))
        val base = System.currentTimeMillis()

        expenseDao.insertExpense(Expense(userId = userId, categoryId = null, amount = 10.0,
            description = "Old", date = base - 10_000_000L))
        expenseDao.insertExpense(Expense(userId = userId, categoryId = null, amount = 20.0,
            description = "Recent", date = base))

        val result = expenseDao.getExpensesByDateRange(userId, base - 1000L, base + 1000L).first()
        assertEquals(1, result.size)
        assertEquals("Recent", result[0].description)
    }

    @Test
    fun usernameMustBeUnique() = runBlocking {
        userDao.insertUser(User(username = "unique", passwordHash = "h"))
        val existing = userDao.getUserByUsername("unique")
        assertNotNull(existing)

        val nonExisting = userDao.getUserByUsername("notexists")
        assertNull(nonExisting)
    }
}
