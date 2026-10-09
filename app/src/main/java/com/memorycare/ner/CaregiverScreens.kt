package com.memorycare.ner

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.util.UUID

// ==========================================
// FIRST-TIME CAREGIVER SETUP SCREEN
// ==========================================

@Composable
fun CaregiverSetupScreen(
    onContinue: (AppLanguage) -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    var selectedLang by remember { mutableStateOf(repository.getLanguage()) }

    val languages = listOf(
        AppLanguage.ENGLISH to "English",
        AppLanguage.HINDI to "हिन्दी — Hindi",
        AppLanguage.ASSAMESE to "অসমীয়া — Assamese",
        AppLanguage.BENGALI to "বাংলা — Bengali",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp),
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
                text = "Welcome to NER-MemoryCare",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Let's set up the app for the patient and caregiver.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Language Selection Card
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
                        text = "Select Default Language",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Text(
                        text = "Choose the initial default language for the application and patient interactions:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    languages.forEach { (langEnum, label) ->
                        val isSelected = selectedLang == langEnum
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLang = langEnum
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (isSelected) "✓ $label" else label,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Continue Button
        Button(
            onClick = {
                repository.setLanguage(selectedLang)
                repository.setCaregiverSetupCompleted(true)
                TextToSpeechManager.getInstance(context).setLanguageLocale(selectedLang)
                onContinue(selectedLang)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(text = "Continue →", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CaregiverSectionCard(
    title: String,
    icon: String,
    content: @Composable () -> Unit,
) {
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
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Text(text = icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            content()
        }
    }
}

@Composable
fun MainCaregiverDashboardScreen(
    onBackClick: () -> Unit,
    onManageMemoriesClick: () -> Unit,
    onOpenGameMediaUploads: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val memoryRepo = remember { MemoryRepository(context) }

    var patientsList by remember { mutableStateOf(repository.getPatients()) }
    val activePatientId = repository.getActivePatientId() ?: patientsList.firstOrNull()?.id ?: "p1"
    var selectedPatientId by remember(activePatientId) { mutableStateOf(activePatientId) }

    val currentPatient = patientsList.firstOrNull { it.id == selectedPatientId } ?: patientsList.first()

    var demoInactiveDays by remember { mutableIntStateOf(repository.getDemoModeInactiveDays()) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showAddPatientDialog by remember { mutableStateOf(false) }
    var showPatientSelectorDialog by remember { mutableStateOf(false) }
    var patientToDelete by remember { mutableStateOf<PatientProfile?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var patientToSwitch by remember { mutableStateOf<PatientProfile?>(null) }
    var showChangePatientConfirmDialog by remember { mutableStateOf(false) }
    var showAddSongDialog by remember { mutableStateOf(false) }
    var songListVersion by remember { mutableIntStateOf(0) }

    // State for Section 5: Image Sets & Media Uploads
    var showCreateSetDialog by remember { mutableStateOf(false) }
    var targetGameForNewSet by remember { mutableStateOf(GameType.FLIP_MATCH) }
    var newSetNameInput by remember { mutableStateOf("") }
    var targetSetForUpload by remember { mutableStateOf<GameImageSetItem?>(null) }

    var showUploadGameImageDialog by remember { mutableStateOf(false) }
    var tempGameImagePath by remember { mutableStateOf("") }
    var gameImageTitleInput by remember { mutableStateOf("") }
    var gameMemoriesVersion by remember { mutableIntStateOf(0) }

    val gamePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = memoryRepo.saveImageToInternalStorage(uri)
            if (savedPath != null) {
                tempGameImagePath = savedPath
                gameImageTitleInput = ""
                showUploadGameImageDialog = true
            } else {
                Toast.makeText(context, "Failed to load selected photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val recentSessions = remember { repository.getGameSessions() }
    val reminders = remember { repository.getReminders().toMutableList() }

    // Effective inactive days: either from selected patient or demo override
    val effectiveInactiveDays = if (demoInactiveDays > 0) demoInactiveDays else currentPatient.inactiveDays

    val aiReport = remember(effectiveInactiveDays, recentSessions, currentPatient) {
        ActivityMonitorEngine.generateWeeklyReport(recentSessions, reminders, effectiveInactiveDays)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back & Settings Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onBackClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = "← Back to Mode Select",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }

            Card(
                modifier = Modifier.clickable(onClick = onSettingsClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = "⚙️ Settings",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Caregiver Dashboard",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Patient Management, AI Monitoring & Customization",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CURRENT PATIENT CARD (DEVICE ASSIGNMENT)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "CURRENT PATIENT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Currently assigned to this device",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = currentPatient.avatarEmoji, fontSize = 34.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentPatient.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = "Age ${currentPatient.age} • ${currentPatient.condition}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            )
                        }
                    }

                    Button(
                        onClick = { showPatientSelectorDialog = true },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(text = "Change Patient", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DEMO MODE BANNER & CONTROLS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🛠️ DEMO MODE (Hackathon Controls)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                    )
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFB74D)) {
                        Text(
                            text = "DEMO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Simulate patient activity patterns to present AI Adaptive Difficulty and Inactivity Alerts live.",
                    fontSize = 13.sp,
                    color = Color(0xFFE65100),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            demoInactiveDays = 0
                            repository.setDemoModeInactiveDays(0)
                            Toast.makeText(context, "Demo Mode: Active Patient", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Active (0d)", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            demoInactiveDays = 4
                            repository.setDemoModeInactiveDays(4)
                            Toast.makeText(context, "Demo Mode: 4 Days Inactive Alert Triggered", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Inactive (4d)", fontSize = 12.sp, color = Color(0xFFD32F2F))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. PATIENT OVERVIEW SECTION
        CaregiverSectionCard(title = "1. Patient Overview", icon = "👴") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = currentPatient.avatarEmoji, fontSize = 32.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = currentPatient.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Age: ${currentPatient.age} • ${currentPatient.condition}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { showEditProfileDialog = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "Edit Profile")
                }
                OutlinedButton(
                    onClick = { showAddPatientDialog = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "➕ Add Patient")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. AI ACTIVITY MONITOR & ALERTS
        CaregiverSectionCard(title = "2. AI Activity Monitor & Alerts", icon = "⚠") {
            if (aiReport.alertMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "⚠ Activity Alert (${currentPatient.name})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = aiReport.alertMessage, fontSize = 14.sp, color = Color(0xFFC62828))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "✓ Activity pattern normal for ${currentPatient.name}. No inactivity alerts.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(14.dp),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(text = "Observation: " + aiReport.activityObservation, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. AI ADAPTIVE DIFFICULTY INSIGHTS
        CaregiverSectionCard(title = "3. AI Game Performance & Difficulty", icon = "📈") {
            aiReport.gameSummaries.forEach { gameSum ->
                val gameTitle = when (gameSum.gameType) {
                    GameType.REMEMBER_ROUTE -> "Remember My Route"
                    GameType.FLIP_MATCH -> "Flip & Match"
                    GameType.SPOT_REPEAT -> "Spot the Repeat"
                    GameType.GUESS_LYRICS -> "Guess the Lyrics"
                    else -> "Cognitive Activity"
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = gameTitle, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = "Level ${gameSum.level}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${gameSum.parameterDetail} • Trend: ${gameSum.trend} • Accuracy: ${gameSum.recentAccuracy.toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "AI Explanation: ${gameSum.explanationReason}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ℹ These insights describe app game activity performance and are not a medical assessment.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. PERSONALIZED CONTENT LIBRARY
        CaregiverSectionCard(title = "4. Personalized Content Library", icon = "🖼️") {
            Text(
                text = "Upload & manage photos of Food, Festivals, Clothes, Places & Routes for ${currentPatient.name}'s memory games.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onManageMemoriesClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Manage Personalized Photos & Content")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. GAME MEDIA UPLOAD CENTER
        CaregiverSectionCard(title = "5. Game Media Upload Center", icon = "📷") {
            Text(
                text = "Create and manage separate image sets for each game for ${currentPatient.name}. When ${currentPatient.name} plays, one eligible image set is randomly selected for the session.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenGameMediaUploads,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = "Open Game Media Uploads", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "›", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
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

    if (showAddSongDialog) {
        var songTitleInput by remember { mutableStateOf("") }
        var artistInput by remember { mutableStateOf("") }
        var lyricPromptInput by remember { mutableStateOf("") }
        var correctAnswerInput by remember { mutableStateOf("") }
        var optionBInput by remember { mutableStateOf("") }
        var optionCInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSongDialog = false },
            title = { Text(text = "Add Song & Lyric Prompt", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(value = songTitleInput, onValueChange = { songTitleInput = it }, label = { Text("Song Title (e.g. Bihu Folk Song)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = artistInput, onValueChange = { artistInput = it }, label = { Text("Artist / Tradition") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = lyricPromptInput, onValueChange = { lyricPromptInput = it }, label = { Text("Lyric Prompt / Question") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = correctAnswerInput, onValueChange = { correctAnswerInput = it }, label = { Text("Correct Next Line (Expected Answer)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = optionBInput, onValueChange = { optionBInput = it }, label = { Text("Wrong Choice 1") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = optionCInput, onValueChange = { optionCInput = it }, label = { Text("Wrong Choice 2") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (songTitleInput.isNotBlank() && lyricPromptInput.isNotBlank() && correctAnswerInput.isNotBlank()) {
                            val newSongId = UUID.randomUUID().toString()
                            val newSong = SongItem(
                                songId = newSongId,
                                patientId = selectedPatientId,
                                title = songTitleInput.trim(),
                                artist = artistInput.trim(),
                                language = currentPatient.preferredLanguage,
                                createdAt = System.currentTimeMillis(),
                            )
                            val newQuestion = LyricQuestionItem(
                                songId = newSongId,
                                patientId = selectedPatientId,
                                prompt = lyricPromptInput.trim(),
                                correctAnswer = correctAnswerInput.trim(),
                                optionA = correctAnswerInput.trim(),
                                optionB = if (optionBInput.isNotBlank()) optionBInput.trim() else "Option B",
                                optionC = if (optionCInput.isNotBlank()) optionCInput.trim() else "Option C",
                            )
                            repository.saveSongForPatient(selectedPatientId, newSong)
                            repository.saveLyricQuestionForPatient(selectedPatientId, newQuestion)
                            songListVersion++
                            showAddSongDialog = false
                            Toast.makeText(context, "Song & Lyrics saved for ${currentPatient.name}!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please fill in title, prompt, and correct answer", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text("Save Song")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddSongDialog = false }) { Text("Cancel") }
            },
        )
    }

    // Upload Game Image Details Dialog
    if (showUploadGameImageDialog && targetSetForUpload != null) {
        val targetSet = targetSetForUpload!!
        AlertDialog(
            onDismissRequest = { showUploadGameImageDialog = false },
            title = { Text(text = "Add Photo to ${targetSet.setName}", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (tempGameImagePath.isNotBlank()) {
                        val bitmap = remember(tempGameImagePath) {
                            try { BitmapFactory.decodeFile(tempGameImagePath) } catch (_: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                    OutlinedTextField(
                        value = gameImageTitleInput,
                        onValueChange = { gameImageTitleInput = it },
                        label = { Text("Photo Title / Name (e.g. Village Market)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (gameImageTitleInput.isNotBlank()) {
                            val newImg = GameImageItem(
                                patientId = selectedPatientId,
                                gameType = targetSet.gameType,
                                setId = targetSet.setId,
                                title = gameImageTitleInput.trim(),
                                localFilePath = tempGameImagePath,
                            )
                            repository.saveGameImageToSet(selectedPatientId, newImg)
                            gameMemoriesVersion++
                            showUploadGameImageDialog = false
                            Toast.makeText(context, "Saved photo to set!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a photo title", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text("Save Photo")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUploadGameImageDialog = false }) { Text("Cancel") }
            },
        )
    }

    // Patient Selector Dialog
    if (showPatientSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showPatientSelectorDialog = false },
            title = { Text(text = "Select Patient to View", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    patientsList.forEach { patient ->
                        val isSelected = patient.id == selectedPatientId
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Text(text = patient.avatarEmoji, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = patient.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            Text(text = "Age ${patient.age} • ${patient.condition}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (patient.inactiveDays >= 4) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                    ) {
                                        Text(
                                            text = if (patient.inactiveDays >= 4) "4d Inactive" else "Active",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (patient.inactiveDays >= 4) Color(0xFFC62828) else Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Button(
                                        onClick = {
                                            if (patient.id != selectedPatientId) {
                                                patientToSwitch = patient
                                                showChangePatientConfirmDialog = true
                                            } else {
                                                showPatientSelectorDialog = false
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Text(text = if (isSelected) "✓ Assigned" else "Select Patient", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            patientToDelete = patient
                                            showDeleteConfirmDialog = true
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Text(text = "🗑️ Delete", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            showPatientSelectorDialog = false
                            showAddPatientDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = "➕ Add New Patient Profile")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showPatientSelectorDialog = false }) { Text("Close") }
            },
        )
    }

    // Change Patient Confirmation Dialog
    if (showChangePatientConfirmDialog && patientToSwitch != null) {
        val targetPatient = patientToSwitch!!
        AlertDialog(
            onDismissRequest = {
                showChangePatientConfirmDialog = false
                patientToSwitch = null
            },
            title = {
                Text(text = "Change Patient?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Change this device assignment from ${currentPatient.name} to ${targetPatient.name}?",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.setActivePatientId(targetPatient.id)
                        repository.setLanguage(targetPatient.preferredLanguage)
                        TextToSpeechManager.getInstance(context).setLanguageLocale(targetPatient.preferredLanguage)
                        selectedPatientId = targetPatient.id
                        showChangePatientConfirmDialog = false
                        showPatientSelectorDialog = false
                        patientToSwitch = null
                        Toast.makeText(context, "Assigned device to ${targetPatient.name}", Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showChangePatientConfirmDialog = false
                        patientToSwitch = null
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && patientToDelete != null) {
        val targetPatient = patientToDelete!!
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                patientToDelete = null
            },
            title = {
                Text(text = "Delete Patient?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${targetPatient.name}?\n\nThis will remove this patient's profile and associated local data.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deletePatient(targetPatient.id)
                        patientsList = repository.getPatients()

                        if (selectedPatientId == targetPatient.id) {
                            if (patientsList.isNotEmpty()) {
                                selectedPatientId = patientsList.first().id
                            } else {
                                val fallback = PatientProfile(id = "p1", name = "Bhaben Sarma", age = "74", condition = "Early-stage Dementia", avatarEmoji = "🧓", preferredLanguage = AppLanguage.ASSAMESE)
                                repository.savePatient(fallback)
                                patientsList = repository.getPatients()
                                selectedPatientId = fallback.id
                            }
                        }

                        showDeleteConfirmDialog = false
                        patientToDelete = null
                        Toast.makeText(context, "Deleted profile for ${targetPatient.name}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        patientToDelete = null
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    // Add Patient Dialog
    if (showAddPatientDialog) {
        var newName by remember { mutableStateOf("") }
        var newAge by remember { mutableStateOf("") }
        var newCondition by remember { mutableStateOf("Early-stage Dementia") }
        var newLang by remember { mutableStateOf(repository.getLanguage()) }

        AlertDialog(
            onDismissRequest = { showAddPatientDialog = false },
            title = { Text(text = "Add New Patient Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Patient Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newAge, onValueChange = { newAge = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newCondition, onValueChange = { newCondition = it }, label = { Text("Condition / Notes") }, modifier = Modifier.fillMaxWidth())

                    Text(text = "Preferred Language:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(
                            AppLanguage.ENGLISH to "EN",
                            AppLanguage.HINDI to "HI",
                            AppLanguage.ASSAMESE to "AS",
                            AppLanguage.BENGALI to "BN",
                        ).forEach { (langEnum, label) ->
                            val isSel = newLang == langEnum
                            Button(
                                onClick = { newLang = langEnum },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ) {
                                Text(label, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val newPatient = PatientProfile(
                                name = newName.trim(),
                                age = newAge.trim(),
                                condition = newCondition.trim(),
                                avatarEmoji = if (patientsList.size % 2 == 0) "👴" else "👵",
                                preferredLanguage = newLang,
                            )
                            repository.savePatient(newPatient)
                            patientsList = repository.getPatients()
                            selectedPatientId = newPatient.id
                            showAddPatientDialog = false
                            Toast.makeText(context, "Added ${newPatient.name}", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddPatientDialog = false }) { Text("Cancel") }
            },
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(currentPatient.name) }
        var ageInput by remember { mutableStateOf(currentPatient.age) }
        var conditionInput by remember { mutableStateOf(currentPatient.condition) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text(text = "Edit Patient Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Patient Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ageInput, onValueChange = { ageInput = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = conditionInput, onValueChange = { conditionInput = it }, label = { Text("Condition / Notes") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = currentPatient.copy(name = nameInput, age = ageInput, condition = conditionInput)
                        repository.savePatient(updated)
                        patientsList = repository.getPatients()
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditProfileDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showAddReminderDialog) {
        var titleInput by remember { mutableStateOf("") }
        var msgInput by remember { mutableStateOf("") }
        var timeInput by remember { mutableStateOf("08:00 AM") }

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text(text = "Add New Reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = titleInput, onValueChange = { titleInput = it }, label = { Text("Title (e.g. Medicine)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = msgInput, onValueChange = { msgInput = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = timeInput, onValueChange = { timeInput = it }, label = { Text("Scheduled Time (e.g. 08:00 AM)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            val newRem = ReminderItem(
                                type = ReminderType.CUSTOM,
                                title = titleInput.trim(),
                                message = msgInput.trim(),
                                scheduledTime = timeInput.trim(),
                            )
                            repository.saveReminderItem(newRem)
                            reminders.add(newRem)
                            showAddReminderDialog = false
                            Toast.makeText(context, "Reminder created", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddReminderDialog = false }) { Text("Cancel") }
            },
        )
    }
}

// ==========================================
// GAME MEDIA UPLOAD MENU SCREEN (SCREEN 2)
// ==========================================

@Composable
fun GameMediaUploadMenuScreen(
    onBackClick: () -> Unit,
    onSelectGame: (GameType) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onBackClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = "← Back to Dashboard",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Game Media Upload",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Manage image sets for each game separately. Each game uses its own uploaded images.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Four Game Selection Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Flip & Match
            GameUploadCardItem(
                icon = "🧩",
                title = "Flip & Match",
                subtitle = "Upload and manage image sets for Flip & Match game.",
                onClick = { onSelectGame(GameType.FLIP_MATCH) },
            )

            // 2. Remember My Route
            GameUploadCardItem(
                icon = "🖼️",
                title = "Remember My Route",
                subtitle = "Upload and manage image sets for Remember My Route game.",
                onClick = { onSelectGame(GameType.REMEMBER_ROUTE) },
            )

            // 3. Spot the Repeat
            GameUploadCardItem(
                icon = "👁️",
                title = "Spot the Repeat",
                subtitle = "Upload and manage image sets for Spot the Repeat game.",
                onClick = { onSelectGame(GameType.SPOT_REPEAT) },
            )

            // 4. Guess the Lyrics
            GameUploadCardItem(
                icon = "🎵",
                title = "Guess the Lyrics",
                subtitle = "Upload audio tracks, song titles and lyric prompts for Guess the Lyrics game.",
                onClick = { onSelectGame(GameType.GUESS_LYRICS) },
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Info Banner Card at Bottom
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "i", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Each game will randomly select one eligible image set for the session.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
fun GameUploadCardItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = icon, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = "›",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

// ==========================================
// GAME IMAGE SETS DETAIL SCREEN (SCREENS 3, 4, 5)
// ==========================================

@Composable
fun GameImageSetsDetailScreen(
    gameType: GameType,
    onBackClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val repository = remember { NERRepository(context) }
    val memoryRepo = remember { MemoryRepository(context) }

    val activePatientId = repository.getActivePatientId() ?: "p1"
    val currentPatient = remember(activePatientId) { repository.getPatientById(activePatientId) }

    var gameMemoriesVersion by remember { mutableIntStateOf(0) }
    var showCreateSetDialog by remember { mutableStateOf(false) }
    var newSetNameInput by remember { mutableStateOf("") }
    var targetSetForUpload by remember { mutableStateOf<GameImageSetItem?>(null) }
    var tempGameImagePath by remember { mutableStateOf("") }
    var gameImageTitleInput by remember { mutableStateOf("") }
    var showUploadGameImageDialog by remember { mutableStateOf(false) }

    val gamePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = memoryRepo.saveImageToInternalStorage(uri)
            if (savedPath != null) {
                tempGameImagePath = savedPath
                gameImageTitleInput = ""
                showUploadGameImageDialog = true
            } else {
                Toast.makeText(context, "Failed to load selected photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val gameTitleText = when (gameType) {
        GameType.FLIP_MATCH -> "Flip & Match - Image Sets"
        GameType.REMEMBER_ROUTE -> "Remember My Route - Sets"
        GameType.SPOT_REPEAT -> "Spot the Repeat - Image Sets"
        GameType.GUESS_LYRICS -> "Guess the Lyrics - Songs"
        else -> "Game Image Sets"
    }

    val gameIcon = when (gameType) {
        GameType.FLIP_MATCH -> "🧩"
        GameType.REMEMBER_ROUTE -> "🖼️"
        GameType.SPOT_REPEAT -> "👁️"
        GameType.GUESS_LYRICS -> "🎵"
        else -> "📷"
    }

    val setsForGame = remember(activePatientId, gameType, gameMemoriesVersion) {
        repository.getImageSetsForGame(activePatientId, gameType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back Header
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
                    text = "← Back",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title Header Card
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = gameIcon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = gameTitleText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (gameType == GameType.GUESS_LYRICS) {
                    "Add audio tracks, song titles, and lyric prompts for Guess the Lyrics game."
                } else {
                    "Create and manage image sets for ${when (gameType) { GameType.FLIP_MATCH -> "Flip & Match"; GameType.REMEMBER_ROUTE -> "Remember My Route"; else -> "Spot the Repeat" }} game."
                },
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (gameType == GameType.GUESS_LYRICS) {
            var songListVersion by remember { mutableIntStateOf(0) }
            var showAddSongDialog by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showAddSongDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(text = "+ Add Song & Lyric Prompt", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            val patientSongs = remember(activePatientId, songListVersion) {
                repository.getSongsForPatient(activePatientId)
            }

            if (patientSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No custom songs added yet for ${currentPatient?.name ?: "Patient"}.\nTap '+ Add Song & Lyric Prompt' above.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    patientSongs.forEach { song ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "🎵 ${song.title}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "Artist: ${song.artist} • ${song.language.name}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                OutlinedButton(
                                    onClick = {
                                        repository.deleteSongForPatient(activePatientId, song.songId)
                                        songListVersion++
                                        Toast.makeText(context, "Removed ${song.title}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.height(38.dp),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Text("Delete", fontSize = 12.sp, color = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                }
            }

            if (showAddSongDialog) {
                var songTitleInput by remember { mutableStateOf("") }
                var artistInput by remember { mutableStateOf("") }
                var lyricPromptInput by remember { mutableStateOf("") }
                var correctAnswerInput by remember { mutableStateOf("") }
                var optionBInput by remember { mutableStateOf("") }
                var optionCInput by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showAddSongDialog = false },
                    title = { Text(text = "Add Song & Lyric Prompt", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedTextField(value = songTitleInput, onValueChange = { songTitleInput = it }, label = { Text("Song Title (e.g. Bihu Folk Song)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = artistInput, onValueChange = { artistInput = it }, label = { Text("Artist / Tradition") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = lyricPromptInput, onValueChange = { lyricPromptInput = it }, label = { Text("Lyric Prompt / Question") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = correctAnswerInput, onValueChange = { correctAnswerInput = it }, label = { Text("Correct Next Line (Expected Answer)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = optionBInput, onValueChange = { optionBInput = it }, label = { Text("Wrong Choice 1") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = optionCInput, onValueChange = { optionCInput = it }, label = { Text("Wrong Choice 2") }, modifier = Modifier.fillMaxWidth())
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (songTitleInput.isNotBlank() && lyricPromptInput.isNotBlank() && correctAnswerInput.isNotBlank()) {
                                    val newSongId = UUID.randomUUID().toString()
                                    val newSong = SongItem(
                                        songId = newSongId,
                                        patientId = activePatientId,
                                        title = songTitleInput.trim(),
                                        artist = artistInput.trim(),
                                        language = currentPatient?.preferredLanguage ?: AppLanguage.ENGLISH,
                                        createdAt = System.currentTimeMillis(),
                                    )
                                    val newQuestion = LyricQuestionItem(
                                        songId = newSongId,
                                        patientId = activePatientId,
                                        prompt = lyricPromptInput.trim(),
                                        correctAnswer = correctAnswerInput.trim(),
                                        optionA = correctAnswerInput.trim(),
                                        optionB = if (optionBInput.isNotBlank()) optionBInput.trim() else "Option B",
                                        optionC = if (optionCInput.isNotBlank()) optionCInput.trim() else "Option C",
                                    )
                                    repository.saveSongForPatient(activePatientId, newSong)
                                    repository.saveLyricQuestionForPatient(activePatientId, newQuestion)
                                    songListVersion++
                                    showAddSongDialog = false
                                    Toast.makeText(context, "Song & Lyric Prompt saved!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please fill in title, prompt, and correct answer", Toast.LENGTH_SHORT).show()
                                }
                            },
                        ) { Text("Save Song") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showAddSongDialog = false }) { Text("Cancel") }
                    },
                )
            }
            return
        }

        // Create New Set Button (Outlined)
        OutlinedButton(
            onClick = {
                newSetNameInput = ""
                showCreateSetDialog = true
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(text = "+ Create New Set", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sets List
        if (setsForGame.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No custom image sets created yet.\nTap '+ Create New Set' above to add your first set.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                setsForGame.forEach { setItem ->
                    val setImages = remember(activePatientId, setItem.setId, gameMemoriesVersion) {
                        repository.getImagesForSet(activePatientId, gameType, setItem.setId)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = setItem.setName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "${setImages.size} images",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Text(text = "⋮", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Horizontal / Grid Thumbnails
                            if (setImages.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    val previewImages = setImages.take(4)
                                    previewImages.forEach { imgItem ->
                                        Card(
                                            modifier = Modifier.weight(1f).height(80.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                val bitmap = remember(imgItem.localFilePath) {
                                                    try { BitmapFactory.decodeFile(imgItem.localFilePath) } catch (_: Exception) { null }
                                                }
                                                if (bitmap != null) {
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = imgItem.title,
                                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                                        contentScale = ContentScale.Crop,
                                                    )
                                                } else {
                                                    Text(text = if (imgItem.localFilePath.length <= 4) imgItem.localFilePath else "🖼️", fontSize = 28.sp)
                                                }
                                            }
                                        }
                                    }
                                    if (previewImages.size < 4) {
                                        for (i in 0 until (4 - previewImages.size)) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Button(
                                    onClick = {
                                        targetSetForUpload = setItem
                                        gamePhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text("Add Photos", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        repository.deleteGameImageSet(activePatientId, gameType, setItem.setId)
                                        gameMemoriesVersion++
                                        Toast.makeText(context, "Deleted ${setItem.setName}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text("Delete Set", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog 1: Create Set
    if (showCreateSetDialog) {
        AlertDialog(
            onDismissRequest = { showCreateSetDialog = false },
            title = { Text(text = "Create New Set", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newSetNameInput,
                        onValueChange = { newSetNameInput = it },
                        label = { Text("Set Name (e.g. Set 1: Family)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSetNameInput.isNotBlank()) {
                            val newSet = GameImageSetItem(
                                patientId = activePatientId,
                                gameType = gameType,
                                setName = newSetNameInput.trim(),
                            )
                            repository.saveGameImageSet(activePatientId, newSet)
                            gameMemoriesVersion++
                            showCreateSetDialog = false
                            Toast.makeText(context, "Created ${newSet.setName}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a set name", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) { Text("Create") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateSetDialog = false }) { Text("Cancel") }
            },
        )
    }

    // Dialog 2: Add Photo to Set
    if (showUploadGameImageDialog && targetSetForUpload != null) {
        val targetSet = targetSetForUpload!!
        AlertDialog(
            onDismissRequest = { showUploadGameImageDialog = false },
            title = { Text(text = "Add Photo to ${targetSet.setName}", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (tempGameImagePath.isNotBlank()) {
                        val bitmap = remember(tempGameImagePath) {
                            try { BitmapFactory.decodeFile(tempGameImagePath) } catch (_: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                    OutlinedTextField(
                        value = gameImageTitleInput,
                        onValueChange = { gameImageTitleInput = it },
                        label = { Text("Photo Title / Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (gameImageTitleInput.isNotBlank()) {
                            val newImg = GameImageItem(
                                patientId = activePatientId,
                                gameType = targetSet.gameType,
                                setId = targetSet.setId,
                                title = gameImageTitleInput.trim(),
                                localFilePath = tempGameImagePath,
                            )
                            repository.saveGameImageToSet(activePatientId, newImg)
                            gameMemoriesVersion++
                            showUploadGameImageDialog = false
                            Toast.makeText(context, "Added photo to ${targetSet.setName}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a photo title", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) { Text("Save Photo") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUploadGameImageDialog = false }) { Text("Cancel") }
            },
        )
    }
}

// ==========================================
// MANAGE MEMORIES SCREEN (PHOTO MANAGEMENT)
// ==========================================

@Composable
fun ManageMemoriesScreen(
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { MemoryRepository(context) }
    var memories by remember { mutableStateOf(repository.getAllMemories()) }

    var showFormDialog by remember { mutableStateOf(false) }
    var editingMemoryId by remember { mutableStateOf<String?>(null) }
    var tempImagePath by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Family") }
    var descriptionInput by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = repository.saveImageToInternalStorage(uri)
            if (savedPath != null) {
                tempImagePath = savedPath
                nameInput = ""
                categoryInput = "Family"
                descriptionInput = ""
                editingMemoryId = null
                showFormDialog = true
            } else {
                Toast.makeText(context, "Failed to load selected photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "← Back to Dashboard",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Manage Memories",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Photos added here can be used in Remember My Route to help the patient remember familiar people, places and objects.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(text = "➕ Add Photo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (memories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No memories added yet.\nTap 'Add Photo' above to get started.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                memories.forEach { memory ->
                    MemoryCardItem(
                        memory = memory,
                        onEdit = {
                            editingMemoryId = memory.id
                            tempImagePath = memory.imagePath
                            nameInput = memory.name
                            categoryInput = memory.category
                            descriptionInput = memory.description
                            showFormDialog = true
                        },
                        onDelete = {
                            repository.deleteMemory(memory.id)
                            memories = repository.getAllMemories()
                            Toast.makeText(context, "Memory deleted", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
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

    if (showFormDialog) {
        val categories = listOf("Family", "Friend", "Place", "Other")

        AlertDialog(
            onDismissRequest = { showFormDialog = false },
            title = { Text(text = if (editingMemoryId == null) "Add Memory Details" else "Edit Memory Details") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (tempImagePath.isNotBlank()) {
                        val bitmap = remember(tempImagePath) {
                            try {
                                BitmapFactory.decodeFile(tempImagePath)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Photo Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Person / Place Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Text(text = "Relationship / Category:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        categories.forEach { cat ->
                            val isSelected = categoryInput == cat
                            if (isSelected) {
                                Button(
                                    onClick = { categoryInput = cat },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(text = cat, fontSize = 12.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { categoryInput = cat },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(text = cat, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Optional Description / Story") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            val memoryItem = MemoryItem(
                                id = editingMemoryId ?: UUID.randomUUID().toString(),
                                imagePath = tempImagePath,
                                name = nameInput.trim(),
                                category = categoryInput,
                                description = descriptionInput.trim(),
                            )
                            repository.saveMemory(memoryItem)
                            memories = repository.getAllMemories()
                            showFormDialog = false
                            Toast.makeText(context, "Memory saved successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showFormDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
fun MemoryCardItem(
    memory: MemoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val bitmap = remember(memory.imagePath) {
                try {
                    BitmapFactory.decodeFile(memory.imagePath)
                } catch (_: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = memory.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "📷", fontSize = 28.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = memory.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = memory.category,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (memory.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = memory.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.height(36.dp),
                ) {
                    Text(text = "Edit", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.height(36.dp),
                ) {
                    Text(text = "Delete", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
