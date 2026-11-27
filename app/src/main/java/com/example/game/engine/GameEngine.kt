package com.example.game.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.game.map.CanyonMap
import com.example.game.model.AbilityType
import com.example.game.model.EnemyEntity
import com.example.game.model.EnemyType
import com.example.game.model.FloatingTextEntity
import com.example.game.model.GamePhase
import com.example.game.model.LaserBeamEntity
import com.example.game.model.LightningArcEntity
import com.example.game.model.ParticleEntity
import com.example.game.model.ProjectileEntity
import com.example.game.model.ProjectileKind
import com.example.game.model.ShockwaveEntity
import com.example.game.model.TargetPriority
import com.example.game.model.TowerInstance
import com.example.game.model.TowerSnapshot
import com.example.game.model.TowerType
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.CrystalViolet
import com.example.ui.theme.EnemyCrimson
import com.example.ui.theme.EnemyEmber
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.HudEmerald
import com.example.ui.theme.IceBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ShieldDomeBlue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class GameEngine {
  // Viewport ratio H / W for accurate circular distance in normalized coordinates
  @Volatile var viewportAspect: Float = 1.85f

  // Frame counter observed by Canvas for zero-allocation 60 FPS redraws
  var renderTick by mutableLongStateOf(0L)
    private set

  var totalTime: Float = 0f
    private set

  private val _hudState = MutableStateFlow(HudUiStateCompat.initial())
  val hudState: StateFlow<com.example.game.model.HudUiState> = _hudState.asStateFlow()

  // Object pools
  val towers = Array<TowerInstance?>(CanyonMap.buildSlots.size) { null }
  val enemyPool = Array(140) { EnemyEntity() }
  val projectilePool = Array(160) { ProjectileEntity() }
  val particlePool = Array(320) { ParticleEntity() }
  val shockwavePool = Array(48) { ShockwaveEntity() }
  val lightningPool = Array(48) { LightningArcEntity() }
  val laserBeams = Array(16) { LaserBeamEntity() }
  val floatingTextPool = Array(48) { FloatingTextEntity() }

  // Simulation scalars
  private var nextEnemyId = 1
  private var currentWaveIndex = -1
  private var currentGroupIndex = 0
  private var remainingInGroup = 0
  private var spawnTimer = 0f
  private var betweenWaveTimer = 0f
  private var waveSpawningActive = false
  private var statusBannerTimer = 0f
  private var missileLaunchParity = 0

  private val abilityCooldowns = mutableMapOf<AbilityType, Float>().apply {
    AbilityType.entries.forEach { put(it, 0f) }
  }
  private var globalFreezeTimer = 0f
  private var overdriveTimer = 0f
  private var pendingBarrageStrikes = mutableListOf<PendingStrike>()

  data class PendingStrike(var x: Float, var y: Float, var delay: Float)

  private var baseHp = 100
  private val maxBaseHp = 100
  private var gold = 320
  private var crystals = 130
  private var totalKills = 0
  private var speedMultiplier = 1
  private var phase = GamePhase.MENU

  private val rng = Random(42)

  fun startNewGame() {
    for (i in towers.indices) towers[i] = null
    enemyPool.forEach { it.active = false }
    projectilePool.forEach { it.active = false }
    particlePool.forEach { it.active = false }
    shockwavePool.forEach { it.active = false }
    lightningPool.forEach { it.active = false }
    laserBeams.forEach { it.active = false }
    floatingTextPool.forEach { it.active = false }
    pendingBarrageStrikes.clear()

    nextEnemyId = 1
    currentWaveIndex = -1
    currentGroupIndex = 0
    remainingInGroup = 0
    spawnTimer = 0f
    betweenWaveTimer = 4.5f
    waveSpawningActive = false
    globalFreezeTimer = 0f
    overdriveTimer = 0f
    statusBannerTimer = 2.5f
    AbilityType.entries.forEach { abilityCooldowns[it] = 0f }

    baseHp = maxBaseHp
    gold = 320
    crystals = 130
    totalKills = 0
    speedMultiplier = 1
    phase = GamePhase.PLAYING

    _hudState.value =
      com.example.game.model.HudUiState(
        phase = GamePhase.PLAYING,
        baseHp = baseHp,
        maxBaseHp = maxBaseHp,
        gold = gold,
        crystals = crystals,
        wave = 0,
        totalWaves = WaveDefinitions.waves.size,
        waveInProgress = false,
        betweenWaveCountdown = betweenWaveTimer,
        speedMultiplier = 1,
        abilityCooldowns = abilityCooldowns.toMap(),
        statusBanner = "Разместите башни на синих кругах",
      )
    renderTick++
  }

  fun togglePause() {
    if (phase == GamePhase.PLAYING) {
      phase = GamePhase.PAUSED
      syncHud()
    } else if (phase == GamePhase.PAUSED) {
      phase = GamePhase.PLAYING
      _hudState.update { it.copy(codexOpen = false) }
      syncHud()
    }
  }

  fun setCodexOpen(open: Boolean) {
    if (open && phase == GamePhase.PLAYING) {
      phase = GamePhase.PAUSED
    }
    _hudState.update { it.copy(codexOpen = open, phase = phase) }
  }

  fun toggleSpeed() {
    speedMultiplier = if (speedMultiplier == 1) 2 else 1
    syncHud()
  }

  fun triggerNextWaveEarly() {
    if (phase != GamePhase.PLAYING) return
    if (waveSpawningActive || hasActiveEnemies()) return
    if (currentWaveIndex + 1 >= WaveDefinitions.waves.size) return

    val bonusGold = (betweenWaveTimer.toInt() * 3).coerceAtLeast(10)
    gold += bonusGold
    spawnFloatingText(CanyonMap.baseGatePosition.x, CanyonMap.baseGatePosition.y - 0.04f, "+${bonusGold}g", GoldAmber)
    startNextWave()
  }

  private fun startNextWave() {
    val nextIdx = currentWaveIndex + 1
    if (nextIdx >= WaveDefinitions.waves.size) return
    currentWaveIndex = nextIdx
    val spec = WaveDefinitions.waves[nextIdx]
    currentGroupIndex = 0
    remainingInGroup = spec.groups.firstOrNull()?.count ?: 0
    spawnTimer = 0.2f
    betweenWaveTimer = 0f
    waveSpawningActive = true
    showBanner("Волна ${spec.waveNumber}/${WaveDefinitions.waves.size}: ${spec.title}")
    syncHud()
  }

  fun onMapTap(normX: Float, normY: Float) {
    if (phase != GamePhase.PLAYING) return

    val targetingAbility = _hudState.value.activeAbilityTargeting
    if (targetingAbility != null) {
      executeTargetedAbility(targetingAbility, normX, normY)
      return
    }

    // Check if user tapped one of the 11 hex slots
    var closestSlotId: Int? = null
    var closestDist = Float.MAX_VALUE
    for (slot in CanyonMap.buildSlots) {
      val d = distW(normX, normY, slot.x, slot.y)
      if (d < 0.088f && d < closestDist) {
        closestDist = d
        closestSlotId = slot.id
      }
    }

    if (closestSlotId != null) {
      val existing = towers[closestSlotId]
      _hudState.update {
        it.copy(
          selectedSlotId = closestSlotId,
          selectedTower = existing?.toSnapshot(),
        )
      }
    } else {
      clearSelection()
    }
  }

  fun clearSelection() {
    _hudState.update {
      it.copy(selectedSlotId = null, selectedTower = null, activeAbilityTargeting = null)
    }
  }

  fun buildTower(slotId: Int, type: TowerType) {
    if (phase != GamePhase.PLAYING) return
    if (slotId !in towers.indices) return
    if (towers[slotId] != null) return
    if (gold < type.baseCost) {
      showBanner("Недостаточно золота (${type.baseCost})")
      return
    }
    val slot = CanyonMap.buildSlots[slotId]
    gold -= type.baseCost
    val initialAngle = atan2((slot.roadAnchorY - slot.y) * viewportAspect, slot.roadAnchorX - slot.x)
    val tower =
      TowerInstance(
        slotId = slot.id,
        x = slot.x,
        y = slot.y,
        roadAnchorX = slot.roadAnchorX,
        roadAnchorY = slot.roadAnchorY,
        type = type,
        angleRad = initialAngle,
      )
    towers[slotId] = tower
    spawnShockwave(slot.x, slot.y, 0.09f, type.accentColor, 0.35f)
    spawnBurstParticles(slot.x, slot.y, type.accentColor, 14, 0.14f)
    _hudState.update {
      it.copy(
        selectedSlotId = slotId,
        selectedTower = tower.toSnapshot(),
        gold = gold,
        towersBuiltCount = towers.count { t -> t != null },
      )
    }
  }

  fun upgradeSelectedTower() {
    val slotId = _hudState.value.selectedSlotId ?: return
    val tower = towers.getOrNull(slotId) ?: return
    if (tower.level >= 3) return
    val cost = tower.type.upgradeCost(tower.level)
    if (gold < cost) {
      showBanner("Нужно $cost золота для улучшения")
      return
    }
    gold -= cost
    tower.totalInvested += cost
    tower.level += 1
    tower.shieldMaxEnergy = 100f + (tower.level - 1) * 45f
    tower.shieldEnergy = tower.shieldMaxEnergy
    spawnShockwave(tower.x, tower.y, 0.11f, NeonCyan, 0.4f)
    spawnBurstParticles(tower.x, tower.y, GoldAmber, 16, 0.16f)
    spawnFloatingText(tower.x, tower.y - 0.03f, "LVL ${tower.level}", NeonCyan)
    syncHud()
  }

  fun cycleTargetPriority() {
    val slotId = _hudState.value.selectedSlotId ?: return
    val tower = towers.getOrNull(slotId) ?: return
    val entries = TargetPriority.entries
    tower.targetPriority = entries[(tower.targetPriority.ordinal + 1) % entries.size]
    syncHud()
  }

  fun sellSelectedTower() {
    val slotId = _hudState.value.selectedSlotId ?: return
    val tower = towers.getOrNull(slotId) ?: return
    val refund = tower.sellRefund()
    gold += refund
    towers[slotId] = null
    spawnFloatingText(tower.x, tower.y - 0.02f, "+${refund}g", GoldAmber)
    spawnBurstParticles(tower.x, tower.y, tower.type.accentColor, 10, 0.10f)
    _hudState.update {
      it.copy(
        selectedSlotId = null,
        selectedTower = null,
        gold = gold,
        towersBuiltCount = towers.count { t -> t != null },
      )
    }
  }

  fun onAbilityButtonClick(ability: AbilityType) {
    if (phase != GamePhase.PLAYING) return
    val cd = abilityCooldowns[ability] ?: 0f
    if (cd > 0.05f) {
      showBanner("${ability.shortName}: перезарядка ${cd.toInt() + 1}с")
      return
    }
    if (crystals < ability.crystalCost) {
      showBanner("Нужно ${ability.crystalCost} кристаллов")
      return
    }
    if (ability.isTargeted) {
      val current = _hudState.value.activeAbilityTargeting
      _hudState.update {
        it.copy(
          activeAbilityTargeting = if (current == ability) null else ability,
          selectedSlotId = null,
          selectedTower = null,
        )
      }
      if (current != ability) {
        showBanner("Нажмите на карту: ${ability.title}")
      }
    } else {
      executeGlobalAbility(ability)
    }
  }

  private fun executeTargetedAbility(ability: AbilityType, x: Float, y: Float) {
    val cd = abilityCooldowns[ability] ?: 0f
    if (cd > 0.05f || crystals < ability.crystalCost) {
      _hudState.update { it.copy(activeAbilityTargeting = null) }
      return
    }
    crystals -= ability.crystalCost
    abilityCooldowns[ability] = ability.cooldownSeconds
    _hudState.update { it.copy(activeAbilityTargeting = null) }

    when (ability) {
      AbilityType.LIGHTNING_STRIKE -> {
        spawnLightningArc(x, 0.02f, x, y, NeonCyan, thickness = 7f, life = 0.35f)
        spawnLightningArc(x - 0.02f, 0.03f, x, y, Color.White, thickness = 3.5f, life = 0.28f)
        spawnShockwave(x, y, 0.16f, NeonCyan, 0.45f)
        spawnBurstParticles(x, y, NeonCyan, 26, 0.24f)
        applyAreaDamage(x, y, radiusW = 0.16f, damage = 320f, stunSeconds = 1.6f, empSeconds = 3f)
        spawnFloatingText(x, y - 0.03f, "РАЗРЯД!", NeonCyan)
      }
      AbilityType.MISSILE_BARRAGE -> {
        pendingBarrageStrikes.clear()
        for (i in 0 until 7) {
          val ang = rng.nextFloat() * 2f * PI.toFloat()
          val rad = rng.nextFloat() * 0.11f
          val sx = (x + cos(ang) * rad).coerceIn(0.06f, 0.94f)
          val sy = (y + sin(ang) * rad / viewportAspect).coerceIn(0.12f, 0.88f)
          pendingBarrageStrikes.add(PendingStrike(sx, sy, delay = i * 0.11f))
        }
        showBanner("Ракетный залп!")
      }
      else -> Unit
    }
    syncHud()
  }

  private fun executeGlobalAbility(ability: AbilityType) {
    crystals -= ability.crystalCost
    abilityCooldowns[ability] = ability.cooldownSeconds
    when (ability) {
      AbilityType.CRYO_FREEZE -> {
        globalFreezeTimer = 5.2f
        spawnShockwave(0.5f, 0.5f, 0.65f, IceBlue, 0.65f)
        for (e in enemyPool) {
          if (!e.active) continue
          e.slowTimer = maxOf(e.slowTimer, 5.2f)
          e.slowStrength = maxOf(e.slowStrength, 0.72f)
          spawnBurstParticles(e.x, e.y, IceBlue, 6, 0.08f)
        }
        showBanner("Крио-волна: враги заморожены!")
      }
      AbilityType.ORBITAL_OVERDRIVE -> {
        overdriveTimer = 6.0f
        // Find strongest active enemy and blast it from orbit
        var strongest: EnemyEntity? = null
        var bestHp = -1f
        for (e in enemyPool) {
          if (e.active && e.hp + e.shield > bestHp) {
            bestHp = e.hp + e.shield
            strongest = e
          }
        }
        if (strongest != null) {
          spawnLightningArc(strongest.x, 0.0f, strongest.x, strongest.y, GoldAmber, 9f, 0.45f)
          spawnLightningArc(strongest.x, 0.0f, strongest.x, strongest.y, Color.White, 4f, 0.40f)
          spawnShockwave(strongest.x, strongest.y, 0.17f, GoldAmber, 0.5f)
          spawnBurstParticles(strongest.x, strongest.y, GoldAmber, 28, 0.25f)
          damageEnemy(strongest, 650f, slotId = -1, ignoreArmor = true)
        }
        for (t in towers) {
          if (t != null) {
            spawnShockwave(t.x, t.y, 0.08f, GoldAmber, 0.35f)
          }
        }
        showBanner("Орбитальный удар + Ускорение башен!")
      }
      else -> Unit
    }
    syncHud()
  }

  fun step(rawDtSeconds: Float) {
    val dtUnscaled = rawDtSeconds.coerceIn(0f, 0.05f)
    if (phase != GamePhase.PLAYING) {
      // Gently animate idle crystal/portal shimmer even in menu/pause? Keep pause frozen, menu animated
      if (phase == GamePhase.MENU) {
        totalTime += dtUnscaled
        renderTick++
      }
      return
    }

    val dt = dtUnscaled * speedMultiplier
    totalTime += dt

    var hudNeedsSync = false

    // Cooldowns & global timers
    var cooldownChanged = false
    for (ab in AbilityType.entries) {
      val cur = abilityCooldowns[ab] ?: 0f
      if (cur > 0f) {
        abilityCooldowns[ab] = (cur - dt).coerceAtLeast(0f)
        cooldownChanged = true
      }
    }
    if (globalFreezeTimer > 0f) {
      globalFreezeTimer = (globalFreezeTimer - dt).coerceAtLeast(0f)
      cooldownChanged = true
    }
    if (overdriveTimer > 0f) {
      overdriveTimer = (overdriveTimer - dt).coerceAtLeast(0f)
      cooldownChanged = true
    }
    if (statusBannerTimer > 0f) {
      statusBannerTimer = (statusBannerTimer - dtUnscaled).coerceAtLeast(0f)
      if (statusBannerTimer == 0f) {
        _hudState.update { it.copy(statusBanner = null) }
      }
    }

    // Pending missile barrage warheads
    if (pendingBarrageStrikes.isNotEmpty()) {
      val iter = pendingBarrageStrikes.iterator()
      while (iter.hasNext()) {
        val strike = iter.next()
        strike.delay -= dt
        if (strike.delay <= 0f) {
          spawnShockwave(strike.x, strike.y, 0.13f, EnemyEmber, 0.38f)
          spawnBurstParticles(strike.x, strike.y, EnemyEmber, 18, 0.22f)
          spawnBurstParticles(strike.x, strike.y, GoldAmber, 8, 0.14f)
          applyAreaDamage(strike.x, strike.y, radiusW = 0.13f, damage = 195f, stunSeconds = 0.4f, empSeconds = 0f)
          iter.remove()
        }
      }
    }

    // Wave spawning logic
    if (waveSpawningActive) {
      updateWaveSpawning(dt)
    } else if (!hasActiveEnemies()) {
      if (currentWaveIndex >= WaveDefinitions.waves.lastIndex) {
        phase = GamePhase.VICTORY
        syncHud()
        renderTick++
        return
      } else {
        val prevSec = betweenWaveTimer.toInt()
        betweenWaveTimer -= dt
        if (betweenWaveTimer.toInt() != prevSec) hudNeedsSync = true
        if (betweenWaveTimer <= 0f) {
          startNextWave()
        }
      }
    }

    // Update enemies, towers & projectiles, then sync HUD if kills/gold/HP changed
    val killsBefore = totalKills
    val goldBefore = gold
    val hpBefore = baseHp
    updateEnemies(dt)

    if (baseHp <= 0) {
      baseHp = 0
      phase = GamePhase.DEFEAT
      syncHud()
      renderTick++
      return
    }

    // Update towers & clear laser beams before tower pass
    for (lb in laserBeams) lb.active = false
    updateTowers(dt)

    // Update projectiles, particles, shockwaves, lightning, floating texts
    updateProjectiles(dt)
    updateEffects(dt)

    if (totalKills != killsBefore || gold != goldBefore || baseHp != hpBefore) {
      hudNeedsSync = true
    }
    if (hudNeedsSync || cooldownChanged) {
      syncHud()
    }
    renderTick++
  }

  private fun updateWaveSpawning(dt: Float) {
    val waveSpec = WaveDefinitions.waves.getOrNull(currentWaveIndex) ?: return
    spawnTimer -= dt
    if (spawnTimer > 0f) return

    if (currentGroupIndex >= waveSpec.groups.size) {
      waveSpawningActive = false
      return
    }

    val group = waveSpec.groups[currentGroupIndex]
    if (remainingInGroup > 0) {
      spawnEnemy(group.enemyType, group.hpMultiplier)
      remainingInGroup--
      spawnTimer = group.intervalSeconds
    }

    if (remainingInGroup <= 0) {
      currentGroupIndex++
      if (currentGroupIndex < waveSpec.groups.size) {
        remainingInGroup = waveSpec.groups[currentGroupIndex].count
        spawnTimer = 1.2f
      } else {
        waveSpawningActive = false
      }
    }
  }

  private fun spawnEnemy(type: EnemyType, hpMult: Float, startDist: Float = 0f) {
    val slot = enemyPool.firstOrNull { !it.active } ?: return
    slot.active = true
    slot.id = nextEnemyId++
    slot.type = type
    slot.distanceAlongPath = startDist
    slot.totalPathLength =
      if (type.isFlying) {
        hypot(
          CanyonMap.baseGatePosition.x - CanyonMap.portalPosition.x,
          CanyonMap.baseGatePosition.y - CanyonMap.portalPosition.y,
        )
      } else {
        CanyonMap.totalPathLength
      }

    val (pos, ang) =
      if (type.isFlying) {
        val t = (startDist / slot.totalPathLength).coerceIn(0f, 1f)
        val px = CanyonMap.portalPosition.x + (CanyonMap.baseGatePosition.x - CanyonMap.portalPosition.x) * t
        val py = CanyonMap.portalPosition.y + (CanyonMap.baseGatePosition.y - CanyonMap.portalPosition.y) * t
        androidx.compose.ui.geometry.Offset(px, py) to
          atan2(
            CanyonMap.baseGatePosition.y - CanyonMap.portalPosition.y,
            CanyonMap.baseGatePosition.x - CanyonMap.portalPosition.x,
          )
      } else {
        CanyonMap.samplePathPosition(startDist)
      }

    val visualAngle = atan2(sin(ang) * viewportAspect, cos(ang))
    slot.x = pos.x
    slot.y = pos.y
    slot.prevX = pos.x
    slot.prevY = pos.y
    slot.angleRad = visualAngle
    slot.targetAngleRad = visualAngle
    slot.laneOffset = if (type.isFlying) 0f else ((slot.id % 5) - 2) * 0.0055f
    slot.currentSpeed = type.speed * 0.55f
    slot.maxHp = type.baseHp * hpMult
    slot.hp = slot.maxHp
    slot.maxShield = type.baseShield * hpMult
    slot.shield = slot.maxShield
    slot.slowTimer = 0f
    slot.slowStrength = 0f
    slot.empTimer = 0f
    slot.stunTimer = 0f
    slot.shieldBlockTimer = 0f
    slot.hitFlashTimer = 0f
    slot.animPhase = rng.nextFloat() * 6.28f
    slot.bossAbilityTimer = 4.5f
    slot.dashTimer = 0f
  }

  private fun updateEnemies(dt: Float) {
    // Check Shield Tower roadblock capacity per tick
    val shieldBlocks = HashMap<Int, Int>()
    for (t in towers) {
      if (t != null && t.type == TowerType.SHIELD) {
        if (t.shieldOverloadTimer > 0f) {
          t.shieldOverloadTimer = (t.shieldOverloadTimer - dt).coerceAtLeast(0f)
        } else {
          t.shieldEnergy = (t.shieldEnergy + dt * 10f).coerceAtMost(t.shieldMaxEnergy)
        }
        shieldBlocks[t.slotId] = 0
      }
    }

    for (e in enemyPool) {
      if (!e.active) continue
      e.animPhase += dt * 10f
      if (e.hitFlashTimer > 0f) e.hitFlashTimer = (e.hitFlashTimer - dt).coerceAtLeast(0f)
      if (e.slowTimer > 0f) {
        e.slowTimer = (e.slowTimer - dt).coerceAtLeast(0f)
        if (e.slowTimer == 0f) e.slowStrength = 0f
      }
      if (e.empTimer > 0f) e.empTimer = (e.empTimer - dt).coerceAtLeast(0f)
      if (e.stunTimer > 0f) e.stunTimer = (e.stunTimer - dt).coerceAtLeast(0f)
      if (e.dashTimer > 0f) e.dashTimer = (e.dashTimer - dt).coerceAtLeast(0f)

      // Boss special abilities (disabled if EMP'd)
      if (e.type == EnemyType.ELITE_BOSS && e.empTimer <= 0f && e.stunTimer <= 0f) {
        e.bossAbilityTimer -= dt
        if (e.bossAbilityTimer <= 0f) {
          e.bossAbilityTimer = 6.0f
          val roll = rng.nextInt(3)
          when (roll) {
            0 -> {
              // Shield regeneration burst
              e.shield = (e.shield + e.maxShield * 0.35f).coerceAtMost(e.maxShield)
              spawnShockwave(e.x, e.y, 0.08f, EnemyCrimson, 0.4f)
              spawnFloatingText(e.x, e.y - 0.03f, "ЩИТ!", EnemyCrimson)
            }
            1 -> {
              // Speed dash
              e.dashTimer = 1.6f
              spawnBurstParticles(e.x, e.y, EnemyEmber, 10, 0.14f)
              spawnFloatingText(e.x, e.y - 0.03f, "РЫВОК!", EnemyEmber)
            }
            2 -> {
              // Summon 2 scout drones ahead/around
              spawnEnemy(EnemyType.SCOUT_DRONE, 1.25f, (e.distanceAlongPath + 0.04f).coerceAtMost(e.totalPathLength - 0.05f))
              spawnEnemy(EnemyType.SCOUT_DRONE, 1.25f, (e.distanceAlongPath - 0.03f).coerceAtLeast(0f))
              spawnFloatingText(e.x, e.y - 0.03f, "ДРОНЫ!", GoldAmber)
            }
          }
        }
      }

      // Check ground blocking by active Shield Towers
      var blockedByShield = false
      if (!e.type.isFlying && e.stunTimer <= 0f) {
        for (t in towers) {
          if (t == null || t.type != TowerType.SHIELD || t.shieldOverloadTimer > 0f) continue
          val distToDome = distW(e.x, e.y, t.roadAnchorX, t.roadAnchorY)
          if (distToDome < t.type.splashRadius) {
            val held = shieldBlocks[t.slotId] ?: 0
            if (held < t.effectiveShieldCapacity() && t.shieldEnergy > 1f) {
              shieldBlocks[t.slotId] = held + 1
              t.shieldEnergy -= dt * (if (e.type == EnemyType.ELITE_BOSS) 30f else 13f)
              if (t.shieldEnergy <= 0f) {
                t.shieldEnergy = 0f
                t.shieldOverloadTimer = 4.0f
                spawnShockwave(t.roadAnchorX, t.roadAnchorY, 0.09f, ShieldDomeBlue, 0.35f)
              }
              blockedByShield = true
              break
            }
          }
        }
      }

      val speedMult =
        when {
          e.stunTimer > 0f -> 0f
          blockedByShield -> 0.12f
          e.dashTimer > 0f -> 1.85f
          else -> (1f - e.slowStrength).coerceAtLeast(0.18f)
        }

      val desiredSpeed = e.type.speed * speedMult
      val inertiaRate = if (desiredSpeed < e.currentSpeed) 10.5f else 6.5f
      e.currentSpeed += (desiredSpeed - e.currentSpeed) * (inertiaRate * dt).coerceIn(0f, 1f)

      e.prevX = e.x
      e.prevY = e.y
      e.distanceAlongPath += e.currentSpeed * dt

      if (e.distanceAlongPath >= e.totalPathLength) {
        // Reached base gate
        e.active = false
        baseHp = (baseHp - e.type.baseDamage).coerceAtLeast(0)
        spawnShockwave(CanyonMap.baseGatePosition.x, CanyonMap.baseGatePosition.y, 0.11f, EnemyCrimson, 0.4f)
        spawnFloatingText(CanyonMap.baseGatePosition.x, CanyonMap.baseGatePosition.y - 0.03f, "-${e.type.baseDamage} HP", EnemyCrimson)
        continue
      }

      if (e.type.isFlying) {
        val t = (e.distanceAlongPath / e.totalPathLength).coerceIn(0f, 1f)
        val swayEnv = sin(t * PI.toFloat()).coerceIn(0f, 1f)
        val sway = sin(e.animPhase * 0.45f) * 0.025f * swayEnv
        val baseX = CanyonMap.portalPosition.x + (CanyonMap.baseGatePosition.x - CanyonMap.portalPosition.x) * t
        val baseY = CanyonMap.portalPosition.y + (CanyonMap.baseGatePosition.y - CanyonMap.portalPosition.y) * t
        e.x = baseX + sway
        e.y = baseY
        val dx = e.x - e.prevX
        val dyW = (e.y - e.prevY) * viewportAspect
        if (hypot(dx, dyW) > 0.0001f) {
          e.targetAngleRad = atan2(dyW, dx)
        }
      } else {
        val (pos, pathAng) = CanyonMap.samplePathPosition(e.distanceAlongPath)
        val progress = e.progress()
        val laneEnvelope =
          when {
            progress < 0.06f -> progress / 0.06f
            progress > 0.90f -> ((1f - progress) / 0.10f).coerceIn(0f, 1f)
            else -> 1f
          }
        val perpX = -sin(pathAng) * e.laneOffset * laneEnvelope
        val perpY = cos(pathAng) * e.laneOffset * laneEnvelope
        e.x = pos.x + perpX
        e.y = pos.y + perpY / viewportAspect
        e.targetAngleRad = atan2(sin(pathAng) * viewportAspect, cos(pathAng))
      }

      val headingErr = normalizeAngle(e.targetAngleRad - e.angleRad)
      e.angleRad = normalizeAngle(e.angleRad + headingErr * (11f * dt).coerceIn(0f, 1f))
    }

    // Check if wave just cleared (all spawns done + zero active enemies)
    if (!waveSpawningActive && currentWaveIndex >= 0 && !hasActiveEnemies()) {
      val waveSpec = WaveDefinitions.waves[currentWaveIndex]
      if (betweenWaveTimer == 0f && phase == GamePhase.PLAYING) {
        gold += waveSpec.clearGoldBonus
        crystals += waveSpec.clearCrystalBonus
        spawnFloatingText(0.5f, 0.48f, "+${waveSpec.clearGoldBonus}g  +${waveSpec.clearCrystalBonus}💎", HudEmerald)
        if (currentWaveIndex < WaveDefinitions.waves.lastIndex) {
          betweenWaveTimer = 8.0f
        }
        syncHud()
      }
    }
  }

  private fun updateTowers(dt: Float) {
    val overdrive = overdriveTimer > 0f
    var laserIdx = 0

    for (t in towers) {
      if (t == null) continue
      if (t.recoil > 0f) t.recoil = (t.recoil - dt * 5f).coerceAtLeast(0f)
      if (t.cooldownTimer > 0f) t.cooldownTimer = (t.cooldownTimer - dt).coerceAtLeast(0f)

      when (t.type) {
        TowerType.FREEZE -> {
          // Continuous aura slow + periodic frost pulse damage + track nearest target in aura
          val range = t.effectiveRange()
          val primaryTarget = findTarget(t)
          if (primaryTarget != null) {
            val desiredAngle = atan2((primaryTarget.y - t.y) * viewportAspect, primaryTarget.x - t.x)
            val err = slewTurret(t, desiredAngle, maxTurnSpeed = if (overdrive) 7.5f else 5.5f, accelRate = 15f, dt = dt)
            t.targetLocked = abs(err) < 0.42f
          } else {
            t.targetLocked = false
            idleSweepTurret(t, dt)
          }

          var hasTargets = false
          for (e in enemyPool) {
            if (!e.active) continue
            if (distW(t.x, t.y, e.x, e.y) <= range) {
              hasTargets = true
              e.slowTimer = maxOf(e.slowTimer, 0.35f)
              e.slowStrength = maxOf(e.slowStrength, t.effectiveSlow())
            }
          }
          if (hasTargets && t.cooldownTimer <= 0f) {
            t.cooldownTimer = 1f / t.effectiveFireRate(overdrive)
            t.recoil = 0.45f
            spawnShockwave(t.x, t.y, range, IceBlue, 0.5f)
            for (e in enemyPool) {
              if (e.active && distW(t.x, t.y, e.x, e.y) <= range) {
                damageEnemy(e, t.effectiveDamage() * 0.5f, t.slotId, ignoreArmor = true)
              }
            }
          }
        }

        TowerType.SHIELD -> {
          // Track nearest ground threat near dome/range + zap held enemies
          var watchTarget: EnemyEntity? = null
          var bestDist = t.effectiveRange() * 1.15f
          for (e in enemyPool) {
            if (!e.active || e.type.isFlying) continue
            val d = distW(t.x, t.y, e.x, e.y)
            if (d < bestDist) {
              bestDist = d
              watchTarget = e
            }
          }
          if (watchTarget != null) {
            val desiredAngle = atan2((watchTarget.y - t.y) * viewportAspect, watchTarget.x - t.x)
            val err = slewTurret(t, desiredAngle, maxTurnSpeed = if (overdrive) 8.0f else 6.0f, accelRate = 16f, dt = dt)
            t.targetLocked = abs(err) < 0.45f
          } else {
            t.targetLocked = false
            idleSweepTurret(t, dt)
          }

          if (t.cooldownTimer <= 0f && t.shieldOverloadTimer <= 0f) {
            t.cooldownTimer = 1f / t.effectiveFireRate(overdrive)
            for (e in enemyPool) {
              if (e.active && !e.type.isFlying && distW(t.roadAnchorX, t.roadAnchorY, e.x, e.y) <= t.type.splashRadius) {
                damageEnemy(e, t.effectiveDamage(), t.slotId, ignoreArmor = false)
                spawnLightningArc(t.x, t.y, e.x, e.y, ShieldDomeBlue, 2.2f, 0.14f)
              }
            }
          }
        }

        TowerType.LASER -> {
          val target = findLaserTargetWithLock(t)
          if (target != null) {
            if (t.laserTargetId != target.id) {
              t.laserTargetId = target.id
              t.laserRamp = (t.laserRamp * 0.35f)
            }
            val desiredAngle = atan2((target.y - t.y) * viewportAspect, target.x - t.x)
            val turnMax = if (overdrive) 8.4f else 6.2f
            val angleErr = slewTurret(t, desiredAngle, maxTurnSpeed = turnMax, accelRate = 18f, dt = dt)
            val aligned = abs(angleErr) < 0.28f
            t.targetLocked = aligned

            if (aligned) {
              t.laserRamp = (t.laserRamp + dt * 0.65f).coerceAtMost(1.8f)
              val rampFrac = (t.laserRamp / 1.8f).coerceIn(0f, 1f)
              val dps = t.effectiveDamage() * (1f + t.laserRamp * 0.55f) * (if (overdrive) 1.65f else 1f)
              damageEnemy(target, dps * dt, t.slotId, ignoreArmor = true)
              t.recoil = (0.06f + 0.08f * rampFrac).coerceAtLeast(t.recoil)

              if (laserIdx < laserBeams.size) {
                val beam = laserBeams[laserIdx++]
                beam.active = true
                val levelScale = 1f + (t.level - 1) * 0.06f
                val muzzleDist = 0.066f * levelScale
                beam.x1 = t.x + cos(t.angleRad) * muzzleDist
                beam.y1 = t.y + sin(t.angleRad) * muzzleDist / viewportAspect
                beam.x2 = target.x
                beam.y2 = target.y
                beam.color = NeonCyan
                beam.intensity = 0.78f + rampFrac * 0.65f
                beam.rampFraction = rampFrac
                beam.towerLevel = t.level
                beam.targetRadius = target.type.radius
              }
              if (rng.nextFloat() < dt * (14f + t.laserRamp * 10f)) {
                val sparkColor = if (t.laserRamp > 0.7f && rng.nextFloat() < 0.45f) GoldAmber else NeonCyan
                spawnBurstParticles(target.x, target.y, sparkColor, 1, 0.075f)
              }
            } else {
              t.laserRamp = (t.laserRamp - dt * 0.75f).coerceAtLeast(0f)
            }
          } else {
            t.laserTargetId = -1
            t.targetLocked = false
            t.laserRamp = (t.laserRamp - dt * 1.4f).coerceAtLeast(0f)
            idleSweepTurret(t, dt)
          }
        }

        TowerType.TESLA -> {
          val target = findTarget(t)
          if (target != null) {
            val desiredAngle = atan2((target.y - t.y) * viewportAspect, target.x - t.x)
            val angleErr = slewTurret(t, desiredAngle, maxTurnSpeed = if (overdrive) 11f else 8.5f, accelRate = 20f, dt = dt)
            t.targetLocked = abs(angleErr) < 0.36f
            if (t.targetLocked && t.cooldownTimer <= 0f) {
              t.cooldownTimer = 1f / t.effectiveFireRate(overdrive)
              t.recoil = 0.8f
              fireChainLightning(t, target)
            }
          } else {
            t.targetLocked = false
            idleSweepTurret(t, dt)
          }
        }

        TowerType.EMP -> {
          val target = findTarget(t)
          if (target != null) {
            val desiredAngle = atan2((target.y - t.y) * viewportAspect, target.x - t.x)
            val err = slewTurret(t, desiredAngle, maxTurnSpeed = if (overdrive) 9.5f else 7.2f, accelRate = 18f, dt = dt)
            t.targetLocked = abs(err) < 0.45f
            if (t.cooldownTimer <= 0f) {
              t.cooldownTimer = 1f / t.effectiveFireRate(overdrive)
              t.recoil = 1f
              spawnShockwave(t.x, t.y, t.effectiveRange(), CrystalViolet, 0.42f)
              for (e in enemyPool) {
                if (!e.active) continue
                if (distW(t.x, t.y, e.x, e.y) <= t.effectiveRange()) {
                  e.empTimer = maxOf(e.empTimer, t.type.empDuration + (t.level - 1) * 0.8f)
                  e.slowTimer = maxOf(e.slowTimer, 1.5f)
                  e.slowStrength = maxOf(e.slowStrength, t.effectiveSlow())
                  // EMP deals bonus shield damage
                  if (e.shield > 0f) {
                    e.shield = (e.shield - t.effectiveDamage() * 2.5f).coerceAtLeast(0f)
                  }
                  damageEnemy(e, t.effectiveDamage(), t.slotId, ignoreArmor = true)
                  spawnLightningArc(t.x, t.y, e.x, e.y, CrystalViolet, 2.5f, 0.18f)
                }
              }
            }
          } else {
            t.targetLocked = false
            idleSweepTurret(t, dt)
          }
        }

        TowerType.CANNON,
        TowerType.GATLING,
        TowerType.MISSILE,
        TowerType.MORTAR -> {
          val target = findTarget(t)
          if (target != null) {
            val (aimX, aimY) = predictTargetPosition(t, target, dt)
            val desiredAngle = atan2((aimY - t.y) * viewportAspect, aimX - t.x)
            val baseTurn =
              when (t.type) {
                TowerType.GATLING -> 10.5f
                TowerType.CANNON -> 7.2f
                TowerType.MISSILE -> 5.8f
                else -> 4.4f
              }
            val turnMax = if (overdrive) baseTurn * 1.35f else baseTurn
            val angleErr = slewTurret(t, desiredAngle, maxTurnSpeed = turnMax, accelRate = turnMax * 2.6f, dt = dt)
            t.targetLocked = abs(angleErr) < 0.34f
            if (t.targetLocked && t.cooldownTimer <= 0f) {
              t.cooldownTimer = 1f / t.effectiveFireRate(overdrive)
              t.recoil = 1f
              spawnTowerProjectile(t, target, aimX, aimY)
            }
          } else {
            t.targetLocked = false
            idleSweepTurret(t, dt)
          }
        }
      }
    }
  }

  private fun normalizeAngle(rad: Float): Float {
    var a = rad
    val twoPi = 2f * PI.toFloat()
    while (a > PI.toFloat()) a -= twoPi
    while (a < -PI.toFloat()) a += twoPi
    return a
  }

  private fun slewTurret(
    t: TowerInstance,
    desiredAngle: Float,
    maxTurnSpeed: Float,
    accelRate: Float,
    dt: Float,
  ): Float {
    val err = normalizeAngle(desiredAngle - t.angleRad)
    val desiredVel = (err * 9.5f).coerceIn(-maxTurnSpeed, maxTurnSpeed)
    val blend = (accelRate * dt).coerceIn(0f, 1f)
    t.angularVelocity += (desiredVel - t.angularVelocity) * blend
    val step = t.angularVelocity * dt
    val clampedStep =
      if (abs(err) < 0.03f && abs(t.angularVelocity) < 0.9f) {
        t.angularVelocity *= 0.5f
        err * 0.65f
      } else if (abs(step) > abs(err) && err * step > 0f) {
        t.angularVelocity *= 0.4f
        err
      } else {
        step
      }
    t.angleRad = normalizeAngle(t.angleRad + clampedStep)
    return normalizeAngle(desiredAngle - t.angleRad)
  }

  private fun idleSweepTurret(t: TowerInstance, dt: Float) {
    val restAngle = atan2((t.roadAnchorY - t.y) * viewportAspect, t.roadAnchorX - t.x)
    val sweepAngle = restAngle + sin(totalTime * 1.15f + t.slotId * 1.37f) * 0.50f
    slewTurret(t, sweepAngle, maxTurnSpeed = 1.9f, accelRate = 6f, dt = dt)
  }

  private fun findLaserTargetWithLock(t: TowerInstance): EnemyEntity? {
    if (t.laserTargetId != -1) {
      val locked = enemyPool.firstOrNull { it.active && it.id == t.laserTargetId }
      if (locked != null && distW(t.x, t.y, locked.x, locked.y) <= t.effectiveRange() * 1.08f) {
        return locked
      }
    }
    return findTarget(t)
  }

  private fun predictTargetPosition(t: TowerInstance, target: EnemyEntity, dt: Float): Pair<Float, Float> {
    if (dt <= 0.0001f || target.stunTimer > 0f) return target.x to target.y
    val dist = distW(t.x, t.y, target.x, target.y)
    val projSpeed =
      when (t.type) {
        TowerType.CANNON -> 1.15f
        TowerType.GATLING -> 1.75f
        TowerType.MISSILE -> 0.78f
        TowerType.MORTAR -> (dist / 0.72f).coerceAtLeast(0.45f)
        else -> 1.5f
      }
    val leadTime =
      if (t.type == TowerType.MORTAR) 0.55f
      else (dist / projSpeed).coerceIn(0f, 0.38f)
    val vx = (target.x - target.prevX) / dt
    val vy = (target.y - target.prevY) / dt
    val aimX = (target.x + vx * leadTime * 0.75f).coerceIn(0.03f, 0.97f)
    val aimY = (target.y + vy * leadTime * 0.75f).coerceIn(0.08f, 0.92f)
    return aimX to aimY
  }

  private fun findTarget(t: TowerInstance): EnemyEntity? {
    val range = t.effectiveRange()
    var chosen: EnemyEntity? = null
    var bestScore = Float.NEGATIVE_INFINITY
    for (e in enemyPool) {
      if (!e.active) continue
      if (e.type.isFlying && !t.type.hitsAir) continue
      val d = distW(t.x, t.y, e.x, e.y)
      if (d > range) continue
      val score =
        when (t.targetPriority) {
          TargetPriority.FIRST -> e.progress()
          TargetPriority.STRONGEST -> e.hp + e.shield
          TargetPriority.CLOSEST -> -d
        }
      if (score > bestScore) {
        bestScore = score
        chosen = e
      }
    }
    return chosen
  }

  private fun fireChainLightning(t: TowerInstance, firstTarget: EnemyEntity) {
    val maxChains = t.effectiveChainCount()
    val hitIds = HashSet<Int>(maxChains)
    var current: EnemyEntity? = firstTarget
    var prevX = t.x
    var prevY = t.y
    var dmg = t.effectiveDamage()

    for (step in 0 until maxChains) {
      val target = current ?: break
      hitIds.add(target.id)
      spawnLightningArc(prevX, prevY, target.x, target.y, CrystalCyan, 3.2f - step * 0.4f, 0.22f)
      spawnBurstParticles(target.x, target.y, CrystalCyan, 4, 0.08f)
      damageEnemy(target, dmg, t.slotId, ignoreArmor = true)

      prevX = target.x
      prevY = target.y
      dmg *= 0.78f

      // Find next nearest enemy
      var next: EnemyEntity? = null
      var bestDist = 0.17f
      for (candidate in enemyPool) {
        if (!candidate.active || hitIds.contains(candidate.id)) continue
        val d = distW(prevX, prevY, candidate.x, candidate.y)
        if (d < bestDist) {
          bestDist = d
          next = candidate
        }
      }
      current = next
    }
  }

  private fun spawnTowerProjectile(
    t: TowerInstance,
    target: EnemyEntity,
    aimX: Float = target.x,
    aimY: Float = target.y,
  ) {
    val p = projectilePool.firstOrNull { !it.active } ?: return
    val muzzleDist =
      when (t.type) {
        TowerType.MISSILE -> 0.044f
        TowerType.CANNON, TowerType.GATLING, TowerType.MORTAR -> 0.050f
        else -> 0.032f
      }
    var sx = t.x + cos(t.angleRad) * muzzleDist
    var sy = t.y + sin(t.angleRad) * muzzleDist / viewportAspect

    p.active = true
    p.sourceSlotId = t.slotId
    p.targetX = aimX
    p.targetY = aimY
    p.targetEnemyId = target.id
    p.damage = t.effectiveDamage()
    p.splashRadius = t.type.splashRadius
    p.color = t.type.accentColor
    p.progress = 0f
    p.smokeTimer = 0f
    p.age = 0f
    p.trailCount = 0

    when (t.type) {
      TowerType.CANNON -> {
        p.kind = ProjectileKind.CANNON_SHELL
        p.speed = 1.15f
        p.angleRad = t.angleRad
        spawnBurstParticles(sx, sy, EnemyEmber, 4, 0.07f)
      }
      TowerType.GATLING -> {
        p.kind = ProjectileKind.GATLING_TRACER
        p.speed = 1.75f
        p.angleRad = t.angleRad
        spawnBurstParticles(sx, sy, GoldAmber, 2, 0.05f)
      }
      TowerType.MISSILE -> {
        p.kind = ProjectileKind.MISSILE
        p.speed = 0.56f
        val sideSign = if ((missileLaunchParity++ % 2) == 0) 1f else -1f
        p.curveSign = sideSign
        p.curvePhase = rng.nextFloat() * 6.28f
        // Offset launch point to left or right twin launcher pod
        val podLat = 0.015f * sideSign
        sx += -sin(t.angleRad) * podLat
        sy += cos(t.angleRad) * podLat / viewportAspect
        // Kick missile outward at an arcing angle so trajectory curves dramatically
        p.angleRad = normalizeAngle(t.angleRad + sideSign * 0.68f)
        spawnBurstParticles(sx, sy, GoldAmber, 5, 0.09f)
        spawnBurstParticles(sx, sy, EnemyCrimson, 4, 0.07f)
      }
      TowerType.MORTAR -> {
        p.kind = ProjectileKind.MORTAR_BOMB
        p.flightDuration = 0.72f
        p.angleRad = t.angleRad
        spawnBurstParticles(sx, sy, EnemyEmber, 7, 0.10f)
      }
      else -> {
        p.angleRad = t.angleRad
      }
    }

    p.startX = sx
    p.startY = sy
    p.x = sx
    p.y = sy
    p.trailX[0] = sx
    p.trailY[0] = sy
    p.trailCount = 1
  }

  private fun updateProjectiles(dt: Float) {
    for (p in projectilePool) {
      if (!p.active) continue
      val target = enemyPool.firstOrNull { it.active && it.id == p.targetEnemyId }

      if (p.kind == ProjectileKind.MORTAR_BOMB) {
        p.progress += dt / p.flightDuration
        if (p.progress >= 1f) {
          p.active = false
          spawnShockwave(p.targetX, p.targetY, p.splashRadius, p.color, 0.42f)
          spawnBurstParticles(p.targetX, p.targetY, EnemyEmber, 18, 0.18f)
          spawnBurstParticles(p.targetX, p.targetY, GoldAmber, 8, 0.12f)
          applyAreaDamage(p.targetX, p.targetY, p.splashRadius, p.damage, stunSeconds = 0f, empSeconds = 0f, sourceSlotId = p.sourceSlotId, hitAir = false)
        } else {
          p.x = p.startX + (p.targetX - p.startX) * p.progress
          p.y = p.startY + (p.targetY - p.startY) * p.progress
        }
        continue
      }

      if (target != null) {
        p.targetX = target.x
        p.targetY = target.y
      }
      val dx = p.targetX - p.x
      val dy = (p.targetY - p.y) * viewportAspect
      val dist = hypot(dx, dy)

      if (p.kind == ProjectileKind.MISSILE) {
        p.age += dt
        val curSpeed = (0.56f + p.age * 1.15f).coerceAtMost(1.24f)
        p.speed = curSpeed
        val step = curSpeed * dt

        if (dist <= step + 0.018f || p.age > 4.0f) {
          p.active = false
          spawnShockwave(p.targetX, p.targetY, p.splashRadius, p.color, 0.36f)
          spawnShockwave(p.targetX, p.targetY, p.splashRadius * 0.55f, GoldAmber, 0.24f)
          spawnBurstParticles(p.targetX, p.targetY, EnemyEmber, 14, 0.17f)
          spawnBurstParticles(p.targetX, p.targetY, GoldAmber, 8, 0.12f)
          applyAreaDamage(p.targetX, p.targetY, p.splashRadius, p.damage, stunSeconds = 0f, empSeconds = 0f, sourceSlotId = p.sourceSlotId, hitAir = true)
          continue
        }

        // Curved & spiraling homing guidance: wide sweeping corkscrew mid-flight, tightening for terminal impact
        val directBearing = atan2(dy, dx)
        val spiralAmp = ((dist - 0.032f) / 0.15f).coerceIn(0f, 1f) * 1.08f
        val spiralWave = sin(p.age * 11.5f + p.curvePhase) * spiralAmp * p.curveSign
        val launchFlare = ((0.22f - p.age) / 0.22f).coerceIn(0f, 1f) * 0.52f * p.curveSign
        val desiredAngle = normalizeAngle(directBearing + spiralWave + launchFlare)

        val turnRate = 5.6f + (1f - (dist / 0.28f).coerceIn(0f, 1f)) * 10.5f
        val angleErr = normalizeAngle(desiredAngle - p.angleRad)
        val maxTurn = turnRate * dt
        p.angleRad = normalizeAngle(p.angleRad + angleErr.coerceIn(-maxTurn, maxTurn))

        p.x += cos(p.angleRad) * curSpeed * dt
        p.y += sin(p.angleRad) * curSpeed * dt / viewportAspect

        // Record curved exhaust trail coordinates
        val maxTrail = p.trailX.size
        if (p.trailCount < maxTrail) {
          p.trailX[p.trailCount] = p.x
          p.trailY[p.trailCount] = p.y
          p.trailCount++
        } else {
          for (i in 0 until maxTrail - 1) {
            p.trailX[i] = p.trailX[i + 1]
            p.trailY[i] = p.trailY[i + 1]
          }
          p.trailX[maxTrail - 1] = p.x
          p.trailY[maxTrail - 1] = p.y
        }

        // Rich exhaust smoke + glowing thruster ember particles
        p.smokeTimer -= dt
        if (p.smokeTimer <= 0f) {
          p.smokeTimer = 0.018f
          val tailX = p.x - cos(p.angleRad) * 0.012f
          val tailY = p.y - sin(p.angleRad) * 0.012f / viewportAspect
          spawnSingleParticle(tailX, tailY, Color(0xFFCBD5E1), radius = 3.0f, life = 0.34f)
          if (rng.nextFloat() < 0.72f) {
            spawnSingleParticle(
              tailX,
              tailY,
              if (rng.nextBoolean()) GoldAmber else EnemyEmber,
              radius = 2.4f,
              life = 0.16f,
            )
          }
        }
        continue
      }

      val step = p.speed * dt
      if (dist <= step + 0.015f) {
        p.active = false
        if (p.splashRadius > 0f) {
          spawnShockwave(p.targetX, p.targetY, p.splashRadius, p.color, 0.32f)
          spawnBurstParticles(p.targetX, p.targetY, p.color, 12, 0.14f)
          applyAreaDamage(p.targetX, p.targetY, p.splashRadius, p.damage, stunSeconds = 0f, empSeconds = 0f, sourceSlotId = p.sourceSlotId, hitAir = true)
        } else if (target != null) {
          spawnBurstParticles(p.targetX, p.targetY, p.color, 5, 0.08f)
          damageEnemy(target, p.damage, p.sourceSlotId, ignoreArmor = false)
        }
      } else {
        p.angleRad = atan2(dy, dx)
        p.x += (dx / dist) * step
        p.y += (dy / dist) * step / viewportAspect
      }
    }
  }

  private fun applyAreaDamage(
    cx: Float,
    cy: Float,
    radiusW: Float,
    damage: Float,
    stunSeconds: Float,
    empSeconds: Float,
    sourceSlotId: Int = -1,
    hitAir: Boolean = true,
  ) {
    for (e in enemyPool) {
      if (!e.active) continue
      if (e.type.isFlying && !hitAir) continue
      val d = distW(cx, cy, e.x, e.y)
      if (d <= radiusW + e.type.radius) {
        val falloff = (1f - (d / (radiusW + 0.05f)) * 0.35f).coerceIn(0.6f, 1f)
        if (stunSeconds > 0f) e.stunTimer = maxOf(e.stunTimer, stunSeconds)
        if (empSeconds > 0f) e.empTimer = maxOf(e.empTimer, empSeconds)
        damageEnemy(e, damage * falloff, sourceSlotId, ignoreArmor = false)
      }
    }
  }

  private fun damageEnemy(e: EnemyEntity, rawDamage: Float, slotId: Int, ignoreArmor: Boolean) {
    if (!e.active) return
    val armorReduction = if (ignoreArmor || e.empTimer > 0f) 0f else e.type.armor * 0.45f
    var remaining = (rawDamage - armorReduction).coerceAtLeast(rawDamage * 0.25f)

    if (e.shield > 0f) {
      val absorbed = minOf(e.shield, remaining)
      e.shield -= absorbed
      remaining -= absorbed
    }

    if (remaining > 0f) {
      e.hp -= remaining
    }
    e.hitFlashTimer = 0.09f

    if (e.hp <= 0f) {
      e.active = false
      totalKills++
      gold += e.type.goldReward
      crystals += e.type.crystalReward
      if (slotId in towers.indices) {
        towers[slotId]?.let { it.kills++ }
      }
      spawnShockwave(e.x, e.y, e.type.radius * 2.6f, e.type.accentColor, 0.30f)
      spawnBurstParticles(e.x, e.y, e.type.accentColor, 14, 0.15f)
      spawnFloatingText(e.x, e.y - 0.02f, "+${e.type.goldReward}g", GoldAmber)
    }
  }

  private fun updateEffects(dt: Float) {
    for (pt in particlePool) {
      if (!pt.active) continue
      pt.life -= dt
      if (pt.life <= 0f) {
        pt.active = false
      } else {
        pt.x += pt.vx * dt
        pt.y += pt.vy * dt / viewportAspect
        pt.vx *= 0.94f
        pt.vy *= 0.94f
      }
    }
    for (sw in shockwavePool) {
      if (!sw.active) continue
      sw.life -= dt
      if (sw.life <= 0f) sw.active = false
    }
    for (la in lightningPool) {
      if (!la.active) continue
      la.life -= dt
      if (la.life <= 0f) la.active = false
    }
    for (ft in floatingTextPool) {
      if (!ft.active) continue
      ft.life -= dt
      ft.y -= dt * 0.032f
      if (ft.life <= 0f) ft.active = false
    }
  }

  private fun spawnBurstParticles(x: Float, y: Float, color: Color, count: Int, maxSpeed: Float) {
    var spawned = 0
    for (pt in particlePool) {
      if (pt.active) continue
      val ang = rng.nextFloat() * 2f * PI.toFloat()
      val spd = (0.25f + rng.nextFloat() * 0.75f) * maxSpeed
      pt.active = true
      pt.x = x
      pt.y = y
      pt.vx = cos(ang) * spd
      pt.vy = sin(ang) * spd
      pt.radius = 2f + rng.nextFloat() * 3.5f
      pt.maxLife = 0.22f + rng.nextFloat() * 0.26f
      pt.life = pt.maxLife
      pt.color = color
      pt.glow = true
      spawned++
      if (spawned >= count) break
    }
  }

  private fun spawnSingleParticle(x: Float, y: Float, color: Color, radius: Float, life: Float) {
    val pt = particlePool.firstOrNull { !it.active } ?: return
    pt.active = true
    pt.x = x
    pt.y = y
    pt.vx = (rng.nextFloat() - 0.5f) * 0.02f
    pt.vy = (rng.nextFloat() - 0.5f) * 0.02f
    pt.radius = radius
    pt.maxLife = life
    pt.life = life
    pt.color = color
    pt.glow = false
  }

  private fun spawnShockwave(x: Float, y: Float, maxRadius: Float, color: Color, life: Float) {
    val sw = shockwavePool.firstOrNull { !it.active } ?: return
    sw.active = true
    sw.x = x
    sw.y = y
    sw.maxRadius = maxRadius
    sw.maxLife = life
    sw.life = life
    sw.color = color
  }

  private fun spawnLightningArc(x1: Float, y1: Float, x2: Float, y2: Float, color: Color, thickness: Float, life: Float) {
    val la = lightningPool.firstOrNull { !it.active } ?: return
    la.active = true
    la.x1 = x1
    la.y1 = y1
    la.x2 = x2
    la.y2 = y2
    la.color = color
    la.thickness = thickness
    la.maxLife = life
    la.life = life
    la.seed = rng.nextInt(1, 9999)
  }

  private fun spawnFloatingText(x: Float, y: Float, text: String, color: Color) {
    val ft = floatingTextPool.firstOrNull { !it.active } ?: return
    ft.active = true
    ft.x = x
    ft.y = y
    ft.text = text
    ft.color = color
    ft.maxLife = 0.85f
    ft.life = ft.maxLife
  }

  private fun showBanner(msg: String) {
    statusBannerTimer = 2.6f
    _hudState.update { it.copy(statusBanner = msg) }
  }

  private fun hasActiveEnemies(): Boolean = enemyPool.any { it.active }

  private fun distW(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return hypot(x2 - x1, (y2 - y1) * viewportAspect)
  }

  private fun syncHud() {
    val selectedId = _hudState.value.selectedSlotId
    val selectedTower = selectedId?.let { towers.getOrNull(it)?.toSnapshot() }
    _hudState.update {
      it.copy(
        phase = phase,
        baseHp = baseHp,
        gold = gold,
        crystals = crystals,
        wave = (currentWaveIndex + 1).coerceAtLeast(0),
        waveInProgress = waveSpawningActive || hasActiveEnemies(),
        betweenWaveCountdown = betweenWaveTimer,
        speedMultiplier = speedMultiplier,
        selectedTower = selectedTower,
        abilityCooldowns = abilityCooldowns.toMap(),
        globalFreezeTimer = globalFreezeTimer,
        overdriveTimer = overdriveTimer,
        totalKills = totalKills,
        towersBuiltCount = towers.count { t -> t != null },
      )
    }
  }

  private fun TowerInstance.toSnapshot(): TowerSnapshot {
    val overdrive = overdriveTimer > 0f
    return TowerSnapshot(
      slotId = slotId,
      type = type,
      level = level,
      damage = effectiveDamage(),
      range = effectiveRange(),
      fireRate = effectiveFireRate(overdrive),
      upgradeCost = type.upgradeCost(level),
      sellRefund = sellRefund(),
      targetPriority = targetPriority,
      kills = kills,
      x = x,
      y = y,
    )
  }
}

private object HudUiStateCompat {
  fun initial() = com.example.game.model.HudUiState()
}
