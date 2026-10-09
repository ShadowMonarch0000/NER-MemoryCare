package com.memorycare.ner

import android.content.Context
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

data class MemoryCard(
    val id: Int, // unique card index on grid
    val memoryId: String,
    val name: String,
    val category: String, // FOOD, CLOTHES, FESTIVAL, etc.
    val imagePath: String, // local file path or emoji representation if path is empty
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false,
)

data class GameResult(
    val date: String,
    val timeTakenSeconds: Int,
    val moves: Int,
    val pairsMatched: Int,
    val totalPairs: Int,
    val accuracy: Float,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("date", date)
        put("timeTakenSeconds", timeTakenSeconds)
        put("moves", moves)
        put("pairsMatched", pairsMatched)
        put("totalPairs", totalPairs)
        put("accuracy", accuracy.toDouble())
    }

    companion object {
        fun fromJson(json: JSONObject): GameResult = GameResult(
            date = json.getString("date"),
            timeTakenSeconds = json.getInt("timeTakenSeconds"),
            moves = json.getInt("moves"),
            pairsMatched = json.getInt("pairsMatched"),
            totalPairs = json.getInt("totalPairs"),
            accuracy = json.getDouble("accuracy").toFloat(),
        )
    }
}

object DailyGameRepository {
    private const val PREF_NAME = "ner_daily_game_prefs"

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun isTodayCompleted(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getString("completed_date", "")
        return lastDate == getTodayDateString()
    }

    fun getTodayResult(context: Context): GameResult? {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("today_result_json", null) ?: return null
        return try {
            GameResult.fromJson(JSONObject(jsonStr))
        } catch (_: Exception) {
            null
        }
    }

    fun saveCompletedResult(context: Context, result: GameResult) {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit(commit = true) {
            putString("completed_date", getTodayDateString())
            putString("today_result_json", result.toJson().toString())
        }
    }
}

object DefaultMemoryProvider {
    data class DefaultMemory(
        val name: String,
        val category: String,
        val emojiOrAsset: String,
    )

    val items = listOf(
        DefaultMemory("Assamese Til Pitha", "FOOD", "🥟"),
        DefaultMemory("Local Fish Curry", "FOOD", "🍛"),
        DefaultMemory("Assam Organic Tea", "FOOD", "🍵"),
        DefaultMemory("Bamboo Shoot Curry", "FOOD", "🎍"),
        DefaultMemory("Mekhela Chador, Assam", "CLOTHES", "👗"),
        DefaultMemory("Phanek, Manipur", "CLOTHES", "👘"),
        DefaultMemory("Naga Tribal Shawl", "CLOTHES", "🧣"),
        DefaultMemory("Risa, Tripura", "CLOTHES", "👚"),
        DefaultMemory("Bihu Festival, Assam", "FESTIVAL", "🎉"),
        DefaultMemory("Hornbill Festival, Nagaland", "FESTIVAL", "🦅"),
        DefaultMemory("Sangai Festival, Manipur", "FESTIVAL", "🦌"),
        DefaultMemory("Losoong Festival, Sikkim", "FESTIVAL", "🌸"),
        DefaultMemory("Kamakhya Temple, Assam", "PLACE", "🏯"),
        DefaultMemory("Kaziranga National Park", "PLACE", "🦏"),
        DefaultMemory("Loktak Lake, Manipur", "PLACE", "🏞️"),
        DefaultMemory("Cherrapunji Waterfalls", "PLACE", "🌧️"),
        DefaultMemory("Tawang Monastery, Arunachal", "PLACE", "🏔️"),
        DefaultMemory("Ujjayanta Palace, Tripura", "PLACE", "🏰"),
    )
}
