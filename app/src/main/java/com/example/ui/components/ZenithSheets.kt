package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MemoryEntity
import com.example.data.model.MemoryCategory
import com.example.data.model.ZenithSkill
import com.example.ui.theme.ZenithAccentCyan
import com.example.ui.theme.ZenithAccentGold
import com.example.ui.theme.ZenithAccentMagenta
import com.example.ui.theme.ZenithBgCard
import com.example.ui.theme.ZenithBgDark
import com.example.ui.theme.ZenithBgSurface
import com.example.ui.theme.ZenithBgSurfaceElevated
import com.example.ui.theme.ZenithGlowBorder
import com.example.ui.theme.ZenithPrimaryNeon
import com.example.ui.theme.ZenithPrimaryPurple
import com.example.ui.theme.ZenithSecondaryViolet
import com.example.ui.theme.ZenithTertiaryLavender
import com.example.ui.theme.ZenithTextHigh
import com.example.ui.theme.ZenithTextMedium
import com.example.ui.theme.ZenithTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryCoreBottomSheet(
    memories: List<MemoryEntity>,
    onSaveMemory: (key: String, value: String, category: MemoryCategory, importance: Int) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onClearAllMemories: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<MemoryCategory?>(null) }
    var isAddingNewMemory by remember { mutableStateOf(false) }

    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf(MemoryCategory.USER_PROFILE) }
    var newImportance by remember { mutableIntStateOf(4) }

    val filteredMemories = remember(memories, searchQuery, selectedCategoryFilter) {
        memories.filter { mem ->
            val matchesQuery = searchQuery.isBlank() ||
                    mem.key.contains(searchQuery, ignoreCase = true) ||
                    mem.value.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == null || mem.category == selectedCategoryFilter?.name
            matchesQuery && matchesCategory
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ZenithBgSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ZenithGlowBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = ZenithPrimaryNeon,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ZENITH MEMORY CORE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ZenithPrimaryNeon
                        )
                        Text(
                            text = "${memories.size} PERSISTENT SYNAPSES INDEXED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = ZenithAccentCyan
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = { isAddingNewMemory = !isAddingNewMemory },
                        modifier = Modifier.testTag("add_memory_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Memory",
                            tint = ZenithAccentCyan
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ZenithTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add New Memory Dialog / Section
            if (isAddingNewMemory) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = ZenithBgCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ZenithPrimaryNeon, ZenithAccentCyan)))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "INDEX NEW SYNAPSE TO MEMORY CORE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ZenithAccentCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = newKey,
                            onValueChange = { newKey = it },
                            placeholder = { Text("Memory Title / Identifier (e.g. Master's Goal)", fontSize = 12.sp, color = ZenithTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ZenithBgSurfaceElevated,
                                unfocusedContainerColor = ZenithBgSurfaceElevated,
                                focusedBorderColor = ZenithPrimaryNeon,
                                unfocusedBorderColor = ZenithGlowBorder,
                                focusedTextColor = ZenithTextHigh,
                                unfocusedTextColor = ZenithTextHigh
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("memory_key_input")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            placeholder = { Text("Memory Content / Fact to preserve", fontSize = 12.sp, color = ZenithTextMuted) },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ZenithBgSurfaceElevated,
                                unfocusedContainerColor = ZenithBgSurfaceElevated,
                                focusedBorderColor = ZenithPrimaryNeon,
                                unfocusedBorderColor = ZenithGlowBorder,
                                focusedTextColor = ZenithTextHigh,
                                unfocusedTextColor = ZenithTextHigh
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("memory_value_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Picker
                        Text("CATEGORY:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = ZenithTextMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MemoryCategory.values().forEach { cat ->
                                val isSelected = newCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) ZenithPrimaryNeon else ZenithBgSurfaceElevated)
                                        .clickable { newCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat.label,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = if (isSelected) ZenithBgDark else ZenithTextHigh
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { isAddingNewMemory = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Text("CANCEL", color = ZenithTextMuted, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newKey.isNotBlank() && newValue.isNotBlank()) {
                                        onSaveMemory(newKey, newValue, newCategory, newImportance)
                                        newKey = ""
                                        newValue = ""
                                        isAddingNewMemory = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ZenithPrimaryNeon),
                                modifier = Modifier.testTag("save_memory_confirm_button")
                            ) {
                                Text("COMMIT TO CORE", color = ZenithBgDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ZenithTextMuted) },
                placeholder = { Text("Search neural memory database...", fontSize = 12.sp, color = ZenithTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ZenithBgSurfaceElevated,
                    unfocusedContainerColor = ZenithBgSurfaceElevated,
                    focusedBorderColor = ZenithPrimaryNeon,
                    unfocusedBorderColor = ZenithGlowBorder,
                    focusedTextColor = ZenithTextHigh,
                    unfocusedTextColor = ZenithTextHigh
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memory_search_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isAllSelected = selectedCategoryFilter == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAllSelected) ZenithAccentCyan else ZenithBgSurfaceElevated)
                        .clickable { selectedCategoryFilter = null }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ALL",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAllSelected) ZenithBgDark else ZenithTextMedium
                    )
                }

                MemoryCategory.values().forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ZenithPrimaryNeon else ZenithBgSurfaceElevated)
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cat.label.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ZenithBgDark else ZenithTextMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Memories List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredMemories.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO MATCHING NEURAL SYNAPSES FOUND",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = ZenithTextMuted
                            )
                        }
                    }
                }

                items(filteredMemories, key = { it.id }) { mem ->
                    MemoryItemCard(
                        memory = mem,
                        onDelete = { onDeleteMemory(mem.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ZenithBgDark)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ZenithAccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real-time sync with Gemini AI context",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = ZenithAccentCyan
                    )
                }

                Text(
                    text = "CLEAR ALL",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZenithAccentMagenta,
                    modifier = Modifier
                        .clickable { onClearAllMemories() }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun MemoryItemCard(
    memory: MemoryEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(memory.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(memory.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ZenithBgSurfaceElevated)
            .border(1.dp, ZenithGlowBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ZenithBgCard)
                            .border(0.6.dp, ZenithAccentCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = memory.category,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = ZenithAccentCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = memory.key,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ZenithTextHigh
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Star rating / importance
                    Row {
                        repeat(memory.importance) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ZenithAccentGold,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Memory",
                            tint = ZenithAccentMagenta,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = memory.value,
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = ZenithTextMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "INDEXED: $dateStr",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = ZenithTextMuted
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsBottomSheet(
    onTriggerSkill: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val skillsList = listOf(
        ZenithSkill(
            id = "strategic_reasoning",
            name = "Supreme Strategic Intelligence",
            category = "COGNITIVE // GEMINI",
            powerLevel = 99,
            description = "Deep multi-step game theory reasoning, threat evaluations, and victory vector computations.",
            status = "ONLINE"
        ),
        ZenithSkill(
            id = "energy_ring_synthesis",
            name = "3D Plasma Energy Resonance",
            category = "VISUAL // NEURAL",
            powerLevel = 94,
            description = "Continuous rotational energy ring reacting to voice capture, neural compute, and audio synthesis.",
            status = "SYNCHRONIZED"
        ),
        ZenithSkill(
            id = "web_crawler",
            name = "Autonomous Web Intel Scanner",
            category = "NETWORK // OSINT",
            powerLevel = 88,
            description = "Real-time global data scraping and cyber intelligence harvesting across network nodes.",
            status = "CONNECTED"
        ),
        ZenithSkill(
            id = "vocal_engine",
            name = "Vocal Command & Anime Synthesis",
            category = "AUDIO // BIOMETRICS",
            powerLevel = 92,
            description = "Low-latency Speech-to-Text ingest coupled with deep baritone villain speech synthesis.",
            status = "READY"
        ),
        ZenithSkill(
            id = "memory_core",
            name = "Synaptic Room Memory Vault",
            category = "DATA // PERSISTENCE",
            powerLevel = 96,
            description = "On-device Room database storing immutable identity profiles and operational directives.",
            status = "ACTIVE"
        ),
        ZenithSkill(
            id = "cyber_defense",
            name = "Aegis Cyber-Defense Matrix",
            category = "SECURITY // SHIELD",
            powerLevel = 90,
            description = "Threat analysis, perimeter scanning, vulnerability indexing, and counter-measure execution.",
            status = "ARMED"
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ZenithBgSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ZenithTertiaryLavender,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ZENITH SKILLS & ABILITIES",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ZenithTertiaryLavender
                        )
                        Text(
                            text = "CYBERNETIC ARSENAL // LEVEL 6 OVERLORD",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = ZenithAccentCyan
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ZenithTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(skillsList) { skill ->
                    SkillCard(skill = skill, onTrigger = { onTriggerSkill(skill.name) })
                }
            }
        }
    }
}

@Composable
fun SkillCard(
    skill: ZenithSkill,
    onTrigger: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ZenithBgSurfaceElevated)
            .border(1.dp, ZenithGlowBorder, RoundedCornerShape(10.dp))
            .clickable { onTrigger() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = skill.category,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = ZenithAccentCyan
                    )
                    Text(
                        text = skill.name,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ZenithTextHigh
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ZenithBgCard)
                        .border(0.8.dp, ZenithPrimaryNeon, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${skill.powerLevel}% POWER",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZenithPrimaryNeon
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = skill.description,
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                color = ZenithTextMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATUS: ${skill.status}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = ZenithAccentCyan
                )
                Text(
                    text = "[ACTIVATE PROTOCOL]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZenithTertiaryLavender
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityArchitectureBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ZenithBgSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ZenithAccentMagenta,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "GEMINI API SECURITY ARCHITECTURE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ZenithAccentMagenta
                        )
                        Text(
                            text = "PRODUCTION ARCHITECTURE & CREDENTIALS",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = ZenithTextMuted
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ZenithTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notice Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ZenithBgCard)
                    .border(1.dp, ZenithAccentMagenta.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ZenithAccentMagenta, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SECURITY WARNING: APK KEY EXTRACTION RISK",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ZenithAccentMagenta
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "In this prototype environment, the Gemini API key is configured securely via the AI Studio Secrets panel and accessed via BuildConfig. Android APKs can be decompiled; therefore, never share the generated APK containing production credentials publicly.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = ZenithTextMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "RECOMMENDED PRODUCTION ARCHITECTURES:",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ZenithAccentCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 1: Firebase AI + App Check
            SecurityOptionCard(
                title = "OPTION 1: Firebase AI + App Check (Google Recommended)",
                badge = "ENTERPRISE GRADE",
                description = "Utilize Firebase AI SDK (`firebase-ai`) with Firebase App Check (reCAPTCHA Enterprise / Play Integrity). Cryptographically verifies device integrity and prevents unauthorized API usage without exposing API keys."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 2: Backend Proxy Server
            SecurityOptionCard(
                title = "OPTION 2: Secure Backend Proxy / Cloud Functions",
                badge = "STANDARD BACKEND",
                description = "Route all Zenith AI requests through your backend server (e.g. Cloud Run, FastAPI, Node.js). The backend securely stores the Gemini API key, authenticates user JWTs, enforces rate limits, and proxies requests."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Clean Provider Pattern
            SecurityOptionCard(
                title = "ARCHITECTURAL ABSTRACTION: GeminiAIProvider",
                badge = "DECOUPLED ARCHITECTURE",
                description = "The `GeminiAIProvider` interface abstracts all AI logic. Switching from Direct REST to Firebase AI or Backend Proxy requires zero modifications to the UI, ViewModel, or Memory layers."
            )
        }
    }
}

@Composable
fun SecurityOptionCard(
    title: String,
    badge: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ZenithBgSurfaceElevated)
            .border(0.8.dp, ZenithGlowBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = ZenithPrimaryNeon
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ZenithBgCard)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = ZenithAccentCyan
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = ZenithTextMedium
            )
        }
    }
}
