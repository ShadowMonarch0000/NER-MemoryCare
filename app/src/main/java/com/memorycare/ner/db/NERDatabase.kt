package com.memorycare.ner.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.memorycare.ner.AppLanguage
import com.memorycare.ner.DifficultyLevel
import com.memorycare.ner.GameImageItem
import com.memorycare.ner.GameImageSetItem
import com.memorycare.ner.GameSessionRecord
import com.memorycare.ner.GameType
import com.memorycare.ner.LyricQuestionItem
import com.memorycare.ner.MemoryItem
import com.memorycare.ner.PatientProfile
import com.memorycare.ner.ReminderItem
import com.memorycare.ner.ReminderType
import com.memorycare.ner.SongItem

class NERDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // 1. PATIENTS TABLE
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS patients (
                patientId TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                age TEXT NOT NULL,
                condition TEXT NOT NULL,
                preferredLanguage TEXT NOT NULL,
                ttsEnabled INTEGER NOT NULL DEFAULT 1,
                fontSize REAL NOT NULL DEFAULT 1.0,
                largeButtonsEnabled INTEGER NOT NULL DEFAULT 1,
                avatarEmoji TEXT NOT NULL DEFAULT '🧓',
                inactiveDays INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // 2. MEMORIES TABLE (PATIENT ISOLATED)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS memories (
                memoryId TEXT PRIMARY KEY NOT NULL,
                patientId TEXT NOT NULL,
                imagePath TEXT NOT NULL,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 3. GAME HISTORY TABLE (PATIENT ISOLATED)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS game_history (
                sessionId TEXT PRIMARY KEY NOT NULL,
                patientId TEXT NOT NULL,
                gameType TEXT NOT NULL,
                date TEXT NOT NULL,
                startedAt INTEGER NOT NULL,
                completedAt INTEGER NOT NULL,
                difficulty TEXT NOT NULL,
                score INTEGER NOT NULL,
                accuracy REAL NOT NULL,
                mistakes INTEGER NOT NULL,
                attempts INTEGER NOT NULL,
                timeTakenSeconds INTEGER NOT NULL,
                levelsCompleted INTEGER NOT NULL,
                FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 4. REMINDERS TABLE (PATIENT ISOLATED)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS reminders (
                reminderId TEXT PRIMARY KEY NOT NULL,
                patientId TEXT NOT NULL,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                scheduledTime TEXT NOT NULL,
                repeatSchedule TEXT NOT NULL,
                enabled INTEGER NOT NULL DEFAULT 1,
                completedToday INTEGER NOT NULL DEFAULT 0,
                snoozedUntilTime INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 5. DEVICE SETTINGS TABLE
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS device_settings (
                id INTEGER PRIMARY KEY NOT NULL,
                currentPatientId TEXT,
                caregiverSetupCompleted INTEGER NOT NULL DEFAULT 0,
                caregiverPin TEXT NOT NULL DEFAULT '1234',
                demoInactiveDays INTEGER NOT NULL DEFAULT 0,
                appLanguage TEXT NOT NULL DEFAULT 'ENGLISH'
            )
            """.trimIndent()
        )

        // 6. SONGS TABLE (PATIENT ISOLATED)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS songs (
                songId TEXT PRIMARY KEY NOT NULL,
                patientId TEXT NOT NULL,
                title TEXT NOT NULL,
                artist TEXT,
                language TEXT NOT NULL DEFAULT 'ENGLISH',
                localFilePath TEXT,
                remoteFileUrl TEXT,
                createdAt INTEGER NOT NULL,
                syncStatus TEXT NOT NULL DEFAULT 'PENDING_SYNC',
                FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 7. LYRIC QUESTIONS TABLE (PATIENT ISOLATED)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS lyric_questions (
                questionId TEXT PRIMARY KEY NOT NULL,
                songId TEXT NOT NULL,
                patientId TEXT NOT NULL,
                prompt TEXT NOT NULL,
                correctAnswer TEXT NOT NULL,
                optionA TEXT NOT NULL,
                optionB TEXT NOT NULL,
                optionC TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 8. GAME IMAGE SETS TABLE (PATIENT & GAME ISOLATED)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS game_image_sets (
                setId TEXT PRIMARY KEY NOT NULL,
                patientId TEXT NOT NULL,
                gameType TEXT NOT NULL,
                setName TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 9. GAME IMAGES TABLE (SET & GAME ISOLATED)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS game_images (
                imageId TEXT PRIMARY KEY NOT NULL,
                patientId TEXT NOT NULL,
                gameType TEXT NOT NULL,
                setId TEXT NOT NULL,
                title TEXT NOT NULL,
                localFilePath TEXT NOT NULL,
                remoteImageUrl TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                syncStatus TEXT NOT NULL DEFAULT 'PENDING_SYNC',
                FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE,
                FOREIGN KEY(setId) REFERENCES game_image_sets(setId) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // SEED DEFAULT DATA
        seedDefaultPatients(db)
        seedDefaultDeviceSettings(db)
        seedDefaultSongsAndQuestions(db)
        seedDefaultGameImageSets(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE patients ADD COLUMN preferredLanguage TEXT DEFAULT 'ENGLISH'")
        }
        if (oldVersion < 3) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS songs (
                    songId TEXT PRIMARY KEY NOT NULL,
                    patientId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    artist TEXT,
                    language TEXT NOT NULL DEFAULT 'ENGLISH',
                    localFilePath TEXT,
                    remoteFileUrl TEXT,
                    createdAt INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL DEFAULT 'PENDING_SYNC'
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS lyric_questions (
                    questionId TEXT PRIMARY KEY NOT NULL,
                    songId TEXT NOT NULL,
                    patientId TEXT NOT NULL,
                    prompt TEXT NOT NULL,
                    correctAnswer TEXT NOT NULL,
                    optionA TEXT NOT NULL,
                    optionB TEXT NOT NULL,
                    optionC TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            seedDefaultSongsAndQuestions(db)
        }
        if (oldVersion < 4) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS game_image_sets (
                    setId TEXT PRIMARY KEY NOT NULL,
                    patientId TEXT NOT NULL,
                    gameType TEXT NOT NULL,
                    setName TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS game_images (
                    imageId TEXT PRIMARY KEY NOT NULL,
                    patientId TEXT NOT NULL,
                    gameType TEXT NOT NULL,
                    setId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    localFilePath TEXT NOT NULL,
                    remoteImageUrl TEXT NOT NULL DEFAULT '',
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL DEFAULT 'PENDING_SYNC'
                )
                """.trimIndent()
            )
            seedDefaultGameImageSets(db)
        }
        if (oldVersion < 5) {
            db.execSQL("UPDATE patients SET preferredLanguage = 'ENGLISH'")
            db.execSQL("UPDATE device_settings SET appLanguage = 'ENGLISH'")
        }
    }

    private fun seedDefaultPatients(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        val patients = listOf(
            PatientProfile(id = "p1", name = "Bhaben Sarma", age = "74", condition = "Early-stage Dementia (NER Region)", avatarEmoji = "🧓", inactiveDays = 0, preferredLanguage = AppLanguage.ENGLISH),
            PatientProfile(id = "p2", name = "Kamala Devi", age = "82", condition = "Mild Cognitive Impairment", avatarEmoji = "👵", inactiveDays = 0, preferredLanguage = AppLanguage.ENGLISH),
            PatientProfile(id = "p3", name = "Hemanta Gogoi", age = "68", condition = "Moderate Dementia", avatarEmoji = "👴", inactiveDays = 4, preferredLanguage = AppLanguage.ENGLISH),
        )

        for (p in patients) {
            val cv = ContentValues().apply {
                put("patientId", p.id)
                put("name", p.name)
                put("age", p.age)
                put("condition", p.condition)
                put("preferredLanguage", p.preferredLanguage.name)
                put("ttsEnabled", 1)
                put("fontSize", 1.0f)
                put("largeButtonsEnabled", 1)
                put("avatarEmoji", p.avatarEmoji)
                put("inactiveDays", p.inactiveDays)
                put("createdAt", now)
                put("updatedAt", now)
            }
            db.insertWithOnConflict("patients", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    private fun seedDefaultDeviceSettings(db: SQLiteDatabase) {
        val cv = ContentValues().apply {
            put("id", 1)
            put("currentPatientId", "p1")
            put("caregiverSetupCompleted", 1)
            put("caregiverPin", "1234")
            put("demoInactiveDays", 0)
            put("appLanguage", "ENGLISH")
        }
        db.insertWithOnConflict("device_settings", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun seedDefaultSongsAndQuestions(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        val sampleSongs = listOf(
            SongItem(
                songId = "s1",
                patientId = "p1",
                title = "Bihu Folk Rhythm",
                artist = "Assam Heritage",
                language = AppLanguage.ASSAMESE,
                localFilePath = "sample_bihu.mp3",
                createdAt = now,
            ),
            SongItem(
                songId = "s2",
                patientId = "p2",
                title = "Classic Hindi Melody",
                artist = "Folk Heritage",
                language = AppLanguage.HINDI,
                localFilePath = "sample_hindi.mp3",
                createdAt = now,
            ),
        )

        val sampleQuestions = listOf(
            LyricQuestionItem(
                questionId = "q1",
                songId = "s1",
                patientId = "p1",
                prompt = "What line comes after the Bihu dhol rhythm?",
                correctAnswer = "Koliya Bhauri and Til Pitha",
                optionA = "Koliya Bhauri and Til Pitha",
                optionB = "Winter snow on hills",
                optionC = "Evening tea garden",
                createdAt = now,
            ),
            LyricQuestionItem(
                questionId = "q2",
                songId = "s2",
                patientId = "p2",
                prompt = "Complete the classic refrain",
                correctAnswer = "Pyaar Hua Ikraar Hua",
                optionA = "Pyaar Hua Ikraar Hua",
                optionB = "Saagar Jaisi Aankhon Wali",
                optionC = "Chhod Do Aanchal Zamaana Kya Kahega",
                createdAt = now,
            ),
        )

        for (s in sampleSongs) {
            val cv = ContentValues().apply {
                put("songId", s.songId)
                put("patientId", s.patientId)
                put("title", s.title)
                put("artist", s.artist)
                put("language", s.language.name)
                put("localFilePath", s.localFilePath)
                put("remoteFileUrl", s.remoteFileUrl)
                put("createdAt", s.createdAt)
                put("syncStatus", s.syncStatus)
            }
            db.insertWithOnConflict("songs", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }

        for (q in sampleQuestions) {
            val cv = ContentValues().apply {
                put("questionId", q.questionId)
                put("songId", q.songId)
                put("patientId", q.patientId)
                put("prompt", q.prompt)
                put("correctAnswer", q.correctAnswer)
                put("optionA", q.optionA)
                put("optionB", q.optionB)
                put("optionC", q.optionC)
                put("createdAt", q.createdAt)
            }
            db.insertWithOnConflict("lyric_questions", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    private fun seedDefaultGameImageSets(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        val defaultSets = listOf(
            GameImageSetItem("set_fm_1", "p1", GameType.FLIP_MATCH, "Set 1: Food & Festivals", now, now),
            GameImageSetItem("set_fm_2", "p1", GameType.FLIP_MATCH, "Set 2: Traditional Attire", now, now),
            GameImageSetItem("set_route_1", "p1", GameType.REMEMBER_ROUTE, "Set 1: Family Homes & Temples", now, now),
            GameImageSetItem("set_route_2", "p1", GameType.REMEMBER_ROUTE, "Set 2: Local Markets & Parks", now, now),
            GameImageSetItem("set_spot_1", "p1", GameType.SPOT_REPEAT, "Set 1: Cultural Artifacts", now, now),
            GameImageSetItem("set_spot_2", "p1", GameType.SPOT_REPEAT, "Set 2: Regional Flora & Animals", now, now),
        )

        for (s in defaultSets) {
            val cv = ContentValues().apply {
                put("setId", s.setId)
                put("patientId", s.patientId)
                put("gameType", s.gameType.name)
                put("setName", s.setName)
                put("createdAt", s.createdAt)
                put("updatedAt", s.updatedAt)
            }
            db.insertWithOnConflict("game_image_sets", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }

        val defaultImages = listOf(
            // Flip & Match Set 1
            GameImageItem("img_1", "p1", GameType.FLIP_MATCH, "set_fm_1", "Assamese Til Pitha", "🥟", "", now, now),
            GameImageItem("img_2", "p1", GameType.FLIP_MATCH, "set_fm_1", "Assam Organic Tea Garden", "🍵", "", now, now),
            GameImageItem("img_3", "p1", GameType.FLIP_MATCH, "set_fm_1", "Bihu Festival", "🎉", "", now, now),
            GameImageItem("img_4", "p1", GameType.FLIP_MATCH, "set_fm_1", "Hornbill Festival", "🦅", "", now, now),

            // Flip & Match Set 2
            GameImageItem("img_5", "p1", GameType.FLIP_MATCH, "set_fm_2", "Mekhela Chador", "👗", "", now, now),
            GameImageItem("img_6", "p1", GameType.FLIP_MATCH, "set_fm_2", "Naga Shawl", "🧣", "", now, now),
            GameImageItem("img_7", "p1", GameType.FLIP_MATCH, "set_fm_2", "Bamboo Craft", "🎍", "", now, now),
            GameImageItem("img_8", "p1", GameType.FLIP_MATCH, "set_fm_2", "Eri Silk Weave", "🧵", "", now, now),

            // Remember Route Set 1
            GameImageItem("img_9", "p1", GameType.REMEMBER_ROUTE, "set_route_1", "Kamakhya Temple", "🏯", "", now, now),
            GameImageItem("img_10", "p1", GameType.REMEMBER_ROUTE, "set_route_1", "Family Home", "🏡", "", now, now),
            GameImageItem("img_11", "p1", GameType.REMEMBER_ROUTE, "set_route_1", "Kaziranga Park", "🦏", "", now, now),
            GameImageItem("img_12", "p1", GameType.REMEMBER_ROUTE, "set_route_1", "Loktak Lake", "🏞️", "", now, now),

            // Remember Route Set 2
            GameImageItem("img_13", "p1", GameType.REMEMBER_ROUTE, "set_route_2", "Local Market", "🛒", "", now, now),
            GameImageItem("img_14", "p1", GameType.REMEMBER_ROUTE, "set_route_2", "Health Clinic", "🏥", "", now, now),
            GameImageItem("img_15", "p1", GameType.REMEMBER_ROUTE, "set_route_2", "Community Garden", "🌺", "", now, now),
            GameImageItem("img_16", "p1", GameType.REMEMBER_ROUTE, "set_route_2", "Bus Station", "🚌", "", now, now),

            // Spot Repeat Set 1
            GameImageItem("img_17", "p1", GameType.SPOT_REPEAT, "set_spot_1", "Jaapi Hat", "👒", "", now, now),
            GameImageItem("img_18", "p1", GameType.SPOT_REPEAT, "set_spot_1", "Xorai Vessel", "🏆", "", now, now),
            GameImageItem("img_19", "p1", GameType.SPOT_REPEAT, "set_spot_1", "Gamosa Towel", "🧣", "", now, now),
            GameImageItem("img_20", "p1", GameType.SPOT_REPEAT, "set_spot_1", "Dhol Drum", "🥁", "", now, now),

            // Spot Repeat Set 2
            GameImageItem("img_21", "p1", GameType.SPOT_REPEAT, "set_spot_2", "One-Horned Rhino", "🦏", "", now, now),
            GameImageItem("img_22", "p1", GameType.SPOT_REPEAT, "set_spot_2", "Orchid Flower", "🌸", "", now, now),
            GameImageItem("img_23", "p1", GameType.SPOT_REPEAT, "set_spot_2", "Brahmaputra River", "🌊", "", now, now),
            GameImageItem("img_24", "p1", GameType.SPOT_REPEAT, "set_spot_2", "Red Panda", "🐼", "", now, now),
        )

        for (img in defaultImages) {
            val cv = ContentValues().apply {
                put("imageId", img.imageId)
                put("patientId", img.patientId)
                put("gameType", img.gameType.name)
                put("setId", img.setId)
                put("title", img.title)
                put("localFilePath", img.localFilePath)
                put("remoteImageUrl", img.remoteImageUrl)
                put("createdAt", img.createdAt)
                put("updatedAt", img.updatedAt)
                put("syncStatus", img.syncStatus)
            }
            db.insertWithOnConflict("game_images", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    // ==========================================
    // PATIENT DAO METHODS
    // ==========================================

    fun getAllPatients(): List<PatientProfile> {
        val list = mutableListOf<PatientProfile>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM patients ORDER BY createdAt ASC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(c.getColumnIndexOrThrow("patientId"))
                val name = c.getString(c.getColumnIndexOrThrow("name"))
                val age = c.getString(c.getColumnIndexOrThrow("age"))
                val condition = c.getString(c.getColumnIndexOrThrow("condition"))
                val emoji = c.getString(c.getColumnIndexOrThrow("avatarEmoji"))
                val inactive = c.getInt(c.getColumnIndexOrThrow("inactiveDays"))
                val langStr = c.getString(c.getColumnIndexOrThrow("preferredLanguage"))
                val lang = try { AppLanguage.valueOf(langStr) } catch (_: Exception) { AppLanguage.ENGLISH }

                list.add(
                    PatientProfile(
                        id = id,
                        name = name,
                        age = age,
                        condition = condition,
                        avatarEmoji = emoji,
                        inactiveDays = inactive,
                        preferredLanguage = lang,
                    )
                )
            }
        }
        return list
    }

    fun getPatientById(patientId: String): PatientProfile? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM patients WHERE patientId = ?", arrayOf(patientId))
        cursor.use { c ->
            if (c.moveToFirst()) {
                val id = c.getString(c.getColumnIndexOrThrow("patientId"))
                val name = c.getString(c.getColumnIndexOrThrow("name"))
                val age = c.getString(c.getColumnIndexOrThrow("age"))
                val condition = c.getString(c.getColumnIndexOrThrow("condition"))
                val emoji = c.getString(c.getColumnIndexOrThrow("avatarEmoji"))
                val inactive = c.getInt(c.getColumnIndexOrThrow("inactiveDays"))
                val langStr = c.getString(c.getColumnIndexOrThrow("preferredLanguage"))
                val lang = try { AppLanguage.valueOf(langStr) } catch (_: Exception) { AppLanguage.ENGLISH }

                return PatientProfile(
                    id = id,
                    name = name,
                    age = age,
                    condition = condition,
                    avatarEmoji = emoji,
                    inactiveDays = inactive,
                    preferredLanguage = lang,
                )
            }
        }
        return null
    }

    fun insertOrUpdatePatient(patient: PatientProfile) {
        val db = writableDatabase
        val now = System.currentTimeMillis()
        val cv = ContentValues().apply {
            put("patientId", patient.id)
            put("name", patient.name)
            put("age", patient.age)
            put("condition", patient.condition)
            put("preferredLanguage", patient.preferredLanguage.name)
            put("ttsEnabled", 1)
            put("fontSize", 1.0f)
            put("largeButtonsEnabled", 1)
            put("avatarEmoji", patient.avatarEmoji)
            put("inactiveDays", patient.inactiveDays)
            put("createdAt", now)
            put("updatedAt", now)
        }
        db.insertWithOnConflict("patients", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deletePatient(patientId: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("patients", "patientId = ?", arrayOf(patientId))
            db.delete("memories", "patientId = ?", arrayOf(patientId))
            db.delete("game_history", "patientId = ?", arrayOf(patientId))
            db.delete("reminders", "patientId = ?", arrayOf(patientId))
            db.delete("songs", "patientId = ?", arrayOf(patientId))
            db.delete("lyric_questions", "patientId = ?", arrayOf(patientId))
            db.delete("game_image_sets", "patientId = ?", arrayOf(patientId))
            db.delete("game_images", "patientId = ?", arrayOf(patientId))

            val currentActive = getCurrentPatientId()
            if (currentActive == patientId) {
                setCurrentPatientId(null)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ==========================================
    // DEVICE SETTINGS DAO METHODS
    // ==========================================

    fun getCurrentPatientId(): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT currentPatientId FROM device_settings WHERE id = 1", null)
        cursor.use { c ->
            if (c.moveToFirst()) {
                return c.getString(0)
            }
        }
        return null
    }

    fun setCurrentPatientId(patientId: String?) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("id", 1)
            put("currentPatientId", patientId)
        }
        db.update("device_settings", cv, "id = 1", null)
    }

    fun isCaregiverSetupCompleted(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT caregiverSetupCompleted FROM device_settings WHERE id = 1", null)
        cursor.use { c ->
            if (c.moveToFirst()) {
                return c.getInt(0) == 1
            }
        }
        return false
    }

    fun setCaregiverSetupCompleted(completed: Boolean) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("id", 1)
            put("caregiverSetupCompleted", if (completed) 1 else 0)
        }
        db.update("device_settings", cv, "id = 1", null)
    }

    // ==========================================
    // MEMORY DAO METHODS (PATIENT ISOLATED)
    // ==========================================

    fun getMemoriesForPatient(patientId: String): List<MemoryItem> {
        val list = mutableListOf<MemoryItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM memories WHERE patientId = ? ORDER BY createdAt DESC", arrayOf(patientId))
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(c.getColumnIndexOrThrow("memoryId"))
                val path = c.getString(c.getColumnIndexOrThrow("imagePath"))
                val title = c.getString(c.getColumnIndexOrThrow("title"))
                val cat = c.getString(c.getColumnIndexOrThrow("category"))
                val desc = c.getString(c.getColumnIndexOrThrow("description")) ?: ""
                val ts = c.getLong(c.getColumnIndexOrThrow("createdAt"))

                list.add(
                    MemoryItem(
                        id = id,
                        imagePath = path,
                        name = title,
                        category = cat,
                        description = desc,
                        timestamp = ts,
                    )
                )
            }
        }
        return list
    }

    fun insertMemory(patientId: String, memory: MemoryItem) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("memoryId", memory.id)
            put("patientId", patientId)
            put("imagePath", memory.imagePath)
            put("title", memory.name)
            put("category", memory.category)
            put("description", memory.description)
            put("createdAt", memory.timestamp)
        }
        db.insertWithOnConflict("memories", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteMemory(memoryId: String, patientId: String) {
        val db = writableDatabase
        db.delete("memories", "memoryId = ? AND patientId = ?", arrayOf(memoryId, patientId))
    }

    // ==========================================
    // GAME IMAGE SETS & GAME IMAGES DAO METHODS
    // ==========================================

    fun getImageSetsForGame(patientId: String, gameType: GameType): List<GameImageSetItem> {
        val list = mutableListOf<GameImageSetItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM game_image_sets WHERE patientId = ? AND gameType = ? ORDER BY createdAt ASC", arrayOf(patientId, gameType.name))
        cursor.use { c ->
            while (c.moveToNext()) {
                val setId = c.getString(c.getColumnIndexOrThrow("setId"))
                val pId = c.getString(c.getColumnIndexOrThrow("patientId"))
                val gTypeStr = c.getString(c.getColumnIndexOrThrow("gameType"))
                val setName = c.getString(c.getColumnIndexOrThrow("setName"))
                val created = c.getLong(c.getColumnIndexOrThrow("createdAt"))
                val updated = c.getLong(c.getColumnIndexOrThrow("updatedAt"))

                val gType = try { GameType.valueOf(gTypeStr) } catch (_: Exception) { gameType }

                list.add(
                    GameImageSetItem(
                        setId = setId,
                        patientId = pId,
                        gameType = gType,
                        setName = setName,
                        createdAt = created,
                        updatedAt = updated,
                    )
                )
            }
        }
        return list
    }

    fun getImagesForSet(patientId: String, gameType: GameType, setId: String): List<GameImageItem> {
        val list = mutableListOf<GameImageItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM game_images WHERE patientId = ? AND gameType = ? AND setId = ? ORDER BY createdAt ASC", arrayOf(patientId, gameType.name, setId))
        cursor.use { c ->
            while (c.moveToNext()) {
                val imgId = c.getString(c.getColumnIndexOrThrow("imageId"))
                val pId = c.getString(c.getColumnIndexOrThrow("patientId"))
                val gTypeStr = c.getString(c.getColumnIndexOrThrow("gameType"))
                val sId = c.getString(c.getColumnIndexOrThrow("setId"))
                val title = c.getString(c.getColumnIndexOrThrow("title"))
                val localPath = c.getString(c.getColumnIndexOrThrow("localFilePath"))
                val remoteUrl = c.getString(c.getColumnIndexOrThrow("remoteImageUrl")) ?: ""
                val created = c.getLong(c.getColumnIndexOrThrow("createdAt"))
                val updated = c.getLong(c.getColumnIndexOrThrow("updatedAt"))
                val sync = c.getString(c.getColumnIndexOrThrow("syncStatus")) ?: "PENDING_SYNC"

                val gType = try { GameType.valueOf(gTypeStr) } catch (_: Exception) { gameType }

                list.add(
                    GameImageItem(
                        imageId = imgId,
                        patientId = pId,
                        gameType = gType,
                        setId = sId,
                        title = title,
                        localFilePath = localPath,
                        remoteImageUrl = remoteUrl,
                        createdAt = created,
                        updatedAt = updated,
                        syncStatus = sync,
                    )
                )
            }
        }
        return list
    }

    fun insertGameImageSet(set: GameImageSetItem) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("setId", set.setId)
            put("patientId", set.patientId)
            put("gameType", set.gameType.name)
            put("setName", set.setName)
            put("createdAt", set.createdAt)
            put("updatedAt", set.updatedAt)
        }
        db.insertWithOnConflict("game_image_sets", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertGameImage(image: GameImageItem) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("imageId", image.imageId)
            put("patientId", image.patientId)
            put("gameType", image.gameType.name)
            put("setId", image.setId)
            put("title", image.title)
            put("localFilePath", image.localFilePath)
            put("remoteImageUrl", image.remoteImageUrl)
            put("createdAt", image.createdAt)
            put("updatedAt", image.updatedAt)
            put("syncStatus", image.syncStatus)
        }
        db.insertWithOnConflict("game_images", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteGameImageSet(patientId: String, gameType: GameType, setId: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("game_image_sets", "patientId = ? AND gameType = ? AND setId = ?", arrayOf(patientId, gameType.name, setId))
            db.delete("game_images", "patientId = ? AND gameType = ? AND setId = ?", arrayOf(patientId, gameType.name, setId))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteGameImage(patientId: String, imageId: String) {
        val db = writableDatabase
        db.delete("game_images", "patientId = ? AND imageId = ?", arrayOf(patientId, imageId))
    }

    // ==========================================
    // SONGS & LYRIC QUESTIONS DAO METHODS
    // ==========================================

    fun getSongsForPatient(patientId: String): List<SongItem> {
        val list = mutableListOf<SongItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM songs WHERE patientId = ? ORDER BY createdAt DESC", arrayOf(patientId))
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(c.getColumnIndexOrThrow("songId"))
                val pId = c.getString(c.getColumnIndexOrThrow("patientId"))
                val title = c.getString(c.getColumnIndexOrThrow("title"))
                val artist = c.getString(c.getColumnIndexOrThrow("artist")) ?: ""
                val langStr = c.getString(c.getColumnIndexOrThrow("language"))
                val localPath = c.getString(c.getColumnIndexOrThrow("localFilePath")) ?: ""
                val remoteUrl = c.getString(c.getColumnIndexOrThrow("remoteFileUrl")) ?: ""
                val created = c.getLong(c.getColumnIndexOrThrow("createdAt"))
                val sync = c.getString(c.getColumnIndexOrThrow("syncStatus")) ?: "PENDING_SYNC"

                val lang = try { AppLanguage.valueOf(langStr) } catch (_: Exception) { AppLanguage.ENGLISH }

                list.add(
                    SongItem(
                        songId = id,
                        patientId = pId,
                        title = title,
                        artist = artist,
                        language = lang,
                        localFilePath = localPath,
                        remoteFileUrl = remoteUrl,
                        createdAt = created,
                        syncStatus = sync,
                    )
                )
            }
        }
        return list
    }

    fun getQuestionsForSong(songId: String, patientId: String): List<LyricQuestionItem> {
        val list = mutableListOf<LyricQuestionItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM lyric_questions WHERE songId = ? AND patientId = ? ORDER BY createdAt ASC", arrayOf(songId, patientId))
        cursor.use { c ->
            while (c.moveToNext()) {
                val qId = c.getString(c.getColumnIndexOrThrow("questionId"))
                val sId = c.getString(c.getColumnIndexOrThrow("songId"))
                val pId = c.getString(c.getColumnIndexOrThrow("patientId"))
                val prompt = c.getString(c.getColumnIndexOrThrow("prompt"))
                val correct = c.getString(c.getColumnIndexOrThrow("correctAnswer"))
                val optA = c.getString(c.getColumnIndexOrThrow("optionA"))
                val optB = c.getString(c.getColumnIndexOrThrow("optionB"))
                val optC = c.getString(c.getColumnIndexOrThrow("optionC"))
                val created = c.getLong(c.getColumnIndexOrThrow("createdAt"))

                list.add(
                    LyricQuestionItem(
                        questionId = qId,
                        songId = sId,
                        patientId = pId,
                        prompt = prompt,
                        correctAnswer = correct,
                        optionA = optA,
                        optionB = optB,
                        optionC = optC,
                        createdAt = created,
                    )
                )
            }
        }
        return list
    }

    fun insertSong(song: SongItem) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("songId", song.songId)
            put("patientId", song.patientId)
            put("title", song.title)
            put("artist", song.artist)
            put("language", song.language.name)
            put("localFilePath", song.localFilePath)
            put("remoteFileUrl", song.remoteFileUrl)
            put("createdAt", song.createdAt)
            put("syncStatus", song.syncStatus)
        }
        db.insertWithOnConflict("songs", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertLyricQuestion(question: LyricQuestionItem) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("questionId", question.questionId)
            put("songId", question.songId)
            put("patientId", question.patientId)
            put("prompt", question.prompt)
            put("correctAnswer", question.correctAnswer)
            put("optionA", question.optionA)
            put("optionB", question.optionB)
            put("optionC", question.optionC)
            put("createdAt", question.createdAt)
        }
        db.insertWithOnConflict("lyric_questions", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteSong(songId: String, patientId: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("songs", "songId = ? AND patientId = ?", arrayOf(songId, patientId))
            db.delete("lyric_questions", "songId = ? AND patientId = ?", arrayOf(songId, patientId))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ==========================================
    // GAME HISTORY DAO METHODS (PATIENT ISOLATED)
    // ==========================================

    fun getGameSessionsForPatient(patientId: String): List<GameSessionRecord> {
        val list = mutableListOf<GameSessionRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM game_history WHERE patientId = ? ORDER BY completedAt DESC", arrayOf(patientId))
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(c.getColumnIndexOrThrow("sessionId"))
                val gTypeStr = c.getString(c.getColumnIndexOrThrow("gameType"))
                val date = c.getString(c.getColumnIndexOrThrow("date"))
                val start = c.getLong(c.getColumnIndexOrThrow("startedAt"))
                val comp = c.getLong(c.getColumnIndexOrThrow("completedAt"))
                val diffStr = c.getString(c.getColumnIndexOrThrow("difficulty"))
                val score = c.getInt(c.getColumnIndexOrThrow("score"))
                val acc = c.getFloat(c.getColumnIndexOrThrow("accuracy"))
                val mist = c.getInt(c.getColumnIndexOrThrow("mistakes"))
                val att = c.getInt(c.getColumnIndexOrThrow("attempts"))
                val time = c.getInt(c.getColumnIndexOrThrow("timeTakenSeconds"))
                val levels = c.getInt(c.getColumnIndexOrThrow("levelsCompleted"))

                val gType = try { GameType.valueOf(gTypeStr) } catch (_: Exception) { GameType.FLIP_MATCH }
                val diff = try { DifficultyLevel.valueOf(diffStr) } catch (_: Exception) { DifficultyLevel.MEDIUM }

                list.add(
                    GameSessionRecord(
                        id = id,
                        gameType = gType,
                        date = date,
                        startedAt = start,
                        completedAt = comp,
                        difficulty = diff,
                        score = score,
                        accuracy = acc,
                        mistakes = mist,
                        attempts = att,
                        timeTakenSeconds = time,
                        levelsCompleted = levels,
                    )
                )
            }
        }
        return list
    }

    fun insertGameSession(patientId: String, session: GameSessionRecord) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("sessionId", session.id)
            put("patientId", patientId)
            put("gameType", session.gameType.name)
            put("date", session.date)
            put("startedAt", session.startedAt)
            put("completedAt", session.completedAt)
            put("difficulty", session.difficulty.name)
            put("score", session.score)
            put("accuracy", session.accuracy)
            put("mistakes", session.mistakes)
            put("attempts", session.attempts)
            put("timeTakenSeconds", session.timeTakenSeconds)
            put("levelsCompleted", session.levelsCompleted)
        }
        db.insertWithOnConflict("game_history", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // ==========================================
    // REMINDERS DAO METHODS (PATIENT ISOLATED)
    // ==========================================

    fun getRemindersForPatient(patientId: String): List<ReminderItem> {
        val list = mutableListOf<ReminderItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM reminders WHERE patientId = ? ORDER BY scheduledTime ASC", arrayOf(patientId))
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(c.getColumnIndexOrThrow("reminderId"))
                val typeStr = c.getString(c.getColumnIndexOrThrow("type"))
                val title = c.getString(c.getColumnIndexOrThrow("title"))
                val msg = c.getString(c.getColumnIndexOrThrow("message"))
                val time = c.getString(c.getColumnIndexOrThrow("scheduledTime"))
                val sched = c.getString(c.getColumnIndexOrThrow("repeatSchedule"))
                val enabled = c.getInt(c.getColumnIndexOrThrow("enabled")) == 1
                val completed = c.getInt(c.getColumnIndexOrThrow("completedToday")) == 1
                val snoozed = c.getLong(c.getColumnIndexOrThrow("snoozedUntilTime"))

                val type = try { ReminderType.valueOf(typeStr) } catch (_: Exception) { ReminderType.MEDICINE }

                list.add(
                    ReminderItem(
                        id = id,
                        type = type,
                        title = title,
                        message = msg,
                        scheduledTime = time,
                        repeatSchedule = sched,
                        enabled = enabled,
                        completedToday = completed,
                        snoozedUntilTime = snoozed,
                    )
                )
            }
        }
        return list
    }

    fun insertReminder(patientId: String, reminder: ReminderItem) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("reminderId", reminder.id)
            put("patientId", patientId)
            put("type", reminder.type.name)
            put("title", reminder.title)
            put("message", reminder.message)
            put("scheduledTime", reminder.scheduledTime)
            put("repeatSchedule", reminder.repeatSchedule)
            put("enabled", if (reminder.enabled) 1 else 0)
            put("completedToday", if (reminder.completedToday) 1 else 0)
            put("snoozedUntilTime", reminder.snoozedUntilTime)
        }
        db.insertWithOnConflict("reminders", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteReminder(reminderId: String, patientId: String) {
        val db = writableDatabase
        db.delete("reminders", "reminderId = ? AND patientId = ?", arrayOf(reminderId, patientId))
    }

    companion object {
        private const val DATABASE_NAME = "ner_memorycare_offline.db"
        private const val DATABASE_VERSION = 5

        @Volatile
        private var instance: NERDatabase? = null

        fun getInstance(context: Context): NERDatabase {
            return instance ?: synchronized(this) {
                instance ?: NERDatabase(context.applicationContext).also { instance = it }
            }
        }
    }
}
