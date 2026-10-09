package com.memorycare.ner

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorycare.ner.voice.TextToSpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// ==========================================
// 1. GAME INTRO / SELECTION SCREEN
// ==========================================

@Composable
fun GameIntroScreen(
    onBackClick: () -> Unit,
    onStartGameClick: () -> Unit,
) {
    val context = LocalContext.current
    val isCompletedToday = remember { DailyGameRepository.isTodayCompleted(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onBackClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "← Back",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🧠", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Flip & Match",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Find matching pairs of food, festivals and clothes.",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Category Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Category",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = "[ Mixed Cards: Food + Festivals + Clothes ]",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status Indicator
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isCompletedToday) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (isCompletedToday) "✓ Today completed" else "○ Today not played",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompletedToday) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Bottom Start Action
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        ) {
            Button(
                onClick = onStartGameClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(
                    text = "▶ Start Game",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estimated time: 10–15 min",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

// ==========================================
// 2. GAME SCREEN ("FLIP & MATCH")
// ==========================================

@Composable
fun MemoryGamePlayScreen(
    onBackClick: () -> Unit,
    patientId: String? = null,
) {
    val context = LocalContext.current
    val repository = remember { MemoryRepository(context) }
    val nerRepo = remember { NERRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val activePatientId = patientId ?: nerRepo.getActivePatientId() ?: "p1"

    val accSettings = remember(activePatientId) { nerRepo.getAccessibilitySettings() }
    val currentLang = remember(activePatientId) { nerRepo.getLanguage() }
    val isVoiceEnabled = accSettings.voiceAssistantEnabled

    val ttsManager = remember(activePatientId, isVoiceEnabled) {
        if (isVoiceEnabled) {
            TextToSpeechManager.getInstance(context).apply { setLanguageLocale(currentLang) }
        } else null
    }

    DisposableEffect(activePatientId, isVoiceEnabled) {
        if (isVoiceEnabled && ttsManager != null) {
            ttsManager.speak(Localization.tr("flip_match_opening", currentLang))
        }
        onDispose {
            ttsManager?.stop()
        }
    }

    var levelInfo by remember(activePatientId) {
        mutableStateOf(FlipMatchDifficultyEngine.calculateFlipMatchLevel(nerRepo.getGameSessionsForPatient(activePatientId)))
    }

    var totalPairs by remember(levelInfo) {
        mutableIntStateOf(
            when (levelInfo.level) {
                1 -> 2  // 4 cards  → 2 pairs
                2 -> 4  // 8 cards  → 4 pairs
                3 -> 6  // 12 cards → 6 pairs
                4 -> 8  // 16 cards → 8 pairs
                5 -> 10 // 20 cards → 10 pairs
                else -> 2
            }
        )
    }

    var gridCols by remember(levelInfo) {
        mutableIntStateOf(
            when (levelInfo.level) {
                1 -> 2 // 2 × 2
                2 -> 4 // 2 × 4
                3 -> 4 // 3 × 4
                4 -> 4 // 4 × 4
                5 -> 4 // 4 × 5
                else -> 4
            }
        )
    }

    val cards = remember { mutableStateListOf<MemoryCard>() }

    var moves by remember { mutableIntStateOf(0) }
    var matchedPairs by remember { mutableIntStateOf(0) }
    var timerSeconds by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("Find all matching pairs") }

    var firstSelectedCardId by remember { mutableStateOf<Int?>(null) }
    var isEvaluating by remember { mutableStateOf(false) }

    fun resetFlipMatchGame() {
        val currentActiveId = nerRepo.getActivePatientId() ?: activePatientId
        levelInfo = FlipMatchDifficultyEngine.calculateFlipMatchLevel(nerRepo.getGameSessionsForPatient(currentActiveId))
        val newPairs = when (levelInfo.level) {
            1 -> 2
            2 -> 4
            3 -> 6
            4 -> 8
            5 -> 10
            else -> 2
        }
        totalPairs = newPairs
        gridCols = when (levelInfo.level) {
            1 -> 2
            2 -> 4
            3 -> 4
            4 -> 4
            5 -> 4
            else -> 4
        }

        cards.clear()
        val availableItems = mutableListOf<Triple<String, String, String>>()

        // Randomly select ONE eligible image set for current patient
        val randomSet = nerRepo.getRandomImageSet(currentActiveId, GameType.FLIP_MATCH, totalPairs)
        if (randomSet != null) {
            for (img in randomSet.second) {
                availableItems.add(Triple(img.imageId, img.title, img.localFilePath))
            }
        } else {
            val savedMemories = repository.getMemoriesForPatient(currentActiveId)
            for (m in savedMemories) {
                availableItems.add(Triple(m.id, m.name, m.imagePath))
            }
        }

        for (def in DefaultMemoryProvider.items) {
            if (availableItems.size < totalPairs) {
                availableItems.add(Triple(def.name, def.name, def.emojiOrAsset))
            }
        }

        val selected = availableItems.shuffled().take(totalPairs)

        var idCounter = 0
        val cardPairs = mutableListOf<MemoryCard>()
        for (item in selected) {
            cardPairs.add(MemoryCard(id = idCounter++, memoryId = item.first, name = item.second, category = "MATCH", imagePath = item.third))
            cardPairs.add(MemoryCard(id = idCounter++, memoryId = item.first, name = item.second, category = "MATCH", imagePath = item.third))
        }

        cards.addAll(cardPairs.shuffled())

        moves = 0
        matchedPairs = 0
        timerSeconds = 0
        isPaused = false
        firstSelectedCardId = null
        isEvaluating = false
        feedbackMessage = "Find all matching pairs"
        isCompleted = false
    }

    LaunchedEffect(activePatientId) {
        resetFlipMatchGame()
    }

    LaunchedEffect(isPaused, isCompleted) {
        while (!isPaused && !isCompleted) {
            delay(1000L)
            timerSeconds++
        }
    }

    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    fun onCardClick(card: MemoryCard) {
        if (isPaused || isEvaluating || card.isFlipped || card.isMatched) return

        val index = cards.indexOfFirst { it.id == card.id }
        if (index != -1) {
            cards[index] = cards[index].copy(isFlipped = true)
        }

        if (isVoiceEnabled && card.name.isNotBlank()) {
            ttsManager?.speak("This is ${card.name}")
        }

        if (firstSelectedCardId == null) {
            firstSelectedCardId = card.id
        } else {
            moves++
            isEvaluating = true
            val firstCard = cards.find { it.id == firstSelectedCardId }

            if (firstCard != null && firstCard.memoryId == card.memoryId) {
                val fIdx = cards.indexOfFirst { it.id == firstCard.id }
                val sIdx = cards.indexOfFirst { it.id == card.id }
                if (fIdx != -1) cards[fIdx] = cards[fIdx].copy(isMatched = true)
                if (sIdx != -1) cards[sIdx] = cards[sIdx].copy(isMatched = true)

                matchedPairs++
                firstSelectedCardId = null
                isEvaluating = false
                feedbackMessage = "Great match!"

                if (isVoiceEnabled) {
                    ttsManager?.speak(Localization.tr("match_found", currentLang))
                }

                if (matchedPairs >= totalPairs) {
                    isCompleted = true
                    val accuracy = if (moves > 0) (totalPairs.toFloat() / moves.toFloat()) * 100f else 100f
                    val result = GameResult(
                        date = DailyGameRepository.getTodayDateString(),
                        timeTakenSeconds = timerSeconds,
                        moves = moves,
                        pairsMatched = matchedPairs,
                        totalPairs = totalPairs,
                        accuracy = accuracy,
                    )
                    DailyGameRepository.saveCompletedResult(context, result)

                    nerRepo.recordGameSessionForPatient(
                        activePatientId,
                        GameSessionRecord(
                            gameType = GameType.FLIP_MATCH,
                            date = DailyGameRepository.getTodayDateString(),
                            startedAt = System.currentTimeMillis() - (timerSeconds * 1000L),
                            completedAt = System.currentTimeMillis(),
                            difficulty = levelInfo.recommendedDifficulty,
                            score = PerformanceScoreCalculator.calculateScore(accuracy, (moves - totalPairs).coerceAtLeast(0), timerSeconds, true),
                            accuracy = accuracy,
                            mistakes = (moves - totalPairs).coerceAtLeast(0),
                            attempts = moves,
                            timeTakenSeconds = timerSeconds,
                            levelsCompleted = levelInfo.level,
                        )
                    )

                    if (isVoiceEnabled) {
                        ttsManager?.speak(Localization.tr("flip_game_completion", currentLang))
                    }
                }
            } else {
                feedbackMessage = "Try again"
                if (isVoiceEnabled) {
                    ttsManager?.speak(Localization.tr("wrong_match", currentLang))
                }

                coroutineScope.launch {
                    delay(1000L)
                    val fIdx = cards.indexOfFirst { it.id == firstSelectedCardId }
                    val sIdx = cards.indexOfFirst { it.id == card.id }
                    if (fIdx != -1 && !cards[fIdx].isMatched) cards[fIdx] = cards[fIdx].copy(isFlipped = false)
                    if (sIdx != -1 && !cards[sIdx].isMatched) cards[sIdx] = cards[sIdx].copy(isFlipped = false)
                    firstSelectedCardId = null
                    isEvaluating = false
                    feedbackMessage = "Find all matching pairs"
                }
            }
        }
    }

    if (isCompleted) {
        GameCompletionScreen(
            timeFormatted = timeFormatted,
            moves = moves,
            pairsMatched = matchedPairs,
            totalPairs = totalPairs,
            onPlayAgain = {
                resetFlipMatchGame()
            },
            onBackToHome = onBackClick,
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onBackClick),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = "← Back",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }

            Text(
                text = "Flip & Match (L${levelInfo.level})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "⏱ $timeFormatted",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Card(
                    modifier = Modifier.clickable { isPaused = !isPaused },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Text(
                        text = if (isPaused) "▶" else "Ⅱ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Level Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Text(
                text = "[ Level ${levelInfo.level} • ${levelInfo.parameterDetail} ]",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = feedbackMessage,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (feedbackMessage == "Great match!") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridCols),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(4.dp),
        ) {
            items(cards) { card ->
                MemoryCardItemView(card = card, onClick = { onCardClick(card) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Stats Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Moves", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                    Text(text = "$moves", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Pairs", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                    Text(text = "$matchedPairs/$totalPairs", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (isPaused) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "Game Paused", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { isPaused = false }) {
                        Text(text = "Resume Game", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryCardItemView(
    card: MemoryCard,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .size(68.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isFlipped || card.isMatched) {
                if (card.isMatched) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primary
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (card.isFlipped) 4.dp else 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (card.isFlipped || card.isMatched) {
                if (card.imagePath.length > 2 && !card.imagePath.startsWith("📁")) {
                    val bitmap = remember(card.imagePath) {
                        try {
                            BitmapFactory.decodeFile(card.imagePath)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = card.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(text = "🖼️", fontSize = 28.sp)
                    }
                } else {
                    Text(
                        text = card.imagePath,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                if (card.isMatched) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x334CAF50), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.TopEnd,
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(2.dp),
                        )
                    }
                }
            } else {
                Text(
                    text = "⭐",
                    fontSize = 22.sp,
                )
            }
        }
    }
}

// ==========================================
// 3. GAME COMPLETION SCREEN
// ==========================================

@Composable
fun GameCompletionScreen(
    timeFormatted: String,
    moves: Int,
    pairsMatched: Int,
    totalPairs: Int,
    onPlayAgain: () -> Unit,
    onBackToHome: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🏆", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Great job!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You matched all pairs.",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ResultRowItem(label = "Category", value = "Mixed Cards")
                    ResultRowItem(label = "Time taken", value = timeFormatted)
                    ResultRowItem(label = "Moves", value = "$moves")
                    ResultRowItem(label = "Pairs", value = "$pairsMatched/$totalPairs")
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                ),
            ) {
                Text(text = "↻ Play Again", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFFFFF))
            }

            OutlinedButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(text = "< Back to Home", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ResultRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, fontSize = 16.sp, color = MaterialTheme.colorScheme.outline)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
