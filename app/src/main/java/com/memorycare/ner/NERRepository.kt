package com.memorycare.ner

import android.content.Context
import androidx.core.content.edit
import com.memorycare.ner.db.NERDatabase
import org.json.JSONArray

class NERRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db = NERDatabase.getInstance(appContext)
    private val prefs = appContext.getSharedPreferences("ner_app_main_store", Context.MODE_PRIVATE)

    // ==========================================
    // 0. PATIENT LIST & SETUP MANAGEMENT (ROOM DB)
    // ==========================================

    fun isCaregiverSetupCompleted(): Boolean {
        return db.isCaregiverSetupCompleted()
    }

    fun setCaregiverSetupCompleted(completed: Boolean) {
        db.setCaregiverSetupCompleted(completed)
    }

    fun getActivePatientId(): String? {
        return db.getCurrentPatientId()
    }

    fun setActivePatientId(patientId: String?) {
        db.setCurrentPatientId(patientId)
        if (patientId != null) {
            val p = db.getPatientById(patientId)
            if (p != null) {
                setLanguage(p.preferredLanguage)
            }
        }
    }

    fun getActivePatient(): PatientProfile? {
        val activeId = getActivePatientId() ?: return null
        return db.getPatientById(activeId)
    }

    fun getPatientById(patientId: String): PatientProfile? {
        return db.getPatientById(patientId)
    }

    fun getPatients(): List<PatientProfile> {
        return db.getAllPatients()
    }

    fun savePatients(list: List<PatientProfile>) {
        for (p in list) {
            db.insertOrUpdatePatient(p)
        }
    }

    fun savePatient(patient: PatientProfile) {
        db.insertOrUpdatePatient(patient)
    }

    fun deletePatient(patientId: String) {
        db.deletePatient(patientId)
    }

    // ==========================================
    // 1. CAREGIVER PIN
    // ==========================================

    fun getCaregiverPin(): String {
        return prefs.getString("caregiver_pin", "1234") ?: "1234"
    }

    fun setCaregiverPin(newPin: String) {
        prefs.edit(commit = true) {
            putString("caregiver_pin", newPin)
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        return getCaregiverPin() == enteredPin.trim()
    }

    // ==========================================
    // 2. PERSONALIZED CONTENT
    // ==========================================

    fun getAllPersonalizedContent(): List<PersonalizedContentItem> {
        val jsonStr = prefs.getString("personalized_content", "[]") ?: "[]"
        val list = mutableListOf<PersonalizedContentItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                list.add(PersonalizedContentItem.fromJson(jsonArray.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getContentByCategory(category: ContentCategory): List<PersonalizedContentItem> {
        return getAllPersonalizedContent().filter { it.category == category }
    }

    fun savePersonalizedContent(item: PersonalizedContentItem) {
        val current = getAllPersonalizedContent().toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            current[index] = item
        } else {
            current.add(0, item)
        }
        persistContent(current)
    }

    fun deletePersonalizedContent(id: String) {
        val current = getAllPersonalizedContent().toMutableList()
        current.removeAll { it.id == id }
        persistContent(current)
    }

    private fun persistContent(list: List<PersonalizedContentItem>) {
        val jsonArray = JSONArray()
        for (item in list) {
            jsonArray.put(item.toJson())
        }
        prefs.edit(commit = true) {
            putString("personalized_content", jsonArray.toString())
        }
    }

    // ==========================================
    // 3. DAILY REMINDERS (PATIENT ISOLATED)
    // ==========================================

    fun getRemindersForPatient(patientId: String): List<ReminderItem> {
        val dbReminders = db.getRemindersForPatient(patientId)
        if (dbReminders.isEmpty()) {
            val defaults = listOf(
                ReminderItem(type = ReminderType.MEDICINE, title = "Morning Medicine", message = "Time to take your morning prescription medicine.", scheduledTime = "08:00 AM"),
                ReminderItem(type = ReminderType.WALKING, title = "Daily Walk", message = "It's time for a gentle 15-minute walk in the garden.", scheduledTime = "10:00 AM"),
                ReminderItem(type = ReminderType.COGNITIVE_GAME, title = "Memory Activity", message = "It's time for your memory activity today.", scheduledTime = "06:00 PM"),
                ReminderItem(type = ReminderType.SLEEP, title = "Night Rest", message = "It's time to get ready for sleep.", scheduledTime = "10:00 PM"),
            )
            for (r in defaults) {
                db.insertReminder(patientId, r)
            }
            return defaults
        }
        return dbReminders
    }

    fun getReminders(): List<ReminderItem> {
        val activeId = getActivePatientId() ?: "p1"
        return getRemindersForPatient(activeId)
    }

    fun saveReminders(list: List<ReminderItem>) {
        val activeId = getActivePatientId() ?: "p1"
        for (item in list) {
            db.insertReminder(activeId, item)
        }
    }

    fun saveReminderItemForPatient(patientId: String, item: ReminderItem) {
        db.insertReminder(patientId, item)
    }

    fun saveReminderItem(item: ReminderItem) {
        val activeId = getActivePatientId() ?: "p1"
        db.insertReminder(activeId, item)
    }

    fun deleteReminderItemForPatient(patientId: String, reminderId: String) {
        db.deleteReminder(reminderId, patientId)
    }

    fun deleteReminderItem(id: String) {
        val activeId = getActivePatientId() ?: "p1"
        db.deleteReminder(id, activeId)
    }

    // ==========================================
    // 4. GAME SESSIONS & HISTORY (PATIENT ISOLATED)
    // ==========================================

    fun getGameSessionsForPatient(patientId: String): List<GameSessionRecord> {
        return db.getGameSessionsForPatient(patientId)
    }

    fun getGameSessions(): List<GameSessionRecord> {
        val activeId = getActivePatientId() ?: "p1"
        return getGameSessionsForPatient(activeId)
    }

    fun recordGameSessionForPatient(patientId: String, session: GameSessionRecord) {
        db.insertGameSession(patientId, session)
    }

    fun recordGameSession(session: GameSessionRecord) {
        val activeId = getActivePatientId() ?: "p1"
        db.insertGameSession(activeId, session)
    }

    // ==========================================
    // 5. DEMO MODE & ACCESSIBILITY SETTINGS
    // ==========================================

    fun getDemoModeInactiveDays(): Int {
        return prefs.getInt("demo_inactive_days", 0)
    }

    fun setDemoModeInactiveDays(days: Int) {
        prefs.edit(commit = true) {
            putInt("demo_inactive_days", days)
        }
    }

    fun getLanguage(): AppLanguage {
        val activePatient = getActivePatient()
        if (activePatient != null) {
            return activePatient.preferredLanguage
        }
        val langStr = prefs.getString("app_language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
        return try {
            AppLanguage.valueOf(langStr)
        } catch (_: Exception) {
            AppLanguage.ENGLISH
        }
    }

    fun setLanguage(lang: AppLanguage) {
        val activePatient = getActivePatient()
        if (activePatient != null) {
            savePatient(activePatient.copy(preferredLanguage = lang))
        }
        prefs.edit(commit = true) {
            putString("app_language", lang.name)
        }
    }

    fun getAccessibilitySettings(): AccessibilitySettings {
        return AccessibilitySettings(
            fontSizeScale = prefs.getFloat("acc_font_scale", 1.0f),
            highContrast = prefs.getBoolean("acc_high_contrast", false),
            voiceAssistantEnabled = prefs.getBoolean("acc_voice_assistant", false),
            reducedMotion = prefs.getBoolean("acc_reduced_motion", false),
            largeButtons = prefs.getBoolean("acc_large_buttons", true),
        )
    }

    fun saveAccessibilitySettings(settings: AccessibilitySettings) {
        prefs.edit(commit = true) {
            putFloat("acc_font_scale", settings.fontSizeScale)
            putBoolean("acc_high_contrast", settings.highContrast)
            putBoolean("acc_voice_assistant", settings.voiceAssistantEnabled)
            putBoolean("acc_reduced_motion", settings.reducedMotion)
            putBoolean("acc_large_buttons", settings.largeButtons)
        }
    }

    // ==========================================
    // 6. SONGS & LYRIC QUESTIONS (PATIENT ISOLATED)
    // ==========================================

    fun getSongsForPatient(patientId: String): List<SongItem> {
        return db.getSongsForPatient(patientId)
    }

    fun getQuestionsForSong(songId: String, patientId: String): List<LyricQuestionItem> {
        return db.getQuestionsForSong(songId, patientId)
    }

    fun saveSongForPatient(patientId: String, song: SongItem) {
        db.insertSong(song.copy(patientId = patientId))
    }

    fun saveLyricQuestionForPatient(patientId: String, question: LyricQuestionItem) {
        db.insertLyricQuestion(question.copy(patientId = patientId))
    }

    fun deleteSongForPatient(patientId: String, songId: String) {
        db.deleteSong(songId, patientId)
    }

    // ==========================================
    // 7. GAME IMAGE SETS & RANDOM SET SELECTION ENGINE
    // ==========================================

    fun getImageSetsForGame(patientId: String, gameType: GameType): List<GameImageSetItem> {
        return db.getImageSetsForGame(patientId, gameType)
    }

    fun getImagesForSet(patientId: String, gameType: GameType, setId: String): List<GameImageItem> {
        return db.getImagesForSet(patientId, gameType, setId)
    }

    fun saveGameImageSet(patientId: String, set: GameImageSetItem) {
        db.insertGameImageSet(set.copy(patientId = patientId))
    }

    fun saveGameImageToSet(patientId: String, image: GameImageItem) {
        db.insertGameImage(image.copy(patientId = patientId))
    }

    fun deleteGameImageSet(patientId: String, gameType: GameType, setId: String) {
        db.deleteGameImageSet(patientId, gameType, setId)
    }

    fun deleteGameImage(patientId: String, imageId: String) {
        db.deleteGameImage(patientId, imageId)
    }

    fun getRandomImageSet(patientId: String, gameType: GameType, minRequiredImages: Int): Pair<GameImageSetItem, List<GameImageItem>>? {
        val sets = getImageSetsForGame(patientId, gameType)
        val eligible = sets.map { set ->
            val imgs = getImagesForSet(patientId, gameType, set.setId)
            Pair(set, imgs)
        }.filter { it.second.size >= minRequiredImages }

        return if (eligible.isNotEmpty()) {
            eligible.shuffled().first()
        } else null
    }
}
