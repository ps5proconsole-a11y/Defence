package com.example.game

import androidx.lifecycle.ViewModel
import com.example.game.engine.GameEngine
import com.example.game.model.AbilityType
import com.example.game.model.HudUiState
import com.example.game.model.TowerType
import kotlinx.coroutines.flow.StateFlow

class GameViewModel : ViewModel() {
  val engine = GameEngine()
  val hudState: StateFlow<HudUiState> = engine.hudState

  fun startNewGame() = engine.startNewGame()

  fun step(dtSeconds: Float) = engine.step(dtSeconds)

  fun setViewportAspect(aspect: Float) {
    if (aspect > 0.1f) {
      engine.viewportAspect = aspect
    }
  }

  fun onMapTap(normX: Float, normY: Float) = engine.onMapTap(normX, normY)

  fun clearSelection() = engine.clearSelection()

  fun buildTower(slotId: Int, type: TowerType) = engine.buildTower(slotId, type)

  fun upgradeSelectedTower() = engine.upgradeSelectedTower()

  fun cycleTargetPriority() = engine.cycleTargetPriority()

  fun sellSelectedTower() = engine.sellSelectedTower()

  fun togglePause() = engine.togglePause()

  fun setCodexOpen(open: Boolean) = engine.setCodexOpen(open)

  fun toggleSpeed() = engine.toggleSpeed()

  fun triggerNextWaveEarly() = engine.triggerNextWaveEarly()

  fun onAbilityClick(ability: AbilityType) = engine.onAbilityButtonClick(ability)
}
