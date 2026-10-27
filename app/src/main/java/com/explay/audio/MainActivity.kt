package com.explay.audio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.explay.audio.ui.components.StatusHeaderCard
import com.explay.audio.ui.screens.EffectsScreen
import com.explay.audio.ui.screens.EqualizerScreen
import com.explay.audio.ui.screens.HalDiagnosticsScreen
import com.explay.audio.ui.screens.RoutingScreen
import com.explay.audio.ui.theme.DeepBackground
import com.explay.audio.ui.theme.EmeraldPrimary
import com.explay.audio.ui.theme.EmeraldPrimaryGlow
import com.explay.audio.ui.theme.ExPlayAudioTheme
import com.explay.audio.ui.theme.SurfaceDark
import com.explay.audio.ui.theme.TextSecondary
import com.explay.audio.ui.viewmodel.AudioViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    EQUALIZER("Equalizer", Icons.Default.Equalizer),
    EFFECTS("Efek & Bass", Icons.Default.SurroundSound),
    ROUTING("Perutean", Icons.Default.Router),
    DIAGNOSTICS("HAL Audio", Icons.Default.Analytics)
}

class MainActivity : ComponentActivity() {

    private val audioViewModel: AudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExPlayAudioTheme {
                RequestNotificationPermission()

                var currentTabIndex by remember { mutableIntStateOf(0) }
                val tabs = NavigationTab.values()

                // BackHandler bertingkat: kembali ke tab Equalizer sebelum keluar
                BackHandler(enabled = currentTabIndex != 0) {
                    currentTabIndex = 0
                }

                val isEngineEnabled by audioViewModel.isEngineEnabled.collectAsStateWithLifecycle()
                val currentProfile by audioViewModel.currentProfile.collectAsStateWithLifecycle()
                val customProfiles by audioViewModel.customProfiles.collectAsStateWithLifecycle()
                val currentDeviceType by audioViewModel.currentDeviceType.collectAsStateWithLifecycle()
                val forceSpeaker by audioViewModel.forceSpeaker.collectAsStateWithLifecycle()
                val forceBluetooth by audioViewModel.forceBluetooth.collectAsStateWithLifecycle()
                val halEffects by audioViewModel.halEffects.collectAsStateWithLifecycle()
                val diagnostics by audioViewModel.deviceDiagnostics.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepBackground),
                    containerColor = DeepBackground,
                    bottomBar = {
                        NavigationBar(
                            containerColor = SurfaceDark,
                            tonalElevation = 8.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                val selected = currentTabIndex == index
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTabIndex = index },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DeepBackground,
                                        selectedTextColor = EmeraldPrimaryGlow,
                                        indicatorColor = EmeraldPrimary,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        // Header Status Terpusat
                        StatusHeaderCard(
                            isEngineEnabled = isEngineEnabled,
                            onToggleEngine = { audioViewModel.toggleEngine(it) },
                            deviceType = currentDeviceType,
                            profileName = currentProfile.name,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // Konten Layar dengan Transisi Halus (Slide + Fade)
                        AnimatedContent(
                            targetState = currentTabIndex,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                            (slideOutHorizontally { width -> -width } + fadeOut())
                                } else {
                                    (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                            (slideOutHorizontally { width -> width } + fadeOut())
                                }
                            },
                            label = "tabTransition",
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) { tabIndex ->
                            when (tabIndex) {
                                0 -> EqualizerScreen(
                                    currentProfile = currentProfile,
                                    customProfiles = customProfiles,
                                    isEngineEnabled = isEngineEnabled,
                                    onBandGainChange = { idx, db -> audioViewModel.setBandGain(idx, db) },
                                    onPreGainChange = { db -> audioViewModel.setPreGain(db) },
                                    onSelectPreset = { preset -> audioViewModel.selectPreset(preset) }
                                )
                                1 -> EffectsScreen(
                                    currentProfile = currentProfile,
                                    isEngineEnabled = isEngineEnabled,
                                    onBassBoostChange = { audioViewModel.setBassBoost(it) },
                                    onVirtualizerChange = { audioViewModel.setVirtualizer(it) },
                                    onLoudnessChange = { audioViewModel.setLoudnessEnhancer(it) },
                                    onLimiterChange = { en, th -> audioViewModel.setLimiter(en, th) },
                                    onReverbPresetChange = { audioViewModel.setReverbPreset(it) },
                                    onReverbDecayChange = { audioViewModel.setReverbDecay(it) }
                                )
                                2 -> RoutingScreen(
                                    currentDeviceType = currentDeviceType,
                                    forceSpeaker = forceSpeaker,
                                    forceBluetooth = forceBluetooth,
                                    customProfiles = customProfiles,
                                    currentProfile = currentProfile,
                                    onForceSpeakerChange = { audioViewModel.setForceSpeaker(it) },
                                    onForceBluetoothChange = { audioViewModel.setForceBluetooth(it) },
                                    onSaveCustomPreset = { name -> audioViewModel.saveAsCustomPreset(name) },
                                    onDeleteCustomPreset = { id -> audioViewModel.deleteCustomPreset(id) },
                                    onSelectPreset = { preset -> audioViewModel.selectPreset(preset) }
                                )
                                3 -> HalDiagnosticsScreen(
                                    diagnostics = diagnostics,
                                    halEffects = halEffects,
                                    onRefresh = { audioViewModel.refreshDiagnostics() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val hasPermission = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* Disetujui atau ditolak */ }
                )
                LaunchedEffect(Unit) {
                    launcher.launch(permission)
                }
            }
        }
    }
}
