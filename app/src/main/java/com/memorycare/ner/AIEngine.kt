package com.memorycare.ner

/**
 * Performance Score Calculator
 * Calculates a normalized explainable performance score (0-100)
 * Weighted factors:
 * - Accuracy: 50%
 * - Mistakes penalty: 20%
 * - Time efficiency: 15%
 * - Completion status: 15%
 */
object PerformanceScoreCalculator {

    fun calculateScore(
        accuracy: Float,
        mistakes: Int,
        timeTakenSeconds: Int,
        isCompleted: Boolean,
    ): Int {
        val accuracyComponent = (accuracy.coerceIn(0f, 100f)) * 0.50f
        val mistakesComponent = ((100 - (mistakes * 10)).coerceIn(0, 100)) * 0.20f
        val timeComponent = ((100 - (timeTakenSeconds * 0.5f)).coerceIn(0f, 100f)) * 0.15f
        val completionComponent = (if (isCompleted) 100f else 0f) * 0.15f

        val total = accuracyComponent + mistakesComponent + timeComponent + completionComponent
        return total.toInt().coerceIn(0, 100)
    }
}

/**
 * Game 1: Flip & Match Difficulty Engine (Evaluated Separately)
 * Level 1: 2×2 Grid (2 pairs / 4 total cards)
 * Level 2: 2×4 Grid (4 pairs / 8 total cards)
 * Level 3: 3×4 Grid (6 pairs / 12 total cards)
 * Level 4: 4×4 Grid (8 pairs / 16 total cards)
 * Level 5: 4×5 Grid (10 pairs / 20 total cards)
 */
object FlipMatchDifficultyEngine {

    fun calculateFlipMatchLevel(sessions: List<GameSessionRecord>): AdaptiveDifficultyInfo {
        val flipSessions = sessions.filter { it.gameType == GameType.FLIP_MATCH }
            .sortedByDescending { it.completedAt }
            .take(5)

        if (flipSessions.isEmpty()) {
            return AdaptiveDifficultyInfo(
                gameType = GameType.FLIP_MATCH,
                level = 1,
                parameterDetail = "Grid: 2×2 (2 pairs)",
                currentDifficulty = DifficultyLevel.EASY,
                recommendedDifficulty = DifficultyLevel.EASY,
                recentAccuracy = 75f,
                recentMistakes = 1,
                recentCompletionRate = 100f,
                trend = "Stable",
                explanationReason = "Initial baseline. Set to Level 1 (2×2 grid).",
            )
        }

        val avgAccuracy = flipSessions.map { it.accuracy }.average().toFloat()
        val avgMistakes = flipSessions.map { it.mistakes }.average().toInt()
        val lastLevel = flipSessions.first().levelsCompleted.coerceIn(1, 5)

        var newLevel = lastLevel
        var trend = "Stable"
        var reason = "Performance is consistent with Level $lastLevel."

        if (flipSessions.size >= 2) {
            val recentAcc = flipSessions.take(2).map { it.accuracy }.average().toFloat()
            if (recentAcc >= 85f && avgMistakes <= 2) {
                trend = "Improving"
                if (lastLevel < 5) {
                    newLevel = lastLevel + 1
                    val gridText = getGridText(newLevel)
                    reason = "Level increased from $lastLevel to $newLevel ($gridText) because recent sessions showed consistently high accuracy (>=85%) with few wrong matches."
                } else {
                    reason = "Consistently high accuracy (>=85%). Patient has reached maximum Level 5 (4×5 grid)."
                }
            } else if (recentAcc < 60f || avgMistakes >= 5) {
                trend = "Needs support"
                if (lastLevel > 1) {
                    newLevel = lastLevel - 1
                    val gridText = getGridText(newLevel)
                    reason = "Level reduced from $lastLevel to $newLevel ($gridText) to provide a more comfortable, low-stress matching experience."
                } else {
                    reason = "Maintained at Level 1 (2×2 grid) for an encouraging baseline."
                }
            }
        }

        val levelDiff = when (newLevel) {
            1 -> DifficultyLevel.EASY
            2, 3 -> DifficultyLevel.MEDIUM
            else -> DifficultyLevel.HARD
        }

        return AdaptiveDifficultyInfo(
            gameType = GameType.FLIP_MATCH,
            level = newLevel,
            parameterDetail = "Grid: ${getGridText(newLevel)}",
            currentDifficulty = levelDiff,
            recommendedDifficulty = levelDiff,
            recentAccuracy = avgAccuracy,
            recentMistakes = avgMistakes,
            recentCompletionRate = 100f,
            trend = trend,
            explanationReason = reason,
        )
    }

    fun getGridText(level: Int): String = when (level) {
        1 -> "2×2 (2 pairs)"
        2 -> "2×4 (4 pairs)"
        3 -> "3×4 (6 pairs)"
        4 -> "4×4 (8 pairs)"
        5 -> "4×5 (10 pairs)"
        else -> "2×2 (2 pairs)"
    }
}

/**
 * Game 2: Remember My Route Difficulty Engine (Evaluated Separately)
 * Level 1: 3 Pictures Sequence
 * Level 2: 4 Pictures Sequence
 * Level 3: 5 Pictures Sequence
 * Level 4: 6 Pictures Sequence
 * Level 5: 7 Pictures Sequence
 */
object RouteMemoryDifficultyEngine {

    fun calculateRouteLevel(sessions: List<GameSessionRecord>): AdaptiveDifficultyInfo {
        val routeSessions = sessions.filter { it.gameType == GameType.REMEMBER_ROUTE }
            .sortedByDescending { it.completedAt }
            .take(5)

        if (routeSessions.isEmpty()) {
            return AdaptiveDifficultyInfo(
                gameType = GameType.REMEMBER_ROUTE,
                level = 2,
                parameterDetail = "Pictures: 4 Sequence",
                currentDifficulty = DifficultyLevel.MEDIUM,
                recommendedDifficulty = DifficultyLevel.MEDIUM,
                recentAccuracy = 80f,
                recentMistakes = 1,
                recentCompletionRate = 100f,
                trend = "Stable",
                explanationReason = "Initial baseline. Set to Level 2 (4 pictures).",
            )
        }

        val avgAccuracy = routeSessions.map { it.accuracy }.average().toFloat()
        val avgMistakes = routeSessions.map { it.mistakes }.average().toInt()
        val lastLevel = routeSessions.first().levelsCompleted.coerceIn(1, 5)

        var newLevel = lastLevel
        var trend = "Stable"
        var reason = "Performance is consistent with Level $lastLevel."

        if (routeSessions.size >= 2) {
            val recentAcc = routeSessions.take(2).map { it.accuracy }.average().toFloat()
            if (recentAcc >= 85f && avgMistakes <= 1) {
                trend = "Improving"
                if (lastLevel < 5) {
                    newLevel = lastLevel + 1
                    val pics = getPictureCount(newLevel)
                    reason = "Level increased from $lastLevel to $newLevel ($pics pictures) because recent sessions demonstrated high sequence accuracy."
                } else {
                    reason = "Consistently high accuracy (>=85%). Patient has reached maximum Level 5 (7 pictures)."
                }
            } else if (recentAcc < 60f || avgMistakes >= 3) {
                trend = "Needs support"
                if (lastLevel > 1) {
                    newLevel = lastLevel - 1
                    val pics = getPictureCount(newLevel)
                    reason = "Level reduced from $lastLevel to $newLevel ($pics pictures) to provide a more supportive landmark recall activity."
                } else {
                    reason = "Maintained at Level 1 (3 pictures) for baseline comfort."
                }
            }
        }

        val levelDiff = when (newLevel) {
            1 -> DifficultyLevel.EASY
            2, 3 -> DifficultyLevel.MEDIUM
            else -> DifficultyLevel.HARD
        }

        return AdaptiveDifficultyInfo(
            gameType = GameType.REMEMBER_ROUTE,
            level = newLevel,
            parameterDetail = "Pictures: ${getPictureCount(newLevel)}",
            currentDifficulty = levelDiff,
            recommendedDifficulty = levelDiff,
            recentAccuracy = avgAccuracy,
            recentMistakes = avgMistakes,
            recentCompletionRate = 100f,
            trend = trend,
            explanationReason = reason,
        )
    }

    fun getPictureCount(level: Int): Int = when (level) {
        1 -> 3
        2 -> 4
        3 -> 5
        4 -> 6
        5 -> 7
        else -> 4
    }
}

/**
 * Game 3: Spot the Repeat Difficulty Engine (Evaluated Separately)
 * Level 1: Speed 2.5s • 6 Pictures
 * Level 2: Speed 2.2s • 8 Pictures
 * Level 3: Speed 2.0s • 10 Pictures
 * Level 4: Speed 1.7s • 12 Pictures
 * Level 5: Speed 1.5s • 15 Pictures
 */
object SpotRepeatDifficultyEngine {

    fun calculateSpotRepeatLevel(sessions: List<GameSessionRecord>): AdaptiveDifficultyInfo {
        val spotSessions = sessions.filter { it.gameType == GameType.SPOT_REPEAT }
            .sortedByDescending { it.completedAt }
            .take(5)

        if (spotSessions.isEmpty()) {
            return AdaptiveDifficultyInfo(
                gameType = GameType.SPOT_REPEAT,
                level = 1,
                parameterDetail = "Speed: 2.5s • 6 Pictures",
                currentDifficulty = DifficultyLevel.EASY,
                recommendedDifficulty = DifficultyLevel.EASY,
                recentAccuracy = 75f,
                recentMistakes = 1,
                recentCompletionRate = 100f,
                trend = "Stable",
                explanationReason = "Initial baseline. Set to Level 1 (2.5s display speed).",
            )
        }

        val avgAccuracy = spotSessions.map { it.accuracy }.average().toFloat()
        val avgMistakes = spotSessions.map { it.mistakes }.average().toInt()
        val lastLevel = spotSessions.first().levelsCompleted.coerceIn(1, 5)

        var newLevel = lastLevel
        var trend = "Stable"
        var reason = "Performance is consistent with Level $lastLevel."

        if (spotSessions.size >= 2) {
            val recentAcc = spotSessions.take(2).map { it.accuracy }.average().toFloat()
            if (recentAcc >= 85f && avgMistakes <= 1) {
                trend = "Improving"
                if (lastLevel < 5) {
                    newLevel = lastLevel + 1
                    val speedText = getSpeedText(newLevel)
                    reason = "Level increased from $lastLevel to $newLevel ($speedText) because recent sessions demonstrated high repeat recognition accuracy."
                } else {
                    reason = "Consistently high accuracy (>=85%). Patient has reached maximum Level 5 (1.5s display speed)."
                }
            } else if (recentAcc < 60f || avgMistakes >= 3) {
                trend = "Needs support"
                if (lastLevel > 1) {
                    newLevel = lastLevel - 1
                    val speedText = getSpeedText(newLevel)
                    reason = "Level reduced from $lastLevel to $newLevel ($speedText) to provide a more supportive display pace."
                } else {
                    reason = "Maintained at Level 1 (2.5s display speed) for baseline comfort."
                }
            }
        }

        val levelDiff = when (newLevel) {
            1 -> DifficultyLevel.EASY
            2, 3 -> DifficultyLevel.MEDIUM
            else -> DifficultyLevel.HARD
        }

        return AdaptiveDifficultyInfo(
            gameType = GameType.SPOT_REPEAT,
            level = newLevel,
            parameterDetail = getSpeedText(newLevel),
            currentDifficulty = levelDiff,
            recommendedDifficulty = levelDiff,
            recentAccuracy = avgAccuracy,
            recentMistakes = avgMistakes,
            recentCompletionRate = 100f,
            trend = trend,
            explanationReason = reason,
        )
    }

    fun getDisplayDurationMillis(level: Int): Long = when (level) {
        1 -> 2500L
        2 -> 2200L
        3 -> 2000L
        4 -> 1700L
        5 -> 1500L
        else -> 2500L
    }

    fun getSequenceLength(level: Int): Int = when (level) {
        1 -> 6
        2 -> 8
        3 -> 10
        4 -> 12
        5 -> 15
        else -> 6
    }

    fun getSpeedText(level: Int): String = when (level) {
        1 -> "Speed: 2.5s • 6 Pictures"
        2 -> "Speed: 2.2s • 8 Pictures"
        3 -> "Speed: 2.0s • 10 Pictures"
        4 -> "Speed: 1.7s • 12 Pictures"
        5 -> "Speed: 1.5s • 15 Pictures"
        else -> "Speed: 2.5s • 6 Pictures"
    }
}

/**
 * Game 4: Guess the Lyrics Difficulty Engine (Evaluated Separately)
 */
object GuessLyricsDifficultyEngine {

    fun calculateGuessLyricsLevel(sessions: List<GameSessionRecord>): AdaptiveDifficultyInfo {
        val lyricsSessions = sessions.filter { it.gameType == GameType.GUESS_LYRICS }
            .sortedByDescending { it.completedAt }
            .take(5)

        if (lyricsSessions.isEmpty()) {
            return AdaptiveDifficultyInfo(
                gameType = GameType.GUESS_LYRICS,
                level = 1,
                parameterDetail = "Mode: Multiple Choice / Vocal Recall",
                currentDifficulty = DifficultyLevel.EASY,
                recommendedDifficulty = DifficultyLevel.EASY,
                recentAccuracy = 80f,
                recentMistakes = 0,
                recentCompletionRate = 100f,
                trend = "Stable",
                explanationReason = "Initial baseline. Set to Level 1.",
            )
        }

        val avgAccuracy = lyricsSessions.map { it.accuracy }.average().toFloat()
        val avgMistakes = lyricsSessions.map { it.mistakes }.average().toInt()
        val lastLevel = lyricsSessions.first().levelsCompleted.coerceIn(1, 5)

        var newLevel = lastLevel
        var trend = "Stable"
        var reason = "Performance is consistent with Level $lastLevel."

        if (lyricsSessions.size >= 2) {
            val recentAcc = lyricsSessions.take(2).map { it.accuracy }.average().toFloat()
            if (recentAcc >= 85f) {
                trend = "Improving"
                if (lastLevel < 5) {
                    newLevel = lastLevel + 1
                    reason = "Level increased to Level $newLevel due to high musical recall accuracy (>=85%)."
                } else {
                    reason = "Consistently high accuracy (>=85%). Reached maximum Level 5."
                }
            } else if (recentAcc < 60f) {
                trend = "Needs support"
                if (lastLevel > 1) {
                    newLevel = lastLevel - 1
                    reason = "Level reduced to Level $newLevel to provide supportive audio prompts."
                }
            }
        }

        val levelDiff = when (newLevel) {
            1 -> DifficultyLevel.EASY
            2, 3 -> DifficultyLevel.MEDIUM
            else -> DifficultyLevel.HARD
        }

        return AdaptiveDifficultyInfo(
            gameType = GameType.GUESS_LYRICS,
            level = newLevel,
            parameterDetail = "Mode: Multiple Choice / Vocal Recall",
            currentDifficulty = levelDiff,
            recommendedDifficulty = levelDiff,
            recentAccuracy = avgAccuracy,
            recentMistakes = avgMistakes,
            recentCompletionRate = 100f,
            trend = trend,
            explanationReason = reason,
        )
    }
}

/**
 * Adaptive Difficulty Engine (Unified Evaluator for Game Summaries)
 */
object AdaptiveDifficultyEngine {

    fun calculateDifficulty(
        gameType: GameType,
        recentSessions: List<GameSessionRecord>,
    ): AdaptiveDifficultyInfo {
        return when (gameType) {
            GameType.FLIP_MATCH -> FlipMatchDifficultyEngine.calculateFlipMatchLevel(recentSessions)
            GameType.REMEMBER_ROUTE -> RouteMemoryDifficultyEngine.calculateRouteLevel(recentSessions)
            GameType.SPOT_REPEAT -> SpotRepeatDifficultyEngine.calculateSpotRepeatLevel(recentSessions)
            GameType.GUESS_LYRICS -> GuessLyricsDifficultyEngine.calculateGuessLyricsLevel(recentSessions)
            else -> FlipMatchDifficultyEngine.calculateFlipMatchLevel(recentSessions)
        }
    }
}

/**
 * Non-Clinical Personal Cognitive Profile Generator
 */
object CognitiveProfileEngine {

    data class ProfileInsight(
        val category: String,
        val status: String, // "Strong", "Moderate", "Needs support"
        val note: String,
    )

    fun generateProfile(sessions: List<GameSessionRecord>): List<ProfileInsight> {
        val flipInfo = FlipMatchDifficultyEngine.calculateFlipMatchLevel(sessions)
        val routeInfo = RouteMemoryDifficultyEngine.calculateRouteLevel(sessions)
        val spotInfo = SpotRepeatDifficultyEngine.calculateSpotRepeatLevel(sessions)
        val lyricsInfo = GuessLyricsDifficultyEngine.calculateGuessLyricsLevel(sessions)

        val visualStatus = if (flipInfo.recentAccuracy >= 80f) "Strong" else if (flipInfo.recentAccuracy >= 60f) "Moderate" else "Needs support"
        val routeStatus = if (routeInfo.recentAccuracy >= 80f) "Strong" else if (routeInfo.recentAccuracy >= 60f) "Moderate" else "Needs support"
        val spotStatus = if (spotInfo.recentAccuracy >= 80f) "Strong" else if (spotInfo.recentAccuracy >= 60f) "Moderate" else "Needs support"
        val lyricsStatus = if (lyricsInfo.recentAccuracy >= 80f) "Strong" else if (lyricsInfo.recentAccuracy >= 60f) "Moderate" else "Needs support"

        return listOf(
            ProfileInsight("Visual Matching", visualStatus, "Level ${flipInfo.level} (${flipInfo.parameterDetail})"),
            ProfileInsight("Route Sequence Recall", routeStatus, "Level ${routeInfo.level} (${routeInfo.parameterDetail})"),
            ProfileInsight("Spot the Repeat Recognition", spotStatus, "Level ${spotInfo.level} (${spotInfo.parameterDetail})"),
            ProfileInsight("Guess the Lyrics Recall", lyricsStatus, "Level ${lyricsInfo.level} (${lyricsInfo.parameterDetail})"),
            ProfileInsight("Family Memory Recognition", "Strong", "Engagement with familiar caregiver-uploaded photos"),
        )
    }
}

/**
 * Activity Recommendation Engine
 */
object ActivityRecommendationEngine {

    data class ActivityRecommendation(
        val gameTitle: String,
        val recommendedLevelText: String,
        val reason: String,
    )

    fun getRecommendation(sessions: List<GameSessionRecord>): ActivityRecommendation {
        val flipInfo = FlipMatchDifficultyEngine.calculateFlipMatchLevel(sessions)
        val routeInfo = RouteMemoryDifficultyEngine.calculateRouteLevel(sessions)

        return if (flipInfo.recentAccuracy >= routeInfo.recentAccuracy) {
            ActivityRecommendation(
                gameTitle = "Flip & Match",
                recommendedLevelText = "Level ${flipInfo.level} (${flipInfo.parameterDetail})",
                reason = "Performance has been strong in visual matching activities.",
            )
        } else {
            ActivityRecommendation(
                gameTitle = "Remember My Route",
                recommendedLevelText = "Level ${routeInfo.level} (${routeInfo.parameterDetail})",
                reason = "Patient demonstrated high consistency in landmark sequence recall.",
            )
        }
    }
}

/**
 * Activity Monitor Engine
 * Detects inactivity locally and generates caregiver alerts.
 */
object ActivityMonitorEngine {

    fun analyzeActivity(
        recentSessions: List<GameSessionRecord>,
        reminders: List<ReminderItem>,
        demoInactiveDays: Int = 0,
    ): CaregiverAlertItem? {
        if (demoInactiveDays >= 4) {
            return CaregiverAlertItem(
                type = "INACTIVITY_ALERT",
                date = DailyGameRepository.getTodayDateString(),
                message = "No cognitive activity has been recorded for $demoInactiveDays consecutive days. Consider checking in with the patient.",
                severity = "HIGH",
            )
        } else if (demoInactiveDays == 3) {
            return CaregiverAlertItem(
                type = "INACTIVITY_ALERT",
                date = DailyGameRepository.getTodayDateString(),
                message = "Patient has not completed a cognitive activity for 3 days.",
                severity = "WARNING",
            )
        }

        val todayStr = DailyGameRepository.getTodayDateString()
        val hasActivityToday = recentSessions.any { it.date == todayStr }

        if (!hasActivityToday && recentSessions.isNotEmpty()) {
            val lastSessionDate = recentSessions.maxByOrNull { it.completedAt }?.date
            if (lastSessionDate != null && lastSessionDate != todayStr) {
                return CaregiverAlertItem(
                    type = "ACTIVITY_NOTICE",
                    date = todayStr,
                    message = "No cognitive activity recorded yet today. Consider encouraging the patient.",
                    severity = "INFO",
                )
            }
        }

        return null
    }

    fun generateWeeklyReport(
        recentSessions: List<GameSessionRecord>,
        reminders: List<ReminderItem>,
        demoInactiveDays: Int = 0,
    ): WeeklyAIReport {
        val activeDaysCount = if (demoInactiveDays > 0) (7 - demoInactiveDays).coerceAtLeast(1) else 6
        val streak = if (demoInactiveDays > 0) 0 else 4

        val routeDiff = RouteMemoryDifficultyEngine.calculateRouteLevel(recentSessions)
        val flipDiff = FlipMatchDifficultyEngine.calculateFlipMatchLevel(recentSessions)
        val spotDiff = SpotRepeatDifficultyEngine.calculateSpotRepeatLevel(recentSessions)
        val lyricsDiff = GuessLyricsDifficultyEngine.calculateGuessLyricsLevel(recentSessions)

        val alert = analyzeActivity(recentSessions, reminders, demoInactiveDays)

        val observation = if (demoInactiveDays >= 3) {
            "Overall app activity performance has decreased this week. Consider checking in."
        } else {
            "The patient maintained regular cognitive activity and steady completion of daily routines this week."
        }

        return WeeklyAIReport(
            activeDaysCount = activeDaysCount,
            totalGamesCompleted = if (demoInactiveDays >= 4) 2 else 10,
            currentStreakDays = streak,
            lastActiveDate = if (demoInactiveDays > 0) "$demoInactiveDays days ago" else "Today",
            gameSummaries = listOf(flipDiff, routeDiff, spotDiff, lyricsDiff),
            activityObservation = observation,
            alertMessage = alert?.message,
        )
    }
}
