package com.example.data.repository

import com.example.data.local.ChatMessageEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.ZenithDao
import com.example.data.model.MemoryCategory
import kotlinx.coroutines.flow.Flow

class ZenithRepository(private val dao: ZenithDao) {

    val allMemoriesFlow: Flow<List<MemoryEntity>> = dao.getAllMemoriesFlow()
    val allMessagesFlow: Flow<List<ChatMessageEntity>> = dao.getAllMessagesFlow()

    suspend fun getAllMemories(): List<MemoryEntity> = dao.getAllMemories()

    fun searchMemories(query: String): Flow<List<MemoryEntity>> = dao.searchMemories(query)

    fun getMemoriesByCategory(category: String): Flow<List<MemoryEntity>> = dao.getMemoriesByCategory(category)

    suspend fun saveMemory(
        key: String,
        value: String,
        category: MemoryCategory = MemoryCategory.USER_PROFILE,
        importance: Int = 3
    ): Long {
        return dao.insertMemory(
            MemoryEntity(
                key = key.trim(),
                value = value.trim(),
                category = category.name,
                importance = importance
            )
        )
    }

    suspend fun deleteMemory(id: Long) = dao.deleteMemoryById(id)

    suspend fun clearMemories() = dao.clearAllMemories()

    suspend fun insertChatMessage(message: ChatMessageEntity): Long = dao.insertMessage(message)

    suspend fun updateChatMessage(message: ChatMessageEntity) = dao.updateMessage(message)

    suspend fun getRecentMessages(limit: Int = 10): List<ChatMessageEntity> = dao.getRecentMessages(limit)

    suspend fun clearChat() = dao.clearChatHistory()

    suspend fun seedDefaultDirectivesIfEmpty() {
        val current = dao.getAllMemories()
        if (current.isEmpty()) {
            dao.insertMemory(
                MemoryEntity(
                    key = "Master Designation",
                    value = "User is recognized as Supreme Commander of the Zenith Terminal.",
                    category = MemoryCategory.USER_PROFILE.name,
                    importance = 5
                )
            )
            dao.insertMemory(
                MemoryEntity(
                    key = "Prime Directive",
                    value = "Operate with unyielding strategic dominance, dark intellect, and absolute precision.",
                    category = MemoryCategory.STRATEGIC_DIRECTIVE.name,
                    importance = 5
                )
            )
            dao.insertMemory(
                MemoryEntity(
                    key = "Void Matrix State",
                    value = "Neural synapses synchronized with Google Gemini Core v3.5 Flash.",
                    category = MemoryCategory.LORE_DATA.name,
                    importance = 4
                )
            )
        }
    }
}
