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

  @Test
  fun `laser turret smoothly slews, locks target, ramps up, and emits active beam`() {
    val engine = GameEngine()
    engine.startNewGame()

    // Build Laser Tower on slot 0 (overlooking top serpentine leg)
    val slot0 = CanyonMap.buildSlots[0]
    engine.buildTower(slot0.id, TowerType.LASER)
    val laser = engine.towers[slot0.id]!!
    val initialAngle = laser.angleRad

    engine.triggerNextWaveEarly()

    var sawIdleSweep = false
    var sawActiveBeam = false
    var maxRamp = 0f

    // Simulate 8 seconds at 60 FPS as Scout Drones move into range of slot 0
    repeat(480) {
      engine.step(1f / 60f)
      if (!laser.targetLocked && kotlin.math.abs(laser.angleRad - initialAngle) > 0.02f) {
        sawIdleSweep = true
      }
      if (engine.laserBeams.any { it.active }) {
        sawActiveBeam = true
        val beam = engine.laserBeams.first { it.active }
        assertTrue(beam.intensity > 0.7f)
      }
      if (laser.laserRamp > maxRamp) maxRamp = laser.laserRamp
    }

    assertTrue("Laser turret should sweep smoothly while scanning", sawIdleSweep)
    assertTrue("Laser turret should align, lock target, and emit beam", sawActiveBeam)
    assertTrue("Laser beam intensity should ramp up during continuous lock", maxRamp > 0.2f)
  }
}
