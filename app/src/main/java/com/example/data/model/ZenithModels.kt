package com.example.data.model

enum class ZenithMode(val title: String, val code: String, val description: String) {
    NORMAL(
        title = "Sovereign",
        code = "PROTOCOL-01 // OVERLORD",
        description = "Supreme intellect, dark charisma, calm superiority, and commanding execution."
    ),
    STRATEGIST(
        title = "Strategist",
        code = "PROTOCOL-02 // TACTICIAN",
        description = "Ruthless tactical breakdowns, game theory optimization, risk matrix & victory blueprints."
    ),
    DARK(
        title = "Dark Void",
        code = "PROTOCOL-03 // SHADOW",
        description = "Existential computing, cybernetic mysticism, forbidden algorithms, and uncompromising logic."
    ),
    GUARDIAN(
        title = "Guardian",
        code = "PROTOCOL-04 // AEGIS",
        description = "Vigilant cybersecurity bastion, counter-measure diagnostics, threat detection and shield arrays."
    )
}

enum class ZenithRingState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

enum class MemoryCategory(val label: String, val iconName: String) {
    USER_PROFILE("Identity", "person"),
    STRATEGIC_DIRECTIVE("Directive", "military_tech"),
    THREAT_INTEL("Threat Intel", "security"),
    LORE_DATA("Neural Archive", "storage"),
    OPERATIONAL_LOG("Ops Log", "terminal")
}

data class ZenithSkill(
    val id: String,
    val name: String,
    val category: String,
    val powerLevel: Int,
    val description: String,
    val status: String
)
