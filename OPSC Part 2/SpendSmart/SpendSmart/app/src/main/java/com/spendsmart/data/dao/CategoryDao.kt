package com.spendsmart.data.dao

import androidx.room.*
import com.spendsmart.data.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategoriesByUser(userId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getCategoriesByUserSync(userId: Long): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): Category?

    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId")
    suspend fun getCategoryCount(userId: Long): Int
}
