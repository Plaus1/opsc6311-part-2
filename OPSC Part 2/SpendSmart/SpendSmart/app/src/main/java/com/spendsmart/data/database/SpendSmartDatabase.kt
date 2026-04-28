package com.spendsmart.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spendsmart.data.dao.BudgetGoalDao
import com.spendsmart.data.dao.CategoryDao
import com.spendsmart.data.dao.ExpenseDao
import com.spendsmart.data.dao.UserDao
import com.spendsmart.data.entities.BudgetGoal
import com.spendsmart.data.entities.Category
import com.spendsmart.data.entities.Expense
import com.spendsmart.data.entities.User

@Database(
    entities = [User::class, Category::class, Expense::class, BudgetGoal::class],
    version = 1,
    exportSchema = true
)
abstract class SpendSmartDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao

    companion object {
        @Volatile
        private var INSTANCE: SpendSmartDatabase? = null

        fun getInstance(context: Context): SpendSmartDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendSmartDatabase::class.java,
                    "spendsmart_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
