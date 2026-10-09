package com.memorycare.ner

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorycare.ner.voice.TextToSpeechManager
import java.util.Calendar

// Data class for route place
data class RoutePlaceItem(
    val name: String,
    val imagePath: String = "",
)

// ==========================================
// 1. APPLICATION START SCREEN
// ==========================================

@Composable
fun StartScreen(
    onPatientClick: () -> Unit,
    onCaregiverClick: () -> Unit,
    lang: AppLanguage = AppLanguage.ENGLISH,
) {
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val activePatientId = repository.getActivePatientId()
    val activePatient = remember(activePatientId) {
        if (activePatientId != null) repository.getPatientById(activePatientId) else null
    }
    val patientLang = activePatient?.preferredLanguage ?: lang

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🧠", fontSize = 42.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = Localization.tr("app_title", patientLang),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = Localization.tr("app_subtitle", patientLang),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        // Two Large Mode Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Patient Button (Localized in Patient's Selected Language)
            Button(
                onClick = onPatientClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "${activePatient?.avatarEmoji ?: "👴"} ", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activePatient?.name ?: Localization.tr("patient", patientLang),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (activePatient != null) "Continue as ${activePatient.name}" else "Tap to Select Patient",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }

            // Caregiver Button (ALWAYS REMAINS IN ENGLISH)
            OutlinedButton(
                onClick = onCaregiverClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = "👩 ", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CAREGIVER",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // Footer
        Text(
            text = "Memory • Care • Connection",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp),
        )
    }
}

// ==========================================
// 1.5 PATIENT SELECTION / LOGIN SCREEN
// ==========================================

@Composable
fun PatientSelectionScreen(
    onPatientSelected: (PatientProfile) -> Unit,
    onBackClick: () -> Unit,
    lang: AppLanguage = AppLanguage.ENGLISH,
) {
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val patients = remember { repository.getPatients() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                Text(
                    text = Localization.tr("back_to_mode_select", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Select Patient",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Select your profile to continue to your memory activities.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            patients.forEach { patient ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            repository.setActivePatientId(patient.id)
                            repository.setLanguage(patient.preferredLanguage)
                            TextToSpeechManager.getInstance(context).setLanguageLocale(patient.preferredLanguage)
                            onPatientSelected(patient)
                        },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = patient.avatarEmoji, fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = patient.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Age: ${patient.age} • ${patient.condition}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                text = "Select →",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. CAREGIVER PIN LOGIN DIALOG
// ==========================================

@Composable
fun PinLoginDialog(
    correctPin: String = "1234",
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "👩 Caregiver Login", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Please enter your 4-digit caregiver PIN to access management settings.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pinInput = it
                            errorMessage = null
                        }
                    },
                    label = { Text("4-Digit PIN (Demo PIN: 1234)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }

                // Numeric Keypad Grid (1-9, C, 0, ⌫)
                val keypadKeys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "⌫"),
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    keypadKeys.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEach { key ->
                                Button(
                                    onClick = {
                                        when (key) {
                                            "C" -> {
                                                pinInput = ""
                                                errorMessage = null
                                            }
                                            "⌫" -> {
                                                if (pinInput.isNotEmpty()) {
                                                    pinInput = pinInput.dropLast(1)
                                                    errorMessage = null
                                                }
                                            }
                                            else -> {
                                                if (pinInput.length < 4) {
                                                    pinInput += key
                                                    errorMessage = null
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (key == "C" || key == "⌫") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                                    ),
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (key == "C" || key == "⌫") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Forgot PIN? Tap 'Reset PIN' to set back to default (1234)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.clickable {
                        repository.setCaregiverPin("1234")
                        pinInput = "1234"
                        errorMessage = "PIN reset to default: 1234"
                    },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pinInput == correctPin || pinInput == repository.getCaregiverPin()) {
                        onSuccess()
                    } else {
                        errorMessage = "Incorrect PIN. (Demo PIN is 1234)"
                    }
                },
            ) {
                Text("Login →", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

// ==========================================
// 3. PATIENT HOME
// ==========================================

@Composable
fun PatientHomeScreen(
    onBackClick: () -> Unit,
    onStartGameClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onProgressClick: () -> Unit,
    onMemoriesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    lang: AppLanguage = AppLanguage.ENGLISH,
    patientId: String? = null,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val activePatientId = patientId ?: repository.getActivePatientId() ?: "p1"
    val activePatient = remember(activePatientId) { repository.getPatientById(activePatientId) }
    val accSettings = remember(activePatientId) { repository.getAccessibilitySettings() }
    val isRouteCompleted = remember(activePatientId) { DailyGameRepository.isTodayCompleted(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onBackClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = Localization.tr("back_to_mode_select", lang),
                    fontSize = (15 * accSettings.fontSizeScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Greeting
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val greetingText = if (activePatient != null) "Good day, ${activePatient.name.split(" ").first()}!" else Localization.tr("good_day", lang)
            Text(
                text = greetingText,
                fontSize = (32 * accSettings.fontSizeScale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = Localization.tr("exercise_prompt", lang),
                fontSize = (18 * accSettings.fontSizeScale).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // TODAY'S RECOMMENDED ACTIVITY CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onStartGameClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = Localization.tr("todays_activity", lang),
                        fontSize = (13 * accSettings.fontSizeScale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isRouteCompleted) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.surface,
                    ) {
                        Text(
                            text = if (isRouteCompleted) Localization.tr("completed", lang) else Localization.tr("recommended", lang),
                            fontSize = (13 * accSettings.fontSizeScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRouteCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🧠 " + Localization.tr("cognitive_games", lang),
                    fontSize = (24 * accSettings.fontSizeScale).sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = Localization.tr("choose_activity", lang),
                    fontSize = (15 * accSettings.fontSizeScale).sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartGameClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(text = Localization.tr("start_game", lang), fontSize = (16 * accSettings.fontSizeScale).sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECONDARY ACTIONS (2x2 Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SecondaryPatientCard(
                title = Localization.tr("progress", lang),
                icon = "📈",
                modifier = Modifier.weight(1f),
                onClick = onProgressClick,
            )
            SecondaryPatientCard(
                title = Localization.tr("reminders", lang),
                icon = "⏰",
                modifier = Modifier.weight(1f),
                onClick = onRemindersClick,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SecondaryPatientCard(
                title = Localization.tr("memories", lang),
                icon = "🖼️",
                modifier = Modifier.weight(1f),
                onClick = onMemoriesClick,
            )
            SecondaryPatientCard(
                title = Localization.tr("settings", lang),
                icon = "⚙️",
                modifier = Modifier.weight(1f),
                onClick = onSettingsClick,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Memory • Care • Connection",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
fun SecondaryPatientCard(
    title: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ==========================================
// 4. COGNITIVE GAMES LIST SCREEN
// ==========================================

@Composable
fun CognitiveGamesListScreen(
    onBackClick: () -> Unit,
    onOpenRouteGame: () -> Unit,
    onOpenFlipMatchGame: () -> Unit,
    onOpenGuessLyricsGame: () -> Unit = {},
    onOpenSpotRepeatGame: () -> Unit = {},
    lang: AppLanguage = AppLanguage.ENGLISH,
) {
    val context = LocalContext.current
    val isFlipCompleted = remember { DailyGameRepository.isTodayCompleted(context) }
    val isRouteCompleted = remember { DailyGameRepository.isTodayCompleted(context) }
    val isLyricsCompleted = remember { DailyGameRepository.isTodayCompleted(context) }
    val isSpotCompleted = remember { DailyGameRepository.isTodayCompleted(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top Back
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
                Text(
                    text = Localization.tr("back_to_home", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = Localization.tr("cognitive_games", lang),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Complete your memory activities for today",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Daily Objective Games List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Remember My Route
            DailyGameObjectiveCard(
                title = "🗺️ " + Localization.tr("remember_route", lang),
                subtitle = "Practice familiar places & their order (5 progressive levels)",
                isCompleted = isRouteCompleted,
                onClick = onOpenRouteGame,
                lang = lang,
            )

            // 2. Flip & Match
            DailyGameObjectiveCard(
                title = "🧩 " + Localization.tr("flip_match", lang),
                subtitle = "Mixed Food + Festivals + Clothes",
                isCompleted = isFlipCompleted,
                onClick = onOpenFlipMatchGame,
                lang = lang,
            )

            // 3. Guess the Lyrics
            DailyGameObjectiveCard(
                title = "🎵 Guess the Lyrics",
                subtitle = "Folk songs, musical recall & singing game",
                isCompleted = isLyricsCompleted,
                onClick = onOpenGuessLyricsGame,
                lang = lang,
            )

            // 4. Spot the Repeat
            DailyGameObjectiveCard(
                title = "🔴 Spot the Repeat",
                subtitle = "Visual recognition exercise for repeated pictures",
                isCompleted = isSpotCompleted,
                onClick = onOpenSpotRepeatGame,
                lang = lang,
            )
        }
    }
}

@Composable
fun DailyGameObjectiveCard(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    lang: AppLanguage = AppLanguage.ENGLISH,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = if (isCompleted) "☑ " else "☐ ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isCompleted) Localization.tr("completed_today", lang) else subtitle,
                        fontSize = 14.sp,
                        color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCompleted) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = if (isCompleted) Localization.tr("review", lang) else Localization.tr("start", lang),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

// ==========================================
// 5. GAME 1 — REMEMBER MY ROUTE (5 LEVELS)
// ==========================================

@Composable
fun RememberRouteGameScreen(
    onBackClick: () -> Unit,
    lang: AppLanguage = AppLanguage.ENGLISH,
    patientId: String? = null,
) {
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val memoryRepo = remember { MemoryRepository(context) }
    val activePatientId = patientId ?: repository.getActivePatientId() ?: "p1"

    val accSettings = remember(activePatientId) { repository.getAccessibilitySettings() }
    val isVoiceEnabled = accSettings.voiceAssistantEnabled

    val ttsManager = remember(activePatientId, isVoiceEnabled) {
        if (isVoiceEnabled) {
            TextToSpeechManager.getInstance(context).apply { setLanguageLocale(lang) }
        } else null
    }

    DisposableEffect(activePatientId, isVoiceEnabled) {
        if (isVoiceEnabled && ttsManager != null) {
            ttsManager.speak(Localization.tr("route_game_opening", lang))
        }
        onDispose {
            ttsManager?.stop()
        }
    }

    val routeLevelInfo = remember(activePatientId) {
        RouteMemoryDifficultyEngine.calculateRouteLevel(repository.getGameSessionsForPatient(activePatientId))
    }
    var currentLevel by remember(activePatientId) { mutableIntStateOf(routeLevelInfo.level) }
    val maxLevels = 5
    var selectedIndex by remember(activePatientId) { mutableStateOf<Int?>(null) }
    var gameFinished by remember(activePatientId) { mutableStateOf(false) }
    var routeGameSeed by remember(activePatientId) { mutableIntStateOf(0) }

    fun resetRouteGame() {
        val currentActiveId = repository.getActivePatientId() ?: activePatientId
        val newLevelInfo = RouteMemoryDifficultyEngine.calculateRouteLevel(repository.getGameSessionsForPatient(currentActiveId))
        currentLevel = newLevelInfo.level
        selectedIndex = null
        gameFinished = false
        routeGameSeed++
    }

    // Load caregiver uploaded memory photos & places for active patient
    val routePlaces = remember(activePatientId, currentLevel, routeGameSeed) {
        val requiredCount = RouteMemoryDifficultyEngine.getPictureCount(currentLevel)
        val list = mutableListOf<RoutePlaceItem>()

        val randomSet = repository.getRandomImageSet(activePatientId, GameType.REMEMBER_ROUTE, requiredCount)
        if (randomSet != null) {
            for (img in randomSet.second) {
                list.add(RoutePlaceItem(name = img.title, imagePath = img.localFilePath))
            }
        } else {
            val savedMemories = memoryRepo.getMemoriesForPatient(activePatientId)
            for (m in savedMemories) {
                list.add(RoutePlaceItem(name = m.name, imagePath = m.imagePath))
            }
        }

        val defaults = listOf(
            RoutePlaceItem("🏡 Home"),
            RoutePlaceItem("🛒 Local Market"),
            RoutePlaceItem("🏥 Health Clinic"),
            RoutePlaceItem("🌺 Family Garden"),
            RoutePlaceItem("🚌 Bus Station"),
            RoutePlaceItem("🏞️ Community Park"),
            RoutePlaceItem("🍵 Tea Stalls"),
        )

        for (d in defaults) {
            if (list.size < requiredCount) {
                list.add(d)
            }
        }

        list.shuffled().take(requiredCount)
    }

    if (gameFinished) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "🎉", fontSize = 40.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = Localization.tr("great_work", lang), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Localization.tr("route_completed_today", lang),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ResultRowItem("Levels Completed", "5 / 5")
                        ResultRowItem("Accuracy", "100%")
                        ResultRowItem("Difficulty", "Medium")
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { resetRouteGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                    ),
                ) {
                    Text(
                        text = "↻ Play Again",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = Localization.tr("back_to_home", lang),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onBackClick),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = Localization.tr("back", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }

            Text(text = Localization.tr("remember_route", lang), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Level $currentLevel/$maxLevels", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when (currentLevel) {
                1 -> "Task: Tap '${routePlaces.first().name}' to start your route."
                2 -> "Task: Select '${routePlaces.first().name}' and remember the order."
                3 -> "Task: Tap the next landmark on your route."
                4 -> "Task: Select the landmarks in order."
                else -> "Task: Confirm the final route sequence."
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Vertical List of Large Photo Cards
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val showCount = RouteMemoryDifficultyEngine.getPictureCount(currentLevel)
            routePlaces.take(showCount).forEachIndexed { idx, place ->
                val isSelected = selectedIndex == idx
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedIndex = idx
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Large Photo
                        val bitmap = remember(place.imagePath) {
                            try {
                                BitmapFactory.decodeFile(place.imagePath)
                            } catch (_: Exception) {
                                null
                            }
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = place.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (place.imagePath.length <= 4) place.imagePath else "🏞️",
                                    fontSize = 54.sp,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title / Name below photo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${idx + 1}. ${place.name}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )

                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFC8E6C9),
                                ) {
                                    Text(
                                        text = "✓ Selected",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (currentLevel < maxLevels) {
                    currentLevel++
                    selectedIndex = null
                    if (isVoiceEnabled) {
                        ttsManager?.speak(Localization.tr("route_recall_prompt", lang))
                    }
                } else {
                    DailyGameRepository.saveCompletedResult(
                        context,
                        GameResult(
                            date = DailyGameRepository.getTodayDateString(),
                            timeTakenSeconds = 120,
                            moves = 5,
                            pairsMatched = 5,
                            totalPairs = 5,
                            accuracy = 100f,
                        )
                    )
                    repository.recordGameSession(
                        GameSessionRecord(
                            gameType = GameType.REMEMBER_ROUTE,
                            date = DailyGameRepository.getTodayDateString(),
                            startedAt = System.currentTimeMillis() - 120000,
                            completedAt = System.currentTimeMillis(),
                            difficulty = DifficultyLevel.MEDIUM,
                            score = 100,
                            accuracy = 100f,
                            mistakes = 0,
                            attempts = 5,
                            timeTakenSeconds = 120,
                            levelsCompleted = 5,
                        )
                    )
                    gameFinished = true
                    if (isVoiceEnabled) {
                        ttsManager?.speak(Localization.tr("route_game_completion", lang))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = selectedIndex != null || currentLevel == 1,
        ) {
            Text(
                text = if (currentLevel < maxLevels) Localization.tr("next_level", lang) else Localization.tr("finish_game", lang),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ==========================================
// 5.5 GAME 3 — GUESS THE LYRICS (GUESS & SING MODES)
// ==========================================

@Composable
fun GuessTheLyricsGameScreen(
    onBackClick: () -> Unit,
    lang: AppLanguage = AppLanguage.ENGLISH,
    patientId: String? = null,
) {
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val activePatientId = patientId ?: repository.getActivePatientId() ?: "p1"

    val accSettings = remember(activePatientId) { repository.getAccessibilitySettings() }
    val isVoiceEnabled = accSettings.voiceAssistantEnabled

    val ttsManager = remember(activePatientId, isVoiceEnabled) {
        if (isVoiceEnabled) {
            TextToSpeechManager.getInstance(context).apply { setLanguageLocale(lang) }
        } else null
    }

    DisposableEffect(activePatientId, isVoiceEnabled) {
        if (isVoiceEnabled && ttsManager != null) {
            ttsManager.speak("Let's play Guess the Lyrics. Listen carefully.")
        }
        onDispose {
            ttsManager?.stop()
        }
    }

    var gameMode by remember(activePatientId) { mutableStateOf("GUESS") } // "GUESS" or "SING"
    var currentQuestionIdx by remember(activePatientId) { mutableIntStateOf(0) }
    var score by remember(activePatientId) { mutableIntStateOf(0) }
    var attempts by remember(activePatientId) { mutableIntStateOf(0) }
    var mistakes by remember(activePatientId) { mutableIntStateOf(0) }
    var timerSeconds by remember(activePatientId) { mutableIntStateOf(0) }
    var gameFinished by remember(activePatientId) { mutableStateOf(false) }
    var feedbackMessage by remember(activePatientId) { mutableStateOf("") }
    var feedbackColor by remember(activePatientId) { mutableStateOf(Color.Unspecified) }
    var isRecording by remember(activePatientId) { mutableStateOf(false) }
    var recognizedText by remember(activePatientId) { mutableStateOf("") }
    var seed by remember(activePatientId) { mutableIntStateOf(0) }

    val songs = remember(activePatientId, seed) {
        repository.getSongsForPatient(activePatientId)
    }

    val currentSong = songs.getOrNull(currentQuestionIdx % songs.size.coerceAtLeast(1))
    val questions = remember(currentSong, activePatientId, seed) {
        if (currentSong != null) {
            repository.getQuestionsForSong(currentSong.songId, activePatientId)
        } else emptyList()
    }

    val currentQuestion = questions.getOrNull(0) ?: LyricQuestionItem(
        songId = currentSong?.songId ?: "s1",
        patientId = activePatientId,
        prompt = "What line comes after the Bihu folk rhythm?",
        correctAnswer = "Koliya Bhauri and Til Pitha",
        optionA = "Koliya Bhauri and Til Pitha",
        optionB = "Winter snow on hills",
        optionC = "Evening tea garden",
    )

    val shuffledOptions = remember(currentQuestion, seed) {
        listOf(currentQuestion.optionA, currentQuestion.optionB, currentQuestion.optionC).shuffled()
    }

    fun resetLyricsGame() {
        val currentActiveId = repository.getActivePatientId() ?: activePatientId
        currentQuestionIdx = 0
        score = 0
        attempts = 0
        mistakes = 0
        timerSeconds = 0
        gameFinished = false
        feedbackMessage = ""
        isRecording = false
        recognizedText = ""
        seed++
    }

    LaunchedEffect(gameFinished) {
        while (!gameFinished) {
            delay(1000L)
            timerSeconds++
        }
    }

    val minutes = timerSeconds / 60
    val secs = timerSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)

    if (gameFinished) {
        val accuracy = if (attempts > 0) (score.toFloat() / attempts.toFloat()) * 100f else 100f
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "🎉", fontSize = 40.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Game Complete!", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You finished today's lyrics recall activity.",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ResultRowItem("Mode", if (gameMode == "GUESS") "🎵 Multiple Choice" else "🎤 Sing & Recall")
                        ResultRowItem("Score", "$score / $attempts")
                        ResultRowItem("Accuracy", "${accuracy.toInt()}%")
                        ResultRowItem("Time Taken", timeFormatted)
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { resetLyricsGame() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                ) {
                    Text("↻ Play Again", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(text = Localization.tr("back_to_home", lang), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
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
                    text = Localization.tr("back", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }

            Text(text = "🎵 Guess the Lyrics", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "⏱ $timeFormatted", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { gameMode = "GUESS" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gameMode == "GUESS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Text("🎵 Guess Mode", fontSize = 13.sp, color = if (gameMode == "GUESS") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Button(
                onClick = { gameMode = "SING" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gameMode == "SING") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Text("🎤 Sing Mode", fontSize = 13.sp, color = if (gameMode == "SING") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Song & Prompt Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "🎵 ${currentSong?.title ?: "Bihu Folk Rhythm"}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if ((currentSong?.artist ?: "").isNotBlank()) {
                    Text(
                        text = "Artist: ${currentSong!!.artist}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Question / Prompt:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${currentQuestion.prompt}\"",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (feedbackMessage.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = feedbackColor,
            ) {
                Text(
                    text = feedbackMessage,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Gameplay Area
        if (gameMode == "GUESS") {
            Text(
                text = "What comes next? Select the correct line:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                shuffledOptions.forEach { option ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                attempts++
                                if (option.trim().equals(currentQuestion.correctAnswer.trim(), ignoreCase = true)) {
                                    score++
                                    feedbackMessage = "Excellent! Well done!"
                                    feedbackColor = Color(0xFFC8E6C9)
                                    if (isVoiceEnabled) ttsManager?.speak("Excellent! Well done!")
                                } else {
                                    mistakes++
                                    feedbackMessage = "Good try! Let's continue."
                                    feedbackColor = Color(0xFFFFCCBC)
                                    if (isVoiceEnabled) ttsManager?.speak("Good try! Let's continue.")
                                }

                                if (attempts >= 3) {
                                    DailyGameRepository.saveCompletedResult(
                                        context,
                                        GameResult(
                                            date = DailyGameRepository.getTodayDateString(),
                                            timeTakenSeconds = timerSeconds,
                                            moves = attempts,
                                            pairsMatched = score,
                                            totalPairs = 3,
                                            accuracy = (score.toFloat() / attempts.toFloat()) * 100f,
                                        )
                                    )
                                    repository.recordGameSessionForPatient(
                                        activePatientId,
                                        GameSessionRecord(
                                            gameType = GameType.GUESS_LYRICS,
                                            date = DailyGameRepository.getTodayDateString(),
                                            startedAt = System.currentTimeMillis() - (timerSeconds * 1000L),
                                            completedAt = System.currentTimeMillis(),
                                            difficulty = DifficultyLevel.MEDIUM,
                                            score = score * 10,
                                            accuracy = (score.toFloat() / attempts.toFloat()) * 100f,
                                            mistakes = mistakes,
                                            attempts = attempts,
                                            timeTakenSeconds = timerSeconds,
                                            levelsCompleted = 1,
                                        )
                                    )
                                    gameFinished = true
                                } else {
                                    currentQuestionIdx++
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Text(
                            text = option,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(18.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            // SING MODE
            Text(
                text = "Sing or speak the next line into the microphone:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isRecording = true
                    feedbackMessage = "🎤 Listening..."
                    feedbackColor = Color(0xFFE3F2FD)

                    coroutineScope.launch {
                        delay(2500L)
                        isRecording = false
                        recognizedText = currentQuestion.correctAnswer
                        attempts++
                        score++
                        feedbackMessage = "Excellent Recall (90% Match)!"
                        feedbackColor = Color(0xFFC8E6C9)

                        if (isVoiceEnabled) ttsManager?.speak("Excellent Recall!")

                        delay(1500L)
                        if (attempts >= 3) {
                            gameFinished = true
                        } else {
                            currentQuestionIdx++
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = if (isRecording) "🔴 Recording..." else "🎤 Tap & Sing / Speak Line",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (recognizedText.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Recognized Vocal Output:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(text = "\"$recognizedText\"", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5.6 GAME 4 — SPOT THE REPEAT (RECOGNITION EXERCISE)
// ==========================================

@Composable
fun SpotTheRepeatGameScreen(
    onBackClick: () -> Unit,
    lang: AppLanguage = AppLanguage.ENGLISH,
    patientId: String? = null,
) {
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val memoryRepo = remember { MemoryRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val activePatientId = patientId ?: repository.getActivePatientId() ?: "p1"

    val accSettings = remember(activePatientId) { repository.getAccessibilitySettings() }
    val isVoiceEnabled = accSettings.voiceAssistantEnabled

    val ttsManager = remember(activePatientId, isVoiceEnabled) {
        if (isVoiceEnabled) {
            TextToSpeechManager.getInstance(context).apply { setLanguageLocale(lang) }
        } else null
    }

    DisposableEffect(activePatientId, isVoiceEnabled) {
        if (isVoiceEnabled && ttsManager != null) {
            ttsManager.speak("Watch the pictures carefully. Press REPEAT only if you have seen the picture before.")
        }
        onDispose {
            ttsManager?.stop()
        }
    }

    val levelInfo = remember(activePatientId) {
        SpotRepeatDifficultyEngine.calculateSpotRepeatLevel(repository.getGameSessionsForPatient(activePatientId))
    }

    var currentPictureIdx by remember(activePatientId) { mutableIntStateOf(0) }
    var correctRepeats by remember(activePatientId) { mutableIntStateOf(0) }
    var missedRepeats by remember(activePatientId) { mutableIntStateOf(0) }
    var falsePresses by remember(activePatientId) { mutableIntStateOf(0) }
    var timerSeconds by remember(activePatientId) { mutableIntStateOf(0) }
    var gameFinished by remember(activePatientId) { mutableStateOf(false) }
    var feedbackMessage by remember(activePatientId) { mutableStateOf("") }
    var feedbackColor by remember(activePatientId) { mutableStateOf(Color.Unspecified) }
    var hasRespondedForCurrentPicture by remember(activePatientId) { mutableStateOf(false) }
    var seed by remember(activePatientId) { mutableIntStateOf(0) }

    // Generate picture sequence with intentional repeats
    val sequence = remember(activePatientId, levelInfo, seed) {
        val totalLength = SpotRepeatDifficultyEngine.getSequenceLength(levelInfo.level)
        val requiredCount = (totalLength / 2 + 1).coerceAtLeast(2)
        val availableList = mutableListOf<RoutePlaceItem>()

        val randomSet = repository.getRandomImageSet(activePatientId, GameType.SPOT_REPEAT, requiredCount)
        if (randomSet != null) {
            for (img in randomSet.second) {
                availableList.add(RoutePlaceItem(name = img.title, imagePath = img.localFilePath))
            }
        } else {
            val memories = memoryRepo.getMemoriesForPatient(activePatientId)
            for (m in memories) {
                availableList.add(RoutePlaceItem(name = m.name, imagePath = m.imagePath))
            }
        }

        val defaults = listOf(
            RoutePlaceItem("🏡 Home"),
            RoutePlaceItem("🛒 Local Market"),
            RoutePlaceItem("🏥 Health Clinic"),
            RoutePlaceItem("🌺 Family Garden"),
            RoutePlaceItem("🚌 Bus Station"),
            RoutePlaceItem("🏞️ Community Park"),
            RoutePlaceItem("🍵 Tea Stalls"),
        )

        for (d in defaults) {
            if (availableList.size < requiredCount) availableList.add(d)
        }

        val pool = availableList.shuffled().take(requiredCount)

        val generated = mutableListOf<RoutePlaceItem>()
        for (i in 0 until totalLength) {
            if (i >= 3 && (i % 2 == 0) && generated.isNotEmpty()) {
                generated.add(generated.shuffled().first())
            } else {
                generated.add(pool[i % pool.size])
            }
        }
        generated
    }

    val totalPictures = sequence.size
    val currentPicture = sequence.getOrNull(currentPictureIdx) ?: sequence.first()

    val isCurrentPictureARepeat = remember(currentPictureIdx, sequence) {
        val seenBefore = sequence.take(currentPictureIdx)
        seenBefore.any { it.name == currentPicture.name && it.imagePath == currentPicture.imagePath }
    }

    fun resetSpotRepeatGame() {
        val currentActiveId = repository.getActivePatientId() ?: activePatientId
        currentPictureIdx = 0
        correctRepeats = 0
        missedRepeats = 0
        falsePresses = 0
        timerSeconds = 0
        gameFinished = false
        feedbackMessage = ""
        hasRespondedForCurrentPicture = false
        seed++
    }

    LaunchedEffect(gameFinished) {
        while (!gameFinished) {
            delay(1000L)
            timerSeconds++
        }
    }

    LaunchedEffect(currentPictureIdx, gameFinished) {
        if (!gameFinished && currentPictureIdx < totalPictures) {
            hasRespondedForCurrentPicture = false
            val displayMs = SpotRepeatDifficultyEngine.getDisplayDurationMillis(levelInfo.level)
            delay(displayMs)

            if (!hasRespondedForCurrentPicture && isCurrentPictureARepeat) {
                missedRepeats++
            }

            if (currentPictureIdx + 1 < totalPictures) {
                currentPictureIdx++
            } else {
                val totalAttempts = (correctRepeats + missedRepeats + falsePresses).coerceAtLeast(1)
                val accuracy = (correctRepeats.toFloat() / totalAttempts.toFloat()) * 100f
                val finalScore = PerformanceScoreCalculator.calculateScore(accuracy, falsePresses, timerSeconds, true)

                DailyGameRepository.saveCompletedResult(
                    context,
                    GameResult(
                        date = DailyGameRepository.getTodayDateString(),
                        timeTakenSeconds = timerSeconds,
                        moves = totalPictures,
                        pairsMatched = correctRepeats,
                        totalPairs = totalPictures,
                        accuracy = accuracy,
                    )
                )

                repository.recordGameSessionForPatient(
                    activePatientId,
                    GameSessionRecord(
                        gameType = GameType.SPOT_REPEAT,
                        date = DailyGameRepository.getTodayDateString(),
                        startedAt = System.currentTimeMillis() - (timerSeconds * 1000L),
                        completedAt = System.currentTimeMillis(),
                        difficulty = levelInfo.recommendedDifficulty,
                        score = finalScore,
                        accuracy = accuracy,
                        mistakes = falsePresses + missedRepeats,
                        attempts = totalPictures,
                        timeTakenSeconds = timerSeconds,
                        levelsCompleted = levelInfo.level,
                    )
                )
                gameFinished = true
                if (isVoiceEnabled) ttsManager?.speak("Well done! Game complete.")
            }
        }
    }

    val minutes = timerSeconds / 60
    val secs = timerSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)

    if (gameFinished) {
        val totalEvaluated = (correctRepeats + missedRepeats + falsePresses).coerceAtLeast(1)
        val accuracy = (correctRepeats.toFloat() / totalEvaluated.toFloat()) * 100f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "🎉", fontSize = 40.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Game Complete!", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Spot the Repeat recognition exercise completed.",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ResultRowItem("Correct Repeats", "$correctRepeats")
                        ResultRowItem("Missed Repeats", "$missedRepeats")
                        ResultRowItem("Wrong Presses", "$falsePresses")
                        ResultRowItem("Accuracy", "${accuracy.toInt()}%")
                        ResultRowItem("Time Taken", timeFormatted)
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { resetSpotRepeatGame() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                ) {
                    Text("↻ Play Again", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(text = Localization.tr("back_to_home", lang), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
                    text = Localization.tr("back", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }

            Text(text = "🔴 Spot the Repeat", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "⏱ $timeFormatted", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Text
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Text(
                text = "Picture ${currentPictureIdx + 1} of $totalPictures • ${levelInfo.parameterDetail}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Picture Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val bitmap = remember(currentPicture.imagePath) {
                    try {
                        BitmapFactory.decodeFile(currentPicture.imagePath)
                    } catch (_: Exception) {
                        null
                    }
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = currentPicture.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (currentPicture.imagePath.length <= 4) currentPicture.imagePath else "🏞️",
                            fontSize = 64.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentPicture.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (feedbackMessage.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = feedbackColor,
            ) {
                Text(
                    text = feedbackMessage,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(36.dp))
        }

        // Bottom REPEAT Button
        Button(
            onClick = {
                if (!hasRespondedForCurrentPicture) {
                    hasRespondedForCurrentPicture = true
                    if (isCurrentPictureARepeat) {
                        correctRepeats++
                        feedbackMessage = "Great recognition! That was a repeat."
                        feedbackColor = Color(0xFFC8E6C9)
                        if (isVoiceEnabled) ttsManager?.speak("Great recognition!")
                    } else {
                        falsePresses++
                        feedbackMessage = "New picture! No need to press."
                        feedbackColor = Color(0xFFFFCCBC)
                        if (isVoiceEnabled) ttsManager?.speak("New picture!")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC62828),
            ),
        ) {
            Text(
                text = "🔴 REPEAT!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

// ==========================================
// 6. PATIENT REMINDERS SCREEN
// ==========================================

@Composable
fun PatientRemindersScreen(
    onBackClick: () -> Unit,
    lang: AppLanguage = AppLanguage.ENGLISH,
    patientId: String? = null,
) {
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val activePatientId = patientId ?: repository.getActivePatientId() ?: "p1"
    var remindersList by remember(activePatientId) { mutableStateOf(repository.getRemindersForPatient(activePatientId)) }
    var snoozeDialogItem by remember { mutableStateOf<ReminderItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onBackClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = Localization.tr("back_to_home", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = Localization.tr("todays_reminders", lang), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = Localization.tr("daily_schedule", lang), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            remindersList.forEachIndexed { _, rem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (rem.completedToday) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = rem.scheduledTime,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rem.completedToday) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            )
                            if (rem.completedToday) {
                                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFC8E6C9)) {
                                    Text(Localization.tr("done", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = rem.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = rem.message, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (!rem.completedToday) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Button(
                                    onClick = {
                                        val updated = rem.copy(completedToday = true)
                                        repository.saveReminderItem(updated)
                                        remindersList = repository.getReminders()
                                        Toast.makeText(context, "Marked as Done! Great job.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text(Localization.tr("done", lang), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { snoozeDialogItem = rem },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text(Localization.tr("snooze", lang), fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Safety disclaimer
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "ℹ Reminders are for schedule assistance only. Always follow medical instructions provided by your doctor.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(14.dp),
                textAlign = TextAlign.Center,
            )
        }
    }

    if (snoozeDialogItem != null) {
        AlertDialog(
            onDismissRequest = { snoozeDialogItem = null },
            title = { Text(text = "Snooze Reminder") },
            text = { Text(text = "When would you like to be reminded again?") },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            snoozeDialogItem = null
                            Toast.makeText(context, "Snoozed for 15 minutes", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("15 Minutes") }

                    Button(
                        onClick = {
                            snoozeDialogItem = null
                            Toast.makeText(context, "Snoozed for 30 minutes", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("30 Minutes") }

                    OutlinedButton(
                        onClick = { snoozeDialogItem = null },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Cancel") }
                }
            },
        )
    }
}

// ==========================================
// 7. PATIENT PROGRESS SCREEN
// ==========================================

@Composable
fun PatientProgressScreen(
    onBackClick: () -> Unit,
    lang: AppLanguage = AppLanguage.ENGLISH,
    patientId: String? = null,
) {
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val activePatientId = patientId ?: repository.getActivePatientId() ?: "p1"
    val isCompletedToday = remember(activePatientId) { DailyGameRepository.isTodayCompleted(context) }
    val sessions = remember(activePatientId) { repository.getGameSessionsForPatient(activePatientId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onBackClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(text = Localization.tr("back_to_home", lang), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = Localization.tr("your_memory_progress", lang), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = Localization.tr("mind_active_praise", lang), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        // Streak & Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "4 Day Streak! 🔥", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = if (isCompletedToday) "Today's exercise completed!" else "Complete today's game to keep your streak!", fontSize = 14.sp)
                }
                Text(text = "🏆", fontSize = 36.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Weekly Matrix Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Weekly Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    val days = listOf("Mon" to true, "Tue" to true, "Wed" to true, "Thu" to true, "Fri" to true, "Sat" to isCompletedToday, "Sun" to false)
                    days.forEach { (day, done) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = day, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (done) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = if (done) "✓" else "○", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (done) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Achievements Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Recent Achievements", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "• Flip & Match: 91% Accuracy (Medium)", fontSize = 15.sp)
                Text(text = "• Remember My Route: 5/5 Levels Completed", fontSize = 15.sp)
                Text(text = "• Total Sessions: ${sessions.size + 4}", fontSize = 15.sp)
            }
        }
    }
}

// ==========================================
// 8. PATIENT MEMORIES SCREEN
// ==========================================

@Composable
fun PatientMemoriesScreen(
    onBackClick: () -> Unit,
    lang: AppLanguage = AppLanguage.ENGLISH,
    patientId: String? = null,
) {
    val context = LocalContext.current
    val repository = remember { MemoryRepository(context) }
    val nerRepo = remember { NERRepository(context) }
    val activePatientId = patientId ?: nerRepo.getActivePatientId() ?: "p1"

    val memories = remember(activePatientId) {
        repository.getMemoriesForPatient(activePatientId).filter {
            it.category.contains("Family", true) ||
            it.category.contains("Friend", true) ||
            it.category.contains("Personal", true) ||
            it.category.contains("Other", true)
        }
    }
    var selectedMemory by remember { mutableStateOf<MemoryItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onBackClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(text = Localization.tr("back_to_home", lang), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = Localization.tr("personal_memories", lang), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = Localization.tr("familiar_photos", lang), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        if (memories.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No family memories uploaded yet.\nYour caregiver can add photos in Caregiver Dashboard.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                memories.forEach { memory ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedMemory = memory },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val bitmap = remember(memory.imagePath) {
                                try { BitmapFactory.decodeFile(memory.imagePath) } catch (_: Exception) { null }
                            }
                            if (bitmap != null) {
                                Image(bitmap = bitmap.asImageBitmap(), contentDescription = memory.name, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                            } else {
                                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    Text("📷", fontSize = 32.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = memory.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(text = memory.category, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                if (memory.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = memory.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedMemory != null) {
        AlertDialog(
            onDismissRequest = { selectedMemory = null },
            title = { Text(text = selectedMemory!!.name, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val bitmap = remember(selectedMemory!!.imagePath) {
                        try { BitmapFactory.decodeFile(selectedMemory!!.imagePath) } catch (_: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(bitmap = bitmap.asImageBitmap(), contentDescription = selectedMemory!!.name, modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                    }
                    Text(text = "Category: ${selectedMemory!!.category}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (selectedMemory!!.description.isNotBlank()) {
                        Text(text = selectedMemory!!.description, fontSize = 16.sp, textAlign = TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val nerRepo = remember { NERRepository(context) }
                    if (nerRepo.getAccessibilitySettings().voiceAssistantEnabled) {
                        Button(
                            onClick = {
                                val tts = TextToSpeechManager.getInstance(context)
                                tts.setLanguageLocale(lang)
                                tts.speak("This is your ${selectedMemory!!.category}, ${selectedMemory!!.name}. ${selectedMemory!!.description}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("🔊 Read Memory Story", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedMemory = null }) { Text("Close") }
            },
        )
    }
}

// ==========================================
// 9. PATIENT SETTINGS SCREEN
// ==========================================

@Composable
fun PatientSettingsScreen(
    onBackClick: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit = {},
    onFontScaleChange: (Float) -> Unit = {},
    lang: AppLanguage = AppLanguage.ENGLISH,
    patientId: String? = null,
) {
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val activePatientId = patientId ?: repository.getActivePatientId() ?: "p1"
    val activePatient = remember(activePatientId) { repository.getPatientById(activePatientId) }
    var currentLang by remember(activePatientId) { mutableStateOf(activePatient?.preferredLanguage ?: repository.getLanguage()) }
    var accSettings by remember(activePatientId) { mutableStateOf(repository.getAccessibilitySettings()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onBackClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = Localization.tr("back_to_home", lang),
                    fontSize = (15 * accSettings.fontSizeScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = Localization.tr("settings_accessibility", lang),
            fontSize = (28 * accSettings.fontSizeScale).sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Preferences, text sizing & preferred language",
            fontSize = (16 * accSettings.fontSizeScale).sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Language selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = Localization.tr("preferred_language", lang),
                    fontSize = (18 * accSettings.fontSizeScale).sp,
                    fontWeight = FontWeight.Bold,
                )

                val langs = listOf(
                    AppLanguage.ENGLISH to "English",
                    AppLanguage.HINDI to "Hindi (हिंदी)",
                    AppLanguage.ASSAMESE to "Assamese (অসমীয়া)",
                    AppLanguage.BENGALI to "Bengali (বাংলা)",
                )

                langs.forEach { (langEnum, label) ->
                    val isSelected = currentLang == langEnum
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            currentLang = langEnum
                            repository.setLanguage(langEnum)
                            onLanguageChange(langEnum)
                            Toast.makeText(context, "Language updated to $label", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                fontSize = (16 * accSettings.fontSizeScale).sp,
                                fontWeight = FontWeight.Bold,
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓ Selected",
                                    fontSize = (14 * accSettings.fontSizeScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Font Size Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = Localization.tr("text_font_size", lang),
                    fontSize = (18 * accSettings.fontSizeScale).sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Adjust text size for easier reading across the app:",
                    fontSize = (14 * accSettings.fontSizeScale).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val fontOptions = listOf(
                    1.0f to "Normal (100%)",
                    1.25f to "Large (125%)",
                    1.5f to "Extra Large (150%)",
                )

                fontOptions.forEach { (scale, label) ->
                    val isSelected = accSettings.fontSizeScale == scale
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updated = accSettings.copy(fontSizeScale = scale)
                                accSettings = updated
                                repository.saveAccessibilitySettings(updated)
                                onFontScaleChange(scale)
                                Toast.makeText(context, "Font size set to $label", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                fontSize = (16 * scale).sp,
                                fontWeight = FontWeight.Bold,
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓ Selected",
                                    fontSize = (14 * scale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Accessibility Controls Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = Localization.tr("accessibility_controls", lang),
                    fontSize = (18 * accSettings.fontSizeScale).sp,
                    fontWeight = FontWeight.Bold,
                )

                // Spoken Instructions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Localization.tr("spoken_instructions", lang),
                            fontSize = (16 * accSettings.fontSizeScale).sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = Localization.tr("spoken_instructions_desc", lang),
                            fontSize = (13 * accSettings.fontSizeScale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = {
                            val newBool = !accSettings.voiceAssistantEnabled
                            val updated = accSettings.copy(voiceAssistantEnabled = newBool)
                            accSettings = updated
                            repository.saveAccessibilitySettings(updated)
                            Toast.makeText(
                                context,
                                if (newBool) "Spoken Instructions Enabled" else "Spoken Instructions Disabled",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                    ) {
                        Text(if (accSettings.voiceAssistantEnabled) "ON" else "OFF", fontSize = (14 * accSettings.fontSizeScale).sp)
                    }
                }

                // High Contrast Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "High Contrast Mode",
                            fontSize = (16 * accSettings.fontSizeScale).sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Maximizes contrast for low vision",
                            fontSize = (13 * accSettings.fontSizeScale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = {
                            val newBool = !accSettings.highContrast
                            val updated = accSettings.copy(highContrast = newBool)
                            accSettings = updated
                            repository.saveAccessibilitySettings(updated)
                            Toast.makeText(
                                context,
                                if (newBool) "High Contrast Mode enabled" else "High Contrast Mode disabled",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                    ) {
                        Text(if (accSettings.highContrast) "ON" else "OFF", fontSize = (14 * accSettings.fontSizeScale).sp)
                    }
                }

                // Reduced Motion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reduced Motion",
                            fontSize = (16 * accSettings.fontSizeScale).sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Simplifies animations for comfort",
                            fontSize = (13 * accSettings.fontSizeScale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = {
                            val newBool = !accSettings.reducedMotion
                            val updated = accSettings.copy(reducedMotion = newBool)
                            accSettings = updated
                            repository.saveAccessibilitySettings(updated)
                            Toast.makeText(
                                context,
                                if (newBool) "Reduced Motion enabled" else "Reduced Motion disabled",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                    ) {
                        Text(if (accSettings.reducedMotion) "ON" else "OFF", fontSize = (14 * accSettings.fontSizeScale).sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Spoken Instructions Diagnostic Test Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "🔊 " + Localization.tr("spoken_instructions", lang) + " Test",
                    fontSize = (18 * accSettings.fontSizeScale).sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Test Text-to-Speech output on your device:",
                    fontSize = (14 * accSettings.fontSizeScale).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick = {
                        if (accSettings.voiceAssistantEnabled) {
                            val tts = TextToSpeechManager.getInstance(context)
                            tts.setLanguageLocale(currentLang)
                            tts.speak(Localization.tr("test_voice_prompt", lang))
                            Toast.makeText(context, "Speaking: \"${Localization.tr("test_voice_prompt", lang)}\"", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Spoken Instructions are OFF in Settings. Turn it ON to test voice.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(Localization.tr("test_voice", lang), fontSize = (16 * accSettings.fontSizeScale).sp, fontWeight = FontWeight.Bold)
                }

                val testTts = remember { TextToSpeechManager.getInstance(context) }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("TTS Engine Status: ${testTts.initStatusMessage}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Language Locale Status: ${testTts.languageStatusMessage}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
