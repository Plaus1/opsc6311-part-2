package com.spendsmart.data.dao

import androidx.room.*
import com.spendsmart.data.entities.BudgetGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: BudgetGoal): Long

    @Update
    suspend fun updateGoal(goal: BudgetGoal)

    @Delete
    suspend fun deleteGoal(goal: BudgetGoal)

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getGoalForMonth(userId: Long, month: Int, year: Int): BudgetGoal?

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    fun getGoalForMonthLive(userId: Long, month: Int, year: Int): Flow<BudgetGoal?>

    @Query("SELECT * FROM budget_goals WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun getAllGoals(userId: Long): Flow<List<BudgetGoal>>
}
