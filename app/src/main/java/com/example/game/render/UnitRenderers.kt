package com.example.game.render

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.game.model.EnemyEntity
import com.example.game.model.EnemyType
import com.example.game.model.TowerInstance
import com.example.game.model.TowerType
import com.example.ui.theme.BossGoldRed
import com.example.ui.theme.CrystalViolet
import com.example.ui.theme.EnemyCrimson
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.IceBlue
import com.example.ui.theme.NeonCyan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun DrawScope.drawHexPlatform(
  center: Offset,
  radius: Float,
  isSelected: Boolean,
  hasTower: Boolean,
  time: Float,
  padSprite: ImageBitmap? = null,
) {
  val pulse = 0.55f + 0.45f * sin(time * 3.2f)

  // Drop shadow
  drawOval(
    color = Color(0x9902040A),
    topLeft = Offset(center.x - radius * 1.08f, center.y - radius * 0.65f),
    size = Size(radius * 2.16f, radius * 1.72f),
  )

  if (padSprite != null) {
    val side = (radius * 2.28f).roundToInt().coerceAtLeast(16)
    val half = side / 2
    drawImage(
      image = padSprite,
      dstOffset = IntOffset((center.x - half).roundToInt(), (center.y - half).roundToInt()),
      dstSize = IntSize(side, side),
    )
  } else {
    val hexPath = buildHexPath(center, radius)
    drawPath(path = hexPath, color = Color(0xFF1A253A))
    drawPath(path = hexPath, color = NeonCyan.copy(alpha = 0.45f), style = Stroke(width = 1.8f))
  }

  if (isSelected) {
    drawCircle(
      color = NeonCyan.copy(alpha = 0.85f),
      radius = radius * 1.15f,
      center = center,
      style = Stroke(width = 3.0f),
    )
  } else if (!hasTower) {
    drawCircle(
      color = NeonCyan.copy(alpha = 0.25f + 0.30f * pulse),
      radius = radius * 1.06f,
      center = center,
      style = Stroke(width = 1.5f),
    )
    val cross = radius * 0.24f
    val cColor = NeonCyan.copy(alpha = 0.40f + 0.35f * pulse)
    drawLine(cColor, Offset(center.x - cross, center.y), Offset(center.x + cross, center.y), strokeWidth = 2f)
    drawLine(cColor, Offset(center.x, center.y - cross), Offset(center.x, center.y + cross), strokeWidth = 2f)
  }
}

fun DrawScope.drawTowerModel(
  tower: TowerInstance,
  center: Offset,
  baseRadius: Float,
  time: Float,
  overdriveActive: Boolean,
  sprites: GameSprites? = null,
) {
  val accent = tower.type.accentColor

  if (overdriveActive) {
    drawCircle(
      color = GoldAmber.copy(alpha = 0.24f + 0.14f * sin(time * 9f)),
      radius = baseRadius * 1.28f,
      center = center,
    )
  }

  // Upgrade level energy rings
  if (tower.level >= 2) {
    drawCircle(
      color = accent.copy(alpha = 0.48f),
      radius = baseRadius * 1.08f,
      center = center,
      style = Stroke(width = 2.0f),
    )
  }
  if (tower.level >= 3) {
    drawCircle(
      color = GoldAmber.copy(alpha = 0.72f),
      radius = baseRadius * 1.20f,
      center = center,
      style = Stroke(width = 2.2f),
    )
  }

  val sprite = sprites?.towers?.get(tower.type)
  if (sprite != null) {
    val scaleFactor =
      when (tower.level) {
        1 -> 1.0f
        2 -> 1.06f
        else -> 1.12f
      }

    if (tower.type == TowerType.LASER) {
      // Top-down circular turret centered at (50%, 50%) with barrel pointing UP (-90 deg)
      val recoilPush = tower.recoil * baseRadius * 0.14f
      val drawCenter =
        Offset(
          center.x - cos(tower.angleRad) * recoilPush,
          center.y - sin(tower.angleRad) * recoilPush,
        )
      val spriteSide = (baseRadius * 3.05f * scaleFactor).roundToInt().coerceAtLeast(20)
      val half = spriteSide / 2
      val rotationDeg = Math.toDegrees(tower.angleRad.toDouble()).toFloat() + 90f

      rotate(degrees = rotationDeg, pivot = drawCenter) {
        drawImage(
          image = sprite,
          dstOffset = IntOffset((drawCenter.x - half).roundToInt(), (drawCenter.y - half).roundToInt()),
          dstSize = IntSize(spriteSide, spriteSide),
        )
      }

      // Glowing plasma lens flare at the barrel aperture when locked / charging
      val rampFrac = (tower.laserRamp / 1.8f).coerceIn(0f, 1f)
      if (tower.targetLocked || rampFrac > 0.02f) {
        val muzzleTip =
          Offset(
            drawCenter.x + cos(tower.angleRad) * (spriteSide * 0.44f),
            drawCenter.y + sin(tower.angleRad) * (spriteSide * 0.44f),
          )
        val glowR = baseRadius * (0.28f + 0.18f * rampFrac)
        drawCircle(
          color = NeonCyan.copy(alpha = 0.42f + 0.35f * rampFrac),
          radius = glowR,
          center = muzzleTip,
        )
        drawCircle(
          color = Color.White.copy(alpha = 0.75f + 0.25f * rampFrac),
          radius = glowR * 0.42f,
          center = muzzleTip,
        )
      }
    } else {
      // 2.5D upright isometric model with subtle recoil kick + facing flip
      val recoilPush = tower.recoil * baseRadius * 0.16f
      val drawCenter =
        Offset(
          center.x - cos(tower.angleRad) * recoilPush,
          center.y - sin(tower.angleRad) * recoilPush - baseRadius * 0.10f,
        )
      val spriteSide = (baseRadius * 2.42f * scaleFactor).roundToInt().coerceAtLeast(16)
      val half = spriteSide / 2

      val mirrorX =
        if (tower.type in setOf(TowerType.CANNON, TowerType.GATLING, TowerType.MISSILE, TowerType.MORTAR) &&
          cos(tower.angleRad) < -0.15f
        ) {
          -1f
        } else {
          1f
        }

      scale(scaleX = mirrorX, scaleY = 1f, pivot = drawCenter) {
        drawImage(
          image = sprite,
          dstOffset = IntOffset((drawCenter.x - half).roundToInt(), (drawCenter.y - half).roundToInt()),
          dstSize = IntSize(spriteSide, spriteSide),
        )
      }

      // Subtle directional muzzle flash/emitter indicator toward target
      if (tower.recoil > 0.25f) {
        val tip =
          Offset(
            center.x + cos(tower.angleRad) * baseRadius * 0.88f,
            center.y + sin(tower.angleRad) * baseRadius * 0.88f,
          )
        drawCircle(accent.copy(alpha = 0.75f * tower.recoil), radius = baseRadius * 0.28f, center = tip)
      }
    }
  } else {
    drawCircle(accent, radius = baseRadius * 0.6f, center = center)
  }

  // Level pips along bottom of tower platform
  val pipSpacing = baseRadius * 0.30f
  val startX = center.x - ((tower.level - 1) * pipSpacing) * 0.5f
  val pipY = center.y + baseRadius * 0.86f
  for (lvl in 0 until tower.level) {
    drawCircle(
      color = GoldAmber,
      radius = 3.2f,
      center = Offset(startX + lvl * pipSpacing, pipY),
    )
  }
}

fun DrawScope.drawEnemyUnit(
  e: EnemyEntity,
  center: Offset,
  unitRadius: Float,
  time: Float,
  sprites: GameSprites? = null,
) {
  // Ground shadow (offset further down for flying jet drones)
  val shadowY = if (e.type.isFlying) unitRadius * 1.45f else unitRadius * 0.45f
  drawOval(
    color = Color(0x88000000),
    topLeft = Offset(center.x - unitRadius * 0.90f, center.y + shadowY - unitRadius * 0.40f),
    size = Size(unitRadius * 1.80f, unitRadius * 0.80f),
  )

  val sprite = sprites?.enemies?.get(e.type)
  if (sprite != null) {
    val sizeMult =
      when (e.type) {
        EnemyType.SCOUT_DRONE -> 2.35f
        EnemyType.CYBER_SPIDER -> 2.45f
        EnemyType.WALKER -> 2.45f
        EnemyType.TANK_BOT -> 2.50f
        EnemyType.FLYING_DRONE -> 2.55f
        EnemyType.ELITE_BOSS -> 2.80f
      }
    val spriteSide = (unitRadius * sizeMult).roundToInt().coerceAtLeast(16)
    val half = spriteSide / 2

    // Dynamic stride bob & subtle banking toward movement direction
    val bobY =
      if (e.type.isFlying) sin(time * 6f + e.id) * 3.0f
      else sin(e.animPhase) * (if (e.type == EnemyType.ELITE_BOSS) 1.8f else 2.2f)
    val bankDeg = (cos(e.angleRad) * 16f)

    val tint =
      when {
        e.hitFlashTimer > 0f -> ColorFilter.tint(Color.White.copy(alpha = 0.55f), androidx.compose.ui.graphics.BlendMode.SrcAtop)
        e.empTimer > 0f -> ColorFilter.tint(CrystalViolet.copy(alpha = 0.35f), androidx.compose.ui.graphics.BlendMode.SrcAtop)
        e.slowTimer > 0f -> ColorFilter.tint(IceBlue.copy(alpha = 0.35f), androidx.compose.ui.graphics.BlendMode.SrcAtop)
        else -> null
      }

    val drawPos = Offset(center.x, center.y + bobY)
    rotate(bankDeg, pivot = drawPos) {
      drawImage(
        image = sprite,
        dstOffset = IntOffset((drawPos.x - half).roundToInt(), (drawPos.y - half).roundToInt()),
        dstSize = IntSize(spriteSide, spriteSide),
        colorFilter = tint,
      )
    }

    // Hostile crimson/orange visor glow indicator so enemies clearly contrast blue defenders
    drawCircle(
      color = e.type.accentColor.copy(alpha = 0.32f),
      radius = unitRadius * 0.45f,
      center = drawPos,
    )
  } else {
    drawCircle(e.type.accentColor, radius = unitRadius, center = center)
  }

  // Active energy shield bubble around unit
  if (e.shield > 0f && e.empTimer <= 0f) {
    drawCircle(
      color = Color(0xFF38BDF8).copy(alpha = 0.42f),
      radius = unitRadius * 1.24f,
      center = center,
      style = Stroke(width = 2.0f),
    )
  }

  // Slow frost ring indicator
  if (e.slowTimer > 0f) {
    drawCircle(
      color = IceBlue.copy(alpha = 0.55f),
      radius = unitRadius * 1.12f,
      center = center,
      style = Stroke(width = 1.8f),
    )
  }

  // Compact HP & Shield Bar above damaged/boss enemies
  val showBar = e.hp < e.maxHp || e.shield < e.maxShield || e.type == EnemyType.ELITE_BOSS
  if (showBar) {
    val barW = unitRadius * 2.3f
    val barH = if (e.type == EnemyType.ELITE_BOSS) 6f else 4.2f
    val barX = center.x - barW * 0.5f
    val barY = center.y - unitRadius * 1.38f
    drawRoundRect(
      color = Color(0xDD060912),
      topLeft = Offset(barX - 1f, barY - 1f),
      size = Size(barW + 2f, barH + 2f),
      cornerRadius = CornerRadius(2f, 2f),
    )
    val hpFrac = (e.hp / e.maxHp).coerceIn(0f, 1f)
    drawRoundRect(
      color = if (e.type == EnemyType.ELITE_BOSS) BossGoldRed else EnemyCrimson,
      topLeft = Offset(barX, barY),
      size = Size(barW * hpFrac, barH),
      cornerRadius = CornerRadius(1.5f, 1.5f),
    )
    if (e.maxShield > 0f && e.shield > 0f) {
      val shFrac = (e.shield / e.maxShield).coerceIn(0f, 1f)
      drawRoundRect(
        color = NeonCyan,
        topLeft = Offset(barX, barY - 3.8f),
        size = Size(barW * shFrac, 2.5f),
        cornerRadius = CornerRadius(1f, 1f),
      )
    }
  }
}

fun buildHexPath(center: Offset, radius: Float, rotationRad: Float = 0f): Path {
  val path = Path()
  for (i in 0 until 6) {
    val ang = rotationRad + i * (PI.toFloat() / 3f)
    val px = center.x + cos(ang) * radius
    val py = center.y + sin(ang) * radius
    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
  }
  path.close()
  return path
}
