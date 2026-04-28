package com.spendsmart.data.dao

import androidx.room.*
import com.spendsmart.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash LIMIT 1")
    suspend fun login(username: String, passwordHash: String): User?

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}
