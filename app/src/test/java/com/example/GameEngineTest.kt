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

  @Test
  fun `missile tower launches curved spiraling missiles with trail and tracks targets`() {
    val engine = GameEngine()
    engine.startNewGame()

    val slot0 = CanyonMap.buildSlots[0]
    engine.buildTower(slot0.id, TowerType.MISSILE)
    val missileTower = engine.towers[slot0.id]!!

    // Separate engine instance so both Missile (150g) and Freeze (200g) fit within 320g starting gold
    val freezeEngine = GameEngine()
    freezeEngine.startNewGame()
    val slot1 = CanyonMap.buildSlots[0]
    freezeEngine.buildTower(slot1.id, TowerType.FREEZE)
    val freezeTower = freezeEngine.towers[slot1.id]!!

    engine.triggerNextWaveEarly()
    freezeEngine.triggerNextWaveEarly()

    var sawCurvedTrail = false
    var sawMissileLock = false
    var sawFreezeLock = false
    var maxDeviationFromStraightLine = 0f

    repeat(540) {
      engine.step(1f / 60f)
      freezeEngine.step(1f / 60f)
      if (missileTower.targetLocked) sawMissileLock = true
      if (freezeTower.targetLocked) sawFreezeLock = true

      for (p in engine.projectilePool) {
        if (p.active && p.kind == com.example.game.model.ProjectileKind.MISSILE && p.trailCount >= 5) {
          sawCurvedTrail = true
          // Measure perpendicular distance of missile from straight line between start and target
          val lx = p.targetX - p.startX
          val ly = p.targetY - p.startY
          val lineLen = kotlin.math.hypot(lx, ly)
          if (lineLen > 0.02f) {
            val cross = kotlin.math.abs((p.x - p.startX) * ly - (p.y - p.startY) * lx) / lineLen
            if (cross > maxDeviationFromStraightLine) maxDeviationFromStraightLine = cross
          }
        }
      }
    }

    assertTrue("Missile turret should track and lock onto enemies", sawMissileLock)
    assertTrue("Freeze tower should also track enemies in aura", sawFreezeLock)
    assertTrue("Missile should record curved contrail history", sawCurvedTrail)
    assertTrue("Missile trajectory should curve noticeably off a straight line", maxDeviationFromStraightLine > 0.008f)
    assertTrue("Missiles should destroy or damage enemies", engine.hudState.value.totalKills > 0)
  }
}
