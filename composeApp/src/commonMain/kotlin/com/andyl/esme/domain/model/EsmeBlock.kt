package com.andyl.esme.domain.model

sealed interface EsmeBlock {
    val id: String
    val noteId: String
    val orderIndex: Int

    // 1. TEXTO BASE (Atómico)
    data class Text(override val id: String, override val noteId: String, override val orderIndex: Int, val content: String) : EsmeBlock

    // 2. TAREA (Checklist)
    data class Todo(override val id: String, override val noteId: String, override val orderIndex: Int, val content: String, val isChecked: Boolean) : EsmeBlock

    // 3. PRIORIDAD (!!!)
    data class Priority(override val id: String, override val noteId: String, override val orderIndex: Int, val content: String, val level: PriorityLevel = PriorityLevel.HIGH) : EsmeBlock

    // 4. CITA ( > )
    data class Quote(override val id: String, override val noteId: String, override val orderIndex: Int, val content: String) : EsmeBlock

    // 5. LISTA (* )
    data class Bullet(override val id: String, override val noteId: String, override val orderIndex: Int, val content: String) : EsmeBlock

    // 6. CÓDIGO ( /code )
    data class Code(override val id: String, override val noteId: String, override val orderIndex: Int, val content: String, val language: String = "Kotlin") : EsmeBlock

    // 7. SEPARADOR ( --- )
    data class Divider(override val id: String, override val noteId: String, override val orderIndex: Int) : EsmeBlock

    // 8. GASTO ($)
    data class Expense(override val id: String, override val noteId: String, override val orderIndex: Int, val description: String, val amount: Double) : EsmeBlock

    // 9. IMAGEN ( 📸 )
    data class Image(override val id: String, override val noteId: String, override val orderIndex: Int, val uri: String, val caption: String = "") : EsmeBlock

    // 10. POMODORO ( /pom )
    data class FocusTimer(override val id: String, override val noteId: String, override val orderIndex: Int, val initialMinutes: Int = 25, val state: TimerState = TimerState.IDLE) : EsmeBlock
}

enum class PriorityLevel { LOW, MEDIUM, HIGH }
enum class TimerState { IDLE, RUNNING, PAUSED, FINISHED }