package com.spendsmart.data.dao

import androidx.room.*
import com.spendsmart.data.entities.Expense
import kotlinx.coroutines.flow.Flow

data class CategorySpending(
    val categoryId: Long?,
    val categoryName: String?,
    val totalAmount: Double
)

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    fun getAllExpensesByUser(userId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getExpensesByDateRangeSync(userId: Long, startDate: Long, endDate: Long): List<Expense>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpending(userId: Long, startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT e.categoryId, c.name AS categoryName, SUM(e.amount) AS totalAmount 
        FROM expenses e
        LEFT JOIN categories c ON e.categoryId = c.id
        WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate
        GROUP BY e.categoryId
    """)
    suspend fun getSpendingByCategory(userId: Long, startDate: Long, endDate: Long): List<CategorySpending>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesInPeriod(userId: Long, startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId")
    suspend fun getExpenseCount(userId: Long): Int

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getTotalSpendingLive(userId: Long, startDate: Long, endDate: Long): Flow<Double?>
}
