package com.example

import com.example.game.engine.GameEngine
import com.example.game.map.CanyonMap
import com.example.game.model.AbilityType
import com.example.game.model.GamePhase
import com.example.game.model.TargetPriority
import com.example.game.model.TowerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

  @Test
  fun `startNewGame initializes economy, phase, and 11 hex slots`() {
    val engine = GameEngine()
    engine.startNewGame()
    val state = engine.hudState.value
    assertEquals(GamePhase.PLAYING, state.phase)
    assertEquals(100, state.baseHp)
    assertEquals(320, state.gold)
    assertEquals(130, state.crystals)
    assertEquals(11, CanyonMap.buildSlots.size)
  }

  @Test
  fun `build, upgrade, cycle priority, and sell tower updates gold accurately`() {
    val engine = GameEngine()
    engine.startNewGame()

    // Select slot 0 and build Cannon (100g)
    val slot = CanyonMap.buildSlots[0]
    engine.onMapTap(slot.x, slot.y)
    engine.buildTower(slot.id, TowerType.CANNON)

    var tower = engine.towers[slot.id]
    assertNotNull(tower)
    assertEquals(220, engine.hudState.value.gold)
    assertEquals(1, tower!!.level)

    // Upgrade to Lv 2 (cost 75)
    engine.upgradeSelectedTower()
    tower = engine.towers[slot.id]!!
    assertEquals(2, tower.level)
    assertEquals(145, engine.hudState.value.gold)

    // Cycle target priority
    assertEquals(TargetPriority.FIRST, tower.targetPriority)
    engine.cycleTargetPriority()
    assertEquals(TargetPriority.STRONGEST, tower.targetPriority)

    // Sell tower -> 70% of 175 = 122.5 -> rounded to 120
    val refund = tower.sellRefund()
    engine.sellSelectedTower()
    assertNull(engine.towers[slot.id])
    assertEquals(145 + refund, engine.hudState.value.gold)
  }

  @Test
  fun `active abilities consume crystals and trigger wave simulation`() {
    val engine = GameEngine()
    engine.startNewGame()
    engine.triggerNextWaveEarly()

    // Step 2 seconds to spawn Scout Drones
    repeat(120) { engine.step(1f / 60f) }
    assertTrue(engine.enemyPool.any { it.active })

    val initialCrystals = engine.hudState.value.crystals
    engine.onAbilityButtonClick(AbilityType.CRYO_FREEZE)
    assertEquals(initialCrystals - AbilityType.CRYO_FREEZE.crystalCost, engine.hudState.value.crystals)
    assertTrue(engine.hudState.value.globalFreezeTimer > 0f)
  }
}
