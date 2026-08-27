package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zenith_chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER", "ZENITH", "SYSTEM"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "NORMAL",
    val status: String = "COMPLETED", // "PROCESSING", "COMPLETED", "ERROR"
    val toolCallName: String? = null,
    val toolCallResult: String? = null
)
