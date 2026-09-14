package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.RepairViewModel
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.GeminiChatbotModal
import com.example.ui.components.SafetyDisclaimerDialog
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DiagnosisScreen
import com.example.ui.screens.GlossaryScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.OfflineSkillPacksScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StartScreen
import com.example.ui.screens.WorkbenchScreen
import com.example.ui.screens.WorkbenchToolsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: RepairViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val activeDiagnosis by viewModel.activeDiagnosis.collectAsState()
    val showWorkbench by viewModel.showWorkbench.collectAsState()
    val showGlossary by viewModel.showGlossary.collectAsState()
    val isSplashVisible by viewModel.isSplashVisible.collectAsState()
    val disclaimerAccepted by viewModel.disclaimerAccepted.collectAsState()
    val showDisclaimerModal by viewModel.showDisclaimerModal.collectAsState()
    val language by viewModel.language.collectAsState()
    val isCatalogOpen by viewModel.isCatalogOpen.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDeviceProfile by viewModel.selectedDeviceProfile.collectAsState()
    val selectedComponentDetail by viewModel.selectedComponentDetail.collectAsState()
    val showGeminiChatModal by viewModel.showGeminiChatModal.collectAsState()
    val showSkillPacksManager by viewModel.showSkillPacksManager.collectAsState()

    Crossfade(
        targetState = isSplashVisible,
        animationSpec = tween(durationMillis = 500),
        label = "splashCrossfade"
    ) { splash ->
        if (splash) {
            SplashScreen(
                language = language,
                onFinish = { viewModel.dismissSplash() }
            )
        } else if (showSkillPacksManager) {
            BackHandler {
                viewModel.closeSkillPacksManager()
            }
            OfflineSkillPacksScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeSkillPacksManager() }
            )
        } else if (activeDiagnosis != null) {
            BackHandler {
                viewModel.closeDiagnosis()
            }
            DiagnosisScreen(
                diagnosis = activeDiagnosis!!,
                viewModel = viewModel,
                onBack = { viewModel.closeDiagnosis() }
            )
        } else if (isCatalogOpen) {
            BackHandler {
                if (selectedComponentDetail != null || selectedDeviceProfile != null || selectedCategory != null) {
                    viewModel.navigateBackInCatalog()
                } else {
                    viewModel.closeCatalog()
                }
            }
            CategoriesScreen(
                viewModel = viewModel,
                onBack = {
                    if (selectedComponentDetail != null || selectedDeviceProfile != null || selectedCategory != null) {
                        viewModel.navigateBackInCatalog()
                    } else {
                        viewModel.closeCatalog()
                    }
                }
            )
        } else if (showWorkbench) {
            BackHandler {
                viewModel.closeWorkbench()
            }
            WorkbenchToolsScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeWorkbench() }
            )
        } else if (showGlossary) {
            BackHandler {
                viewModel.closeGlossary()
            }
            GlossaryScreen(
                language = language,
                onBack = { viewModel.closeGlossary() }
            )
        } else {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground),
                containerColor = DarkBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    AppBottomNavBar(
                        selectedTab = selectedTab,
                        language = language,
                        onTabSelected = { tab ->
                            viewModel.selectTab(tab)
                        }
                    )
                }
            ) { innerPadding ->
                val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding, bottom = innerPadding.calculateBottomPadding())
                ) {
                    when (selectedTab) {
                        0 -> StartScreen(
                            viewModel = viewModel,
                            onNavigateToCategories = { viewModel.openCatalogAll() }
                        )
                        1 -> WorkbenchToolsScreen(viewModel = viewModel)
                        2 -> ProjectsScreen(viewModel = viewModel)
                        3 -> HistoryScreen(viewModel = viewModel)
                        4 -> ProfileScreen(viewModel = viewModel)
                    }

                    // First-launch or requested Safety Disclaimer Dialog
                    if (!disclaimerAccepted || showDisclaimerModal) {
                        SafetyDisclaimerDialog(
                            language = language,
                            onAccept = { viewModel.acceptDisclaimer() },
                            onDismiss = if (disclaimerAccepted) {
                                { viewModel.closeDisclaimerModal() }
                            } else null
                        )
                    }

                    // Global Gemini KI-Chatbot with Google Search Grounding
                    if (showGeminiChatModal) {
                        GeminiChatbotModal(
                            viewModel = viewModel,
                            onDismiss = { viewModel.closeGeminiChat() }
                        )
                    }
                }
            }
        }
    }
}
