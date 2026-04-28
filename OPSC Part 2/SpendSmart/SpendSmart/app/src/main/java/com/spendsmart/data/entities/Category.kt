package com.spendsmart.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val colorHex: String = "#4CAF50",
    val iconName: String = "ic_category",
    val createdAt: Long = System.currentTimeMillis()
)
