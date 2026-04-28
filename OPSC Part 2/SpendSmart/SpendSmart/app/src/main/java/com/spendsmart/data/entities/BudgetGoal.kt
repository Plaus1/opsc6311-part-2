package com.spendsmart.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_goals",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val minGoal: Double,
    val maxGoal: Double,
    val month: Int,
    val year: Int,
    val createdAt: Long = System.currentTimeMillis()
)
