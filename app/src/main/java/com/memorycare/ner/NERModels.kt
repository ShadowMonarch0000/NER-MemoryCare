package com.memorycare.ner

import org.json.JSONObject
import java.util.UUID

enum class GameType {
    REMEMBER_ROUTE,
    FLIP_MATCH,
    GUESS_LYRICS,
    SPOT_REPEAT,
    BASIC_MEMORY,
}

enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD,
}

enum class ContentCategory {
    FOOD,
    FESTIVALS,
    CLOTHES,
    PLACES,
    ROUTES,
    MEMORIES,
}

enum class ReminderType {
    MEDICINE,
    WALKING,
    SLEEP,
    COGNITIVE_GAME,
    WATER,
    MEALS,
    APPOINTMENT,
    CUSTOM,
}

enum class AppLanguage {
    ENGLISH,
    HINDI,
    ASSAMESE,
    BENGALI,
}

data class PatientProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val age: String,
    val condition: String,
    val avatarEmoji: String = "🧓",
    val inactiveDays: Int = 0,
    val preferredLanguage: AppLanguage = AppLanguage.ENGLISH,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("age", age)
        put("condition", condition)
        put("avatarEmoji", avatarEmoji)
        put("inactiveDays", inactiveDays)
        put("preferredLanguage", preferredLanguage.name)
    }

    companion object {
        fun fromJson(json: JSONObject): PatientProfile = PatientProfile(
            id = json.getString("id"),
            name = json.getString("name"),
            age = json.getString("age"),
            condition = json.getString("condition"),
            avatarEmoji = json.optString("avatarEmoji", "🧓"),
            inactiveDays = json.optInt("inactiveDays", 0),
            preferredLanguage = try {
                AppLanguage.valueOf(json.optString("preferredLanguage", AppLanguage.ENGLISH.name))
            } catch (_: Exception) {
                AppLanguage.ENGLISH
            },
        )
    }
}

data class GameImageSetItem(
    val setId: String = UUID.randomUUID().toString(),
    val patientId: String,
    val gameType: GameType,
    val setName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class GameImageItem(
    val imageId: String = UUID.randomUUID().toString(),
    val patientId: String,
    val gameType: GameType,
    val setId: String,
    val title: String,
    val localFilePath: String,
    val remoteImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING_SYNC",
)

data class SongItem(
    val songId: String = UUID.randomUUID().toString(),
    val patientId: String,
    val title: String,
    val artist: String = "",
    val language: AppLanguage = AppLanguage.ENGLISH,
    val localFilePath: String = "",
    val remoteFileUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING_SYNC",
)

data class LyricQuestionItem(
    val questionId: String = UUID.randomUUID().toString(),
    val songId: String,
    val patientId: String,
    val prompt: String,
    val correctAnswer: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val createdAt: Long = System.currentTimeMillis(),
)

data class PersonalizedContentItem(
    val id: String = UUID.randomUUID().toString(),
    val category: ContentCategory,
    val title: String,
    val photoPath: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("category", category.name)
        put("title", title)
        put("photoPath", photoPath)
        put("description", description)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    companion object {
        fun fromJson(json: JSONObject): PersonalizedContentItem = PersonalizedContentItem(
            id = json.getString("id"),
            category = ContentCategory.valueOf(json.getString("category")),
            title = json.getString("title"),
            photoPath = json.getString("photoPath"),
            description = json.optString("description", ""),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
        )
    }
}

data class GameSessionRecord(
    val id: String = UUID.randomUUID().toString(),
    val gameType: GameType,
    val date: String,
    val startedAt: Long,
    val completedAt: Long,
    val difficulty: DifficultyLevel,
    val score: Int,
    val accuracy: Float,
    val mistakes: Int,
    val attempts: Int,
    val timeTakenSeconds: Int,
    val levelsCompleted: Int,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("gameType", gameType.name)
        put("date", date)
        put("startedAt", startedAt)
        put("completedAt", completedAt)
        put("difficulty", difficulty.name)
        put("score", score)
        put("accuracy", accuracy.toDouble())
        put("mistakes", mistakes)
        put("attempts", attempts)
        put("timeTakenSeconds", timeTakenSeconds)
        put("levelsCompleted", levelsCompleted)
    }

    companion object {
        fun fromJson(json: JSONObject): GameSessionRecord = GameSessionRecord(
            id = json.getString("id"),
            gameType = GameType.valueOf(json.getString("gameType")),
            date = json.getString("date"),
            startedAt = json.getLong("startedAt"),
            completedAt = json.getLong("completedAt"),
            difficulty = DifficultyLevel.valueOf(json.getString("difficulty")),
            score = json.getInt("score"),
            accuracy = json.getDouble("accuracy").toFloat(),
            mistakes = json.getInt("mistakes"),
            attempts = json.getInt("attempts"),
            timeTakenSeconds = json.getInt("timeTakenSeconds"),
            levelsCompleted = json.getInt("levelsCompleted"),
        )
    }
}

data class AdaptiveDifficultyInfo(
    val gameType: GameType,
    val level: Int = 1,
    val parameterDetail: String = "", // e.g. "Grid: 3×3" or "Pictures: 3"
    val currentDifficulty: DifficultyLevel,
    val recommendedDifficulty: DifficultyLevel,
    val recentAccuracy: Float,
    val recentMistakes: Int,
    val recentCompletionRate: Float,
    val trend: String, // "Improving", "Stable", "Needs support"
    val explanationReason: String,
)

data class ReminderItem(
    val id: String = UUID.randomUUID().toString(),
    val type: ReminderType,
    val title: String,
    val message: String,
    val scheduledTime: String, // e.g. "08:00 AM"
    val repeatSchedule: String = "Every day",
    val enabled: Boolean = true,
    val completedToday: Boolean = false,
    val snoozedUntilTime: Long = 0L,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("type", type.name)
        put("title", title)
        put("message", message)
        put("scheduledTime", scheduledTime)
        put("repeatSchedule", repeatSchedule)
        put("enabled", enabled)
        put("completedToday", completedToday)
        put("snoozedUntilTime", snoozedUntilTime)
    }

    companion object {
        fun fromJson(json: JSONObject): ReminderItem = ReminderItem(
            id = json.getString("id"),
            type = ReminderType.valueOf(json.getString("type")),
            title = json.getString("title"),
            message = json.getString("message"),
            scheduledTime = json.getString("scheduledTime"),
            repeatSchedule = json.optString("repeatSchedule", "Every day"),
            enabled = json.optBoolean("enabled", true),
            completedToday = json.optBoolean("completedToday", false),
            snoozedUntilTime = json.optLong("snoozedUntilTime", 0L),
        )
    }
}

data class CaregiverAlertItem(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "INACTIVITY_ALERT", "PATTERN_CHANGE", "REMINDER_MISSED"
    val date: String,
    val message: String,
    val severity: String, // "INFO", "WARNING", "HIGH"
    val acknowledged: Boolean = false,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("type", type)
        put("date", date)
        put("message", message)
        put("severity", severity)
        put("acknowledged", acknowledged)
    }

    companion object {
        fun fromJson(json: JSONObject): CaregiverAlertItem = CaregiverAlertItem(
            id = json.getString("id"),
            type = json.getString("type"),
            date = json.getString("date"),
            message = json.getString("message"),
            severity = json.getString("severity"),
            acknowledged = json.optBoolean("acknowledged", false),
        )
    }
}

data class AccessibilitySettings(
    val fontSizeScale: Float = 1.0f, // 1.0 Normal, 1.25 Large, 1.5 Extra Large
    val highContrast: Boolean = false,
    val voiceAssistantEnabled: Boolean = false,
    val reducedMotion: Boolean = false,
    val largeButtons: Boolean = true,
)

data class WeeklyAIReport(
    val activeDaysCount: Int,
    val totalGamesCompleted: Int,
    val currentStreakDays: Int,
    val lastActiveDate: String,
    val gameSummaries: List<AdaptiveDifficultyInfo>,
    val activityObservation: String,
    val alertMessage: String?,
)
