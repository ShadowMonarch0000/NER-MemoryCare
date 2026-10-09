package com.memorycare.ner

import android.content.Context
import android.net.Uri
import com.memorycare.ner.db.NERDatabase
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class MemoryItem(
    val id: String = UUID.randomUUID().toString(),
    val imagePath: String,
    val name: String,
    val category: String, // e.g. Family, Friend, Place, Route, Food, Festivals, Clothes
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("imagePath", imagePath)
        put("name", name)
        put("category", category)
        put("description", description)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJson(json: JSONObject): MemoryItem = MemoryItem(
            id = json.getString("id"),
            imagePath = json.getString("imagePath"),
            name = json.getString("name"),
            category = json.getString("category"),
            description = json.optString("description", ""),
            timestamp = json.optLong("timestamp", System.currentTimeMillis()),
        )
    }
}

class MemoryRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db = NERDatabase.getInstance(appContext)
    private val nerRepo = NERRepository(appContext)

    fun getMemoriesForPatient(patientId: String): List<MemoryItem> {
        val memories = db.getMemoriesForPatient(patientId)
        if (memories.isEmpty()) {
            val samples = listOf(
                MemoryItem(
                    id = "ne_1",
                    imagePath = "🏯",
                    name = "Kamakhya Temple, Guwahati",
                    category = "Route / Place",
                    description = "Historic temple landmark in Guwahati, Assam",
                ),
                MemoryItem(
                    id = "ne_2",
                    imagePath = "🦏",
                    name = "Kaziranga National Park",
                    category = "Route / Place",
                    description = "Famous rhino sanctuary in Assam",
                ),
                MemoryItem(
                    id = "ne_3",
                    imagePath = "🏞️",
                    name = "Loktak Lake, Manipur",
                    category = "Route / Place",
                    description = "Famous floating phumdis lake in Manipur",
                ),
                MemoryItem(
                    id = "ne_4",
                    imagePath = "🌧️",
                    name = "Cherrapunji Falls, Meghalaya",
                    category = "Route / Place",
                    description = "Beautiful waterfalls in East Khasi Hills",
                ),
                MemoryItem(
                    id = "ne_5",
                    imagePath = "🥟",
                    name = "Assamese Til Pitha",
                    category = "Food",
                    description = "Traditional sesame and jaggery sweet dish",
                ),
                MemoryItem(
                    id = "ne_6",
                    imagePath = "🍵",
                    name = "Assam Organic Tea Garden",
                    category = "Food",
                    description = "Freshly harvested Assam tea leaves",
                ),
                MemoryItem(
                    id = "ne_7",
                    imagePath = "🎉",
                    name = "Bihu Festival, Assam",
                    category = "Festivals",
                    description = "Spring harvest celebration in Assam",
                ),
                MemoryItem(
                    id = "ne_8",
                    imagePath = "🦅",
                    name = "Hornbill Festival, Nagaland",
                    category = "Festivals",
                    description = "Cultural festival of Naga heritage",
                ),
                MemoryItem(
                    id = "ne_9",
                    imagePath = "👗",
                    name = "Mekhela Chador, Assam",
                    category = "Clothes",
                    description = "Traditional Assamese silk attire",
                ),
                MemoryItem(
                    id = "ne_10",
                    imagePath = "🧣",
                    name = "Naga Traditional Shawl",
                    category = "Clothes",
                    description = "Handwoven tribal pattern shawl from Nagaland",
                ),
            )
            for (m in samples) {
                db.insertMemory(patientId, m)
            }
            return samples
        }
        return memories
    }

    fun getAllMemories(): List<MemoryItem> {
        val activePatientId = nerRepo.getActivePatientId() ?: "p1"
        return getMemoriesForPatient(activePatientId)
    }

    fun saveMemoryForPatient(patientId: String, item: MemoryItem) {
        db.insertMemory(patientId, item)
    }

    fun saveMemory(item: MemoryItem) {
        val activePatientId = nerRepo.getActivePatientId() ?: "p1"
        saveMemoryForPatient(activePatientId, item)
    }

    fun deleteMemory(id: String) {
        val activePatientId = nerRepo.getActivePatientId() ?: "p1"
        val memories = db.getMemoriesForPatient(activePatientId)
        val itemToDelete = memories.find { it.id == id }
        if (itemToDelete != null) {
            try {
                val file = File(itemToDelete.imagePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        db.deleteMemory(id, activePatientId)
    }

    fun saveImageToInternalStorage(sourceUri: Uri): String? {
        return try {
            val inputStream = appContext.contentResolver.openInputStream(sourceUri)
            val fileName = "memory_${System.currentTimeMillis()}.jpg"
            val file = File(appContext.filesDir, fileName)
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
