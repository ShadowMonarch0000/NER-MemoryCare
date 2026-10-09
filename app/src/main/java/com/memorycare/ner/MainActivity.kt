package com.memorycare.ner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.memorycare.ner.ui.theme.NERMemoryCareTheme

sealed class AppScreen {
    object Start : AppScreen()
    object PatientSelection : AppScreen()
    object CaregiverSetup : AppScreen()
    object PatientHome : AppScreen()
    object CognitiveGamesList : AppScreen()
    object RememberRouteGame : AppScreen()
    object FlipMatchGame : AppScreen()
    object GuessTheLyricsGame : AppScreen()
    object SpotTheRepeatGame : AppScreen()
    object PatientReminders : AppScreen()
    object PatientProgress : AppScreen()
    object PatientMemories : AppScreen()
    object PatientSettings : AppScreen()
    object CaregiverDashboard : AppScreen()
    object ManageMemories : AppScreen()
    object GameMediaUploadMenu : AppScreen()
    data class GameImageSetsDetail(val gameType: GameType) : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NERMemoryCareTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFF9FAFC),
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = Color(0xFFF9FAFC),
                    ) {
                        val context = LocalContext.current
                        val repository = remember { NERRepository(context) }

                        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Start) }
                        var previousScreenBeforeSettings by remember { mutableStateOf<AppScreen>(AppScreen.PatientHome) }
                        var showPinDialog by remember { mutableStateOf(false) }

                        var activePatientId by remember { mutableStateOf(repository.getActivePatientId()) }
                        var currentLang by remember { mutableStateOf(repository.getLanguage()) }
                        var fontScale by remember { mutableStateOf(repository.getAccessibilitySettings().fontSizeScale) }

                        when (currentScreen) {
                            is AppScreen.Start -> {
                                activePatientId = repository.getActivePatientId()
                                currentLang = repository.getLanguage()
                                StartScreen(
                                    onPatientClick = {
                                        if (repository.getActivePatientId() == null) {
                                            currentScreen = AppScreen.PatientSelection
                                        } else {
                                            currentScreen = AppScreen.PatientHome
                                        }
                                    },
                                    onCaregiverClick = { showPinDialog = true },
                                    lang = currentLang,
                                )
                            }

                            is AppScreen.PatientSelection -> {
                                PatientSelectionScreen(
                                    onPatientSelected = { patient ->
                                        repository.setActivePatientId(patient.id)
                                        activePatientId = patient.id
                                        currentLang = patient.preferredLanguage
                                        currentScreen = AppScreen.PatientHome
                                    },
                                    onBackClick = { currentScreen = AppScreen.Start },
                                    lang = currentLang,
                                )
                            }

                            is AppScreen.CaregiverSetup -> {
                                CaregiverSetupScreen(
                                    onContinue = { selectedLang ->
                                        currentLang = selectedLang
                                        currentScreen = AppScreen.CaregiverDashboard
                                    },
                                )
                            }

                            is AppScreen.PatientHome -> {
                                activePatientId = repository.getActivePatientId()
                                PatientHomeScreen(
                                    onBackClick = { currentScreen = AppScreen.Start },
                                    onStartGameClick = { currentScreen = AppScreen.CognitiveGamesList },
                                    onRemindersClick = { currentScreen = AppScreen.PatientReminders },
                                    onProgressClick = { currentScreen = AppScreen.PatientProgress },
                                    onMemoriesClick = { currentScreen = AppScreen.PatientMemories },
                                    onSettingsClick = {
                                        previousScreenBeforeSettings = AppScreen.PatientHome
                                        currentScreen = AppScreen.PatientSettings
                                    },
                                    lang = currentLang,
                                    patientId = activePatientId,
                                )
                            }

                            is AppScreen.CognitiveGamesList -> {
                                CognitiveGamesListScreen(
                                    onBackClick = { currentScreen = AppScreen.PatientHome },
                                    onOpenRouteGame = { currentScreen = AppScreen.RememberRouteGame },
                                    onOpenFlipMatchGame = { currentScreen = AppScreen.FlipMatchGame },
                                    onOpenGuessLyricsGame = { currentScreen = AppScreen.GuessTheLyricsGame },
                                    onOpenSpotRepeatGame = { currentScreen = AppScreen.SpotTheRepeatGame },
                                    lang = currentLang,
                                )
                            }

                            is AppScreen.RememberRouteGame -> {
                                RememberRouteGameScreen(
                                    onBackClick = { currentScreen = AppScreen.CognitiveGamesList },
                                    lang = currentLang,
                                    patientId = activePatientId,
                                )
                            }

                            is AppScreen.FlipMatchGame -> {
                                MemoryGamePlayScreen(
                                    onBackClick = { currentScreen = AppScreen.CognitiveGamesList },
                                    patientId = activePatientId,
                                )
                            }

                            is AppScreen.GuessTheLyricsGame -> {
                                GuessTheLyricsGameScreen(
                                    onBackClick = { currentScreen = AppScreen.CognitiveGamesList },
                                    lang = currentLang,
                                    patientId = activePatientId,
                                )
                            }

                            is AppScreen.SpotTheRepeatGame -> {
                                SpotTheRepeatGameScreen(
                                    onBackClick = { currentScreen = AppScreen.CognitiveGamesList },
                                    lang = currentLang,
                                    patientId = activePatientId,
                                )
                            }

                            is AppScreen.PatientReminders -> {
                                PatientRemindersScreen(
                                    onBackClick = { currentScreen = AppScreen.PatientHome },
                                    lang = currentLang,
                                    patientId = activePatientId,
                                )
                            }

                            is AppScreen.PatientProgress -> {
                                PatientProgressScreen(
                                    onBackClick = { currentScreen = AppScreen.PatientHome },
                                    lang = currentLang,
                                    patientId = activePatientId,
                                )
                            }

                            is AppScreen.PatientMemories -> {
                                PatientMemoriesScreen(
                                    onBackClick = { currentScreen = AppScreen.PatientHome },
                                    lang = currentLang,
                                    patientId = activePatientId,
                                )
                            }

                            is AppScreen.PatientSettings -> {
                                PatientSettingsScreen(
                                    onBackClick = { currentScreen = previousScreenBeforeSettings },
                                    onLanguageChange = { currentLang = it },
                                    onFontScaleChange = { fontScale = it },
                                    lang = currentLang,
                                    patientId = activePatientId,
                                )
                            }

                            is AppScreen.CaregiverDashboard -> {
                                MainCaregiverDashboardScreen(
                                    onBackClick = {
                                        activePatientId = repository.getActivePatientId()
                                        currentLang = repository.getLanguage()
                                        currentScreen = AppScreen.Start
                                    },
                                    onManageMemoriesClick = { currentScreen = AppScreen.ManageMemories },
                                    onOpenGameMediaUploads = { currentScreen = AppScreen.GameMediaUploadMenu },
                                    onSettingsClick = {
                                        previousScreenBeforeSettings = AppScreen.CaregiverDashboard
                                        currentScreen = AppScreen.PatientSettings
                                    },
                                )
                            }

                            is AppScreen.GameMediaUploadMenu -> {
                                GameMediaUploadMenuScreen(
                                    onBackClick = { currentScreen = AppScreen.CaregiverDashboard },
                                    onSelectGame = { selectedGameType ->
                                        currentScreen = AppScreen.GameImageSetsDetail(selectedGameType)
                                    },
                                )
                            }

                            is AppScreen.GameImageSetsDetail -> {
                                GameImageSetsDetailScreen(
                                    gameType = (currentScreen as AppScreen.GameImageSetsDetail).gameType,
                                    onBackClick = { currentScreen = AppScreen.GameMediaUploadMenu },
                                )
                            }

                            is AppScreen.ManageMemories -> {
                                ManageMemoriesScreen(
                                    onBackClick = { currentScreen = AppScreen.CaregiverDashboard },
                                )
                            }
                        }

                        if (showPinDialog) {
                            PinLoginDialog(
                                correctPin = repository.getCaregiverPin(),
                                onSuccess = {
                                    showPinDialog = false
                                    if (!repository.isCaregiverSetupCompleted()) {
                                        currentScreen = AppScreen.CaregiverSetup
                                    } else {
                                        currentScreen = AppScreen.CaregiverDashboard
                                    }
                                },
                                onDismiss = {
                                    showPinDialog = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
