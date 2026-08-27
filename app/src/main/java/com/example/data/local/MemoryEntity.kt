package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MemoryCategory

@Entity(tableName = "zenith_memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = MemoryCategory.USER_PROFILE.name,
    val importance: Int = 3, // 1 to 5
    val timestamp: Long = System.currentTimeMillis()
)
