package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameViewModel
import com.example.game.model.GamePhase
import com.example.game.render.BattlefieldCanvas
import com.example.ui.components.BottomAbilityBar
import com.example.ui.components.CodexOverlay
import com.example.ui.components.EndGameOverlay
import com.example.ui.components.MainMenuOverlay
import com.example.ui.components.PauseOverlay
import com.example.ui.components.TopGameHud
import com.example.ui.components.TowerBuildSheet
import com.example.ui.components.TowerUpgradeSheet
import com.example.ui.theme.NeonBastionTheme
import com.example.ui.theme.SpaceObsidian

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
    )
    setContent {
      NeonBastionTheme {
        NeonBastionGameScreen()
      }
    }
  }
}

@Composable
fun NeonBastionGameScreen(gameViewModel: GameViewModel = viewModel()) {
  val hudState by gameViewModel.hudState.collectAsStateWithLifecycle()

  // 60 FPS display-synced simulation loop
  LaunchedEffect(Unit) {
    var lastNanos = 0L
    while (true) {
      withFrameNanos { frameNanos ->
        if (lastNanos != 0L) {
          val dt = (frameNanos - lastNanos) / 1_000_000_000f
          gameViewModel.step(dt)
        }
        lastNanos = frameNanos
      }
    }
  }

  // Handle system back during active gameplay -> pause
  BackHandler(enabled = hudState.phase == GamePhase.PLAYING && hudState.selectedSlotId == null && !hudState.codexOpen) {
    gameViewModel.togglePause()
  }

  BoxWithConstraints(
    modifier = Modifier.fillMaxSize().background(SpaceObsidian),
    contentAlignment = Alignment.Center,
  ) {
    val isWideScreen = maxWidth > 600.dp
    val arenaModifier =
      if (isWideScreen) {
        Modifier.widthIn(max = 520.dp).fillMaxHeight()
      } else {
        Modifier.fillMaxSize()
      }

    Box(modifier = arenaModifier) {
      // 2.5D Sci-Fi Canyon Battlefield
      BattlefieldCanvas(
        engine = gameViewModel.engine,
        selectedSlotId = hudState.selectedSlotId,
        activeAbilityTargeting = hudState.activeAbilityTargeting,
        globalFreezeTimer = hudState.globalFreezeTimer,
        overdriveTimer = hudState.overdriveTimer,
        onTapMap = { nx, ny -> gameViewModel.onMapTap(nx, ny) },
        onViewportAspectChanged = { aspect -> gameViewModel.setViewportAspect(aspect) },
      )

      // Active HUD during play / pause
      if (hudState.phase != GamePhase.MENU) {
        TopGameHud(
          state = hudState,
          onPauseClick = { gameViewModel.togglePause() },
          onNextWaveClick = { gameViewModel.triggerNextWaveEarly() },
          onOpenCodexClick = { gameViewModel.setCodexOpen(true) },
          modifier = Modifier.align(Alignment.TopCenter),
        )

        // Bottom Ability & Speed Bar
        BottomAbilityBar(
          state = hudState,
          onAbilityClick = { ability -> gameViewModel.onAbilityClick(ability) },
          onSpeedToggle = { gameViewModel.toggleSpeed() },
          modifier = Modifier.align(Alignment.BottomCenter),
        )
      }

      // Build or Upgrade Drawer anchored smoothly above the bottom ability bar
      val selectedSlot = hudState.selectedSlotId
      val selectedTower = hudState.selectedTower
      AnimatedVisibility(
        visible = hudState.phase == GamePhase.PLAYING && selectedSlot != null,
        enter = slideInVertically { it / 2 } + fadeIn(),
        exit = slideOutVertically { it / 2 } + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 86.dp),
      ) {
        if (selectedSlot != null) {
          if (selectedTower == null) {
            TowerBuildSheet(
              slotId = selectedSlot,
              gold = hudState.gold,
              onSelectTower = { type -> gameViewModel.buildTower(selectedSlot, type) },
              onDismiss = { gameViewModel.clearSelection() },
            )
          } else {
            TowerUpgradeSheet(
              tower = selectedTower,
              gold = hudState.gold,
              onUpgrade = { gameViewModel.upgradeSelectedTower() },
              onCyclePriority = { gameViewModel.cycleTargetPriority() },
              onSell = { gameViewModel.sellSelectedTower() },
              onDismiss = { gameViewModel.clearSelection() },
            )
          }
        }
      }

      // Menu, Pause, End-Game & Codex Overlays
      when (hudState.phase) {
        GamePhase.MENU -> {
          MainMenuOverlay(
            onStartGame = { gameViewModel.startNewGame() },
            onOpenCodex = { gameViewModel.setCodexOpen(true) },
          )
        }
        GamePhase.PAUSED -> {
          if (!hudState.codexOpen) {
            PauseOverlay(
              onResume = { gameViewModel.togglePause() },
              onRestart = { gameViewModel.startNewGame() },
              onOpenCodex = { gameViewModel.setCodexOpen(true) },
            )
          }
        }
        GamePhase.VICTORY -> {
          EndGameOverlay(
            victory = true,
            state = hudState,
            onRestart = { gameViewModel.startNewGame() },
          )
        }
        GamePhase.DEFEAT -> {
          EndGameOverlay(
            victory = false,
            state = hudState,
            onRestart = { gameViewModel.startNewGame() },
          )
        }
        GamePhase.PLAYING -> Unit
      }

      if (hudState.codexOpen) {
        CodexOverlay(onClose = { gameViewModel.setCodexOpen(false) })
      }
    }
  }
}
