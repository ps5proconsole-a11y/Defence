package com.example.game.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.map.CanyonMap
import com.example.game.model.AbilityType
import com.example.game.model.ProjectileKind
import com.example.game.model.TowerType
import com.example.ui.theme.CanyonCliffDark
import com.example.ui.theme.EnemyCrimson
import com.example.ui.theme.EnemyEmber
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.IceBlue
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoadMetalCurb
import com.example.ui.theme.RoadSurface
import com.example.ui.theme.ShieldDomeBlue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun BattlefieldCanvas(
  engine: GameEngine,
  selectedSlotId: Int?,
  activeAbilityTargeting: AbilityType?,
  globalFreezeTimer: Float,
  overdriveTimer: Float,
  onTapMap: (normX: Float, normY: Float) -> Unit,
  onViewportAspectChanged: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()
  val sprites = rememberGameSprites()
  val beamArcPath = remember { Path() }
  val stars = remember {
    val r = Random(1337)
    List(42) {
      Triple(r.nextFloat(), r.nextFloat() * 0.25f, 1.2f + r.nextFloat() * 1.8f)
    }
  }

  Canvas(
    modifier =
      modifier
        .fillMaxSize()
        .onSizeChanged { sz ->
          if (sz.width > 0 && sz.height > 0) {
            onViewportAspectChanged(sz.height.toFloat() / sz.width.toFloat())
          }
        }
        .pointerInput(Unit) {
          detectTapGestures { offset ->
            if (size.width > 0 && size.height > 0) {
              onTapMap(offset.x / size.width.toFloat(), offset.y / size.height.toFloat())
            }
          }
        },
  ) {
    @Suppress("UNUSED_VARIABLE")
    val tick = engine.renderTick
    val t = engine.totalTime
    val w = size.width
    val h = size.height

    // 1. Night alien sky & rocky canyon gradient
    drawRect(
      brush =
        Brush.verticalGradient(
          colors = listOf(Color(0xFF040712), Color(0xFF091224), CanyonCliffDark, Color(0xFF0A101D)),
          startY = 0f,
          endY = h,
        ),
    )

    // Twinkling stars
    for ((sx, sy, sr) in stars) {
      val twinkle = 0.4f + 0.6f * sin(t * 2.2f + sx * 20f)
      drawCircle(Color(0xFFCFE8FF).copy(alpha = twinkle), radius = sr, center = Offset(sx * w, sy * h))
    }

    // Distant planet sphere sprite
    drawAlienPlanetSprite(w, h, sprites.planet)

    // Futuristic spires along upper ridge
    drawHorizonSpiresSprite(w, h, sprites.spire)

    // 2. Rocky canyon tiles, coniferous trees, and glowing blue crystals
    for (rock in CanyonMap.rocks) {
      val rw = (rock.rx * 2.4f * w).roundToInt().coerceAtLeast(24)
      val cx = (rock.x * w).roundToInt()
      val cy = (rock.y * h).roundToInt()
      drawImage(
        image = sprites.rocks,
        dstOffset = IntOffset(cx - rw / 2, cy - rw / 2),
        dstSize = IntSize(rw, rw),
        alpha = 0.88f,
      )
    }

    for (tree in CanyonMap.trees) {
      val ts = (w * 0.082f * tree.scale).roundToInt().coerceAtLeast(20)
      val cx = (tree.x * w).roundToInt()
      val cy = (tree.y * h).roundToInt()
      drawImage(
        image = sprites.tree,
        dstOffset = IntOffset(cx - ts / 2, cy - (ts * 0.72f).roundToInt()),
        dstSize = IntSize(ts, ts),
      )
    }

    for (crystal in CanyonMap.crystals) {
      val cx = crystal.x * w
      val cy = crystal.y * h
      val cs = (w * 0.088f * crystal.scale).roundToInt().coerceAtLeast(20)
      val glow = 0.55f + 0.45f * sin((t + crystal.phase) * 2.6f)
      drawCircle(
        brush = Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.34f * glow), Color.Transparent), center = Offset(cx, cy), radius = cs * 0.7f),
        radius = cs * 0.7f,
        center = Offset(cx, cy),
      )
      drawImage(
        image = sprites.crystals,
        dstOffset = IntOffset(cx.roundToInt() - cs / 2, cy.roundToInt() - cs / 2),
        dstSize = IntSize(cs, cs),
      )
    }

    // 3. Winding S-shaped serpentine road with metallic curbs & animated neon chevrons
    drawSerpentineRoad(w, h, t)

    // 4. Enemy Portal (top) & Defended Energy Gate Base (bottom) using sprite sheet structures
    drawEnemyPortalSprite(
      Offset(CanyonMap.portalPosition.x * w, CanyonMap.portalPosition.y * h),
      w * 0.092f,
      t,
      sprites.portal,
    )
    drawBaseEnergyGateSprite(
      Offset(CanyonMap.baseGatePosition.x * w, CanyonMap.baseGatePosition.y * h),
      w * 0.105f,
      t,
      sprites.baseGate,
    )

    // 5. Tower Freeze & Shield Aura Domes
    for (tower in engine.towers) {
      if (tower == null) continue
      val tc = Offset(tower.x * w, tower.y * h)
      if (tower.type == TowerType.FREEZE) {
        val rPx = tower.effectiveRange() * w
        drawCircle(
          brush = Brush.radialGradient(listOf(IceBlue.copy(alpha = 0.14f), IceBlue.copy(alpha = 0.03f)), center = tc, radius = rPx),
          radius = rPx,
          center = tc,
        )
        drawCircle(IceBlue.copy(alpha = 0.25f), radius = rPx, center = tc, style = Stroke(width = 1.4f))
      } else if (tower.type == TowerType.SHIELD) {
        val anchor = Offset(tower.roadAnchorX * w, tower.roadAnchorY * h)
        val domeR = tower.type.splashRadius * w
        val active = tower.shieldOverloadTimer <= 0f
        val pulse = 0.65f + 0.35f * sin(t * 5f)
        val hexDome = buildHexPath(anchor, domeR, rotationRad = t * 0.25f)
        if (active) {
          drawPath(hexDome, color = ShieldDomeBlue.copy(alpha = 0.18f * pulse))
          drawPath(hexDome, color = NeonCyan.copy(alpha = 0.65f * pulse), style = Stroke(width = 2.4f))
        } else {
          drawPath(hexDome, color = EnemyCrimson.copy(alpha = 0.25f), style = Stroke(width = 1.5f))
        }
      }
    }

    // Selected slot range circle
    if (selectedSlotId != null) {
      val selTower = engine.towers.getOrNull(selectedSlotId)
      if (selTower != null) {
        val tc = Offset(selTower.x * w, selTower.y * h)
        val rPx = selTower.effectiveRange() * w
        drawCircle(selTower.type.accentColor.copy(alpha = 0.10f), radius = rPx, center = tc)
        drawCircle(
          color = selTower.type.accentColor.copy(alpha = 0.65f),
          radius = rPx,
          center = tc,
          style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))),
        )
      }
    }

    // 6. 11 Hexagonal Build Platforms + 9 Tower Models
    val hexRadius = w * 0.056f
    for (slot in CanyonMap.buildSlots) {
      val c = Offset(slot.x * w, slot.y * h)
      val tower = engine.towers[slot.id]
      drawHexPlatform(
        center = c,
        radius = hexRadius,
        isSelected = slot.id == selectedSlotId,
        hasTower = tower != null,
        time = t,
        padSprite = sprites.pad,
      )
      if (tower != null) {
        drawTowerModel(tower, c, hexRadius * 0.86f, t, overdriveActive = overdriveTimer > 0f, sprites = sprites)
      }
    }

    // 7. Ground & Air Enemies
    for (e in engine.enemyPool) {
      if (!e.active) continue
      drawEnemyUnit(e, Offset(e.x * w, e.y * h), e.type.radius * w, t, sprites = sprites)
    }

    // 8. Continuous Multi-Layered Plasma Laser Beams
    for (lb in engine.laserBeams) {
      if (!lb.active) continue
      drawBeautifulLaserBeam(
        start = Offset(lb.x1 * w, lb.y1 * h),
        end = Offset(lb.x2 * w, lb.y2 * h),
        baseColor = lb.color,
        intensity = lb.intensity,
        rampFraction = lb.rampFraction,
        towerLevel = lb.towerLevel,
        time = t,
        viewportWidth = w,
        reusablePath = beamArcPath,
      )
    }

    // 9. Projectiles
    for (p in engine.projectilePool) {
      if (!p.active) continue
      val px = p.x * w
      val py = p.y * h
      when (p.kind) {
        ProjectileKind.CANNON_SHELL -> {
          drawCircle(EnemyEmber.copy(alpha = 0.45f), radius = 7.5f, center = Offset(px, py))
          drawCircle(GoldAmber, radius = 4f, center = Offset(px, py))
        }
        ProjectileKind.GATLING_TRACER -> {
          val tailX = px - cos(p.angleRad) * 14f
          val tailY = py - sin(p.angleRad) * 14f
          drawLine(GoldAmber, Offset(tailX, tailY), Offset(px, py), strokeWidth = 2.6f, cap = StrokeCap.Round)
        }
        ProjectileKind.MISSILE -> {
          rotate(Math.toDegrees(p.angleRad.toDouble()).toFloat(), pivot = Offset(px, py)) {
            drawLine(EnemyCrimson, Offset(px - 8f, py), Offset(px + 6f, py), strokeWidth = 4.5f, cap = StrokeCap.Round)
            drawCircle(GoldAmber, radius = 2.5f, center = Offset(px - 9f, py))
          }
        }
        ProjectileKind.MORTAR_BOMB -> {
          val arcLift = sin(p.progress * PI.toFloat()) * w * 0.085f
          drawOval(
            color = Color(0x88000000),
            topLeft = Offset(px - 7f, py - 4f),
            size = Size(14f, 8f),
          )
          val scale = 1f + sin(p.progress * PI.toFloat()) * 0.65f
          drawCircle(EnemyEmber.copy(alpha = 0.4f), radius = 8f * scale, center = Offset(px, py - arcLift))
          drawCircle(Color(0xFFFFB300), radius = 4.5f * scale, center = Offset(px, py - arcLift))
        }
      }
    }

    // 10. Jagged Lightning Arcs
    for (la in engine.lightningPool) {
      if (!la.active) continue
      val alpha = (la.life / la.maxLife).coerceIn(0f, 1f)
      drawJaggedArc(
        start = Offset(la.x1 * w, la.y1 * h),
        end = Offset(la.x2 * w, la.y2 * h),
        color = la.color.copy(alpha = alpha),
        thickness = la.thickness,
        seed = la.seed,
      )
    }

    // 11. Shockwaves & Particles
    for (sw in engine.shockwavePool) {
      if (!sw.active) continue
      val p = 1f - (sw.life / sw.maxLife).coerceIn(0f, 1f)
      val r = sw.maxRadius * w * p
      drawCircle(
        color = sw.color.copy(alpha = (1f - p) * 0.75f),
        radius = r,
        center = Offset(sw.x * w, sw.y * h),
        style = Stroke(width = 3.5f * (1f - p) + 1f),
      )
    }
    for (pt in engine.particlePool) {
      if (!pt.active) continue
      val alpha = (pt.life / pt.maxLife).coerceIn(0f, 1f)
      val c = Offset(pt.x * w, pt.y * h)
      if (pt.glow) {
        drawCircle(pt.color.copy(alpha = alpha * 0.35f), radius = pt.radius * 2f, center = c)
      }
      drawCircle(pt.color.copy(alpha = alpha), radius = pt.radius * alpha, center = c)
    }

    // 12. Global Cryo-Freeze Screen Tint
    if (globalFreezeTimer > 0f) {
      drawRect(
        color = IceBlue.copy(alpha = 0.11f),
        style = Stroke(width = 16f),
      )
    }

    // 13. Floating Gold / Damage Numbers
    for (ft in engine.floatingTextPool) {
      if (!ft.active) continue
      val alpha = (ft.life / ft.maxLife).coerceIn(0f, 1f)
      val measured =
        textMeasurer.measure(
          text = ft.text,
          style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp),
        )
      drawText(
        textLayoutResult = measured,
        color = ft.color.copy(alpha = alpha),
        topLeft = Offset(ft.x * w - measured.size.width * 0.5f, ft.y * h),
      )
    }

    // 14. Targeting Overlay Indicator
    if (activeAbilityTargeting != null) {
      val pulse = 0.55f + 0.45f * sin(t * 7f)
      drawRect(
        color = activeAbilityTargeting.accentColor.copy(alpha = 0.25f * pulse),
        style = Stroke(width = 6f),
      )
    }
  }
}

private fun DrawScope.drawAlienPlanetSprite(w: Float, h: Float, planet: ImageBitmap) {
  val center = Offset(w * 0.80f, h * 0.088f)
  val r = w * 0.145f
  drawCircle(
    brush = Brush.radialGradient(listOf(Color(0x5500E5FF), Color(0x0000E5FF)), center = center, radius = r * 1.45f),
    radius = r * 1.45f,
    center = center,
  )
  val side = (r * 2.0f).roundToInt().coerceAtLeast(32)
  drawImage(
    image = planet,
    dstOffset = IntOffset(center.x.roundToInt() - side / 2, center.y.roundToInt() - side / 2),
    dstSize = IntSize(side, side),
    alpha = 0.90f,
  )
}

private fun DrawScope.drawHorizonSpiresSprite(w: Float, h: Float, spire: ImageBitmap) {
  val placements = listOf(0.08f to 0.09f, 0.42f to 0.105f, 0.56f to 0.085f)
  val baseY = h * 0.145f
  for ((sx, sizeFrac) in placements) {
    val side = (w * sizeFrac * 1.35f).roundToInt().coerceAtLeast(24)
    val x = (sx * w).roundToInt()
    drawImage(
      image = spire,
      dstOffset = IntOffset(x - side / 2, (baseY - side * 0.85f).roundToInt()),
      dstSize = IntSize(side, side),
      alpha = 0.78f,
    )
  }
}

private fun DrawScope.drawSerpentineRoad(w: Float, h: Float, time: Float) {
  val path = Path()
  CanyonMap.roadPoints.forEachIndexed { idx, pt ->
    val px = pt.x * w
    val py = pt.y * h
    if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
  }
  val roadW = w * 0.088f
  drawPath(path, Color(0xFF050811), style = Stroke(width = roadW + 14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
  drawPath(path, RoadMetalCurb, style = Stroke(width = roadW + 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
  drawPath(path, RoadSurface, style = Stroke(width = roadW, cap = StrokeCap.Round, join = StrokeJoin.Round))
  drawPath(
    path,
    NeonCyan.copy(alpha = 0.24f),
    style = Stroke(width = roadW - 4f, cap = StrokeCap.Round, join = StrokeJoin.Round),
  )
  drawPath(path, RoadSurface, style = Stroke(width = roadW - 8f, cap = StrokeCap.Round, join = StrokeJoin.Round))

  val dashOffset = -(time * 42f) % 36f
  drawPath(
    path = path,
    color = NeonCyan.copy(alpha = 0.45f),
    style =
      Stroke(
        width = 2.5f,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 20f), phase = dashOffset),
      ),
  )
}

private fun DrawScope.drawEnemyPortalSprite(center: Offset, radius: Float, time: Float, portal: ImageBitmap) {
  val pulse = 0.72f + 0.28f * sin(time * 4.5f)
  drawCircle(
    brush = Brush.radialGradient(listOf(EnemyCrimson.copy(alpha = 0.55f * pulse), Color.Transparent), center = center, radius = radius * 1.3f),
    radius = radius * 1.3f,
    center = center,
  )
  val side = (radius * 2.15f).roundToInt().coerceAtLeast(32)
  drawImage(
    image = portal,
    dstOffset = IntOffset(center.x.roundToInt() - side / 2, center.y.roundToInt() - (side * 0.58f).roundToInt()),
    dstSize = IntSize(side, side),
    colorFilter = ColorFilter.tint(EnemyCrimson.copy(alpha = 0.45f), androidx.compose.ui.graphics.BlendMode.SrcAtop),
  )
}

private fun DrawScope.drawBaseEnergyGateSprite(center: Offset, radius: Float, time: Float, baseGate: ImageBitmap) {
  val pulse = 0.72f + 0.28f * sin(time * 4f)
  drawCircle(
    brush = Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.45f * pulse), Color.Transparent), center = center, radius = radius * 1.35f),
    radius = radius * 1.35f,
    center = center,
  )
  // Guiding blue chevrons pointing into the energy gate
  for (i in 0..1) {
    val cy = center.y - radius * (0.68f + i * 0.26f) + ((time * 22f) % 14f)
    val chev = Path().apply {
      moveTo(center.x - radius * 0.25f, cy - 5f)
      lineTo(center.x, cy + 5f)
      lineTo(center.x + radius * 0.25f, cy - 5f)
    }
    drawPath(chev, NeonCyan.copy(alpha = 0.70f), style = Stroke(width = 2.6f, cap = StrokeCap.Round))
  }
  val side = (radius * 2.15f).roundToInt().coerceAtLeast(32)
  drawImage(
    image = baseGate,
    dstOffset = IntOffset(center.x.roundToInt() - side / 2, center.y.roundToInt() - (side * 0.55f).roundToInt()),
    dstSize = IntSize(side, side),
  )
}

private fun DrawScope.drawJaggedArc(
  start: Offset,
  end: Offset,
  color: Color,
  thickness: Float,
  seed: Int,
) {
  val r = Random(seed)
  val path = Path().apply { moveTo(start.x, start.y) }
  val steps = 5
  for (i in 1 until steps) {
    val t = i.toFloat() / steps
    val jitter = (r.nextFloat() - 0.5f) * 26f
    val px = start.x + (end.x - start.x) * t + jitter
    val py = start.y + (end.y - start.y) * t + (r.nextFloat() - 0.5f) * 20f
    path.lineTo(px, py)
  }
  path.lineTo(end.x, end.y)
  drawPath(path, color.copy(alpha = color.alpha * 0.35f), style = Stroke(width = thickness * 2.6f, cap = StrokeCap.Round))
  drawPath(path, color, style = Stroke(width = thickness, cap = StrokeCap.Round))
  drawPath(path, Color.White.copy(alpha = color.alpha * 0.85f), style = Stroke(width = (thickness * 0.4f).coerceAtLeast(1f)))
}

private fun DrawScope.drawBeautifulLaserBeam(
  start: Offset,
  end: Offset,
  baseColor: Color,
  intensity: Float,
  rampFraction: Float,
  towerLevel: Int,
  time: Float,
  viewportWidth: Float,
  reusablePath: Path,
) {
  val dx = end.x - start.x
  val dy = end.y - start.y
  val len = hypot(dx, dy)
  if (len < 1f) return

  val ux = dx / len
  val uy = dy / len
  val nx = -uy
  val ny = ux

  val pxScale = (viewportWidth / 1080f).coerceIn(0.65f, 1.55f)
  val levelScale = 1f + (towerLevel - 1) * 0.22f
  val shimmer = 0.90f + 0.10f * sin(time * 38f + start.x * 0.05f)
  val beamScale = pxScale * levelScale * (0.88f + rampFraction * 0.42f) * shimmer

  // 1. Wide atmospheric plasma aura
  drawLine(
    brush =
      Brush.linearGradient(
        colors =
          listOf(
            baseColor.copy(alpha = 0.26f),
            Color(0xFF2979FF).copy(alpha = 0.18f),
            baseColor.copy(alpha = 0.30f),
          ),
        start = start,
        end = end,
      ),
    start = start,
    end = end,
    strokeWidth = 20f * beamScale * intensity,
    cap = StrokeCap.Round,
  )

  // 2. Electric blue energy sheath
  drawLine(
    color = Color(0xFF00B0FF).copy(alpha = 0.56f),
    start = start,
    end = end,
    strokeWidth = 10.5f * beamScale * intensity,
    cap = StrokeCap.Round,
  )

  // 3. Vibrant neon cyan inner beam
  drawLine(
    color = baseColor.copy(alpha = 0.94f),
    start = start,
    end = end,
    strokeWidth = 5.2f * beamScale,
    cap = StrokeCap.Round,
  )

  // 4. Incandescent pure white plasma filament
  drawLine(
    color = Color.White,
    start = start,
    end = end,
    strokeWidth = (2.1f + rampFraction * 1.4f) * pxScale * levelScale,
    cap = StrokeCap.Round,
  )

  // 5. Animated helical plasma arc weaving along the core
  reusablePath.reset()
  val segments = 14
  val waveAmp = (4.2f + rampFraction * 5.2f) * pxScale * levelScale
  for (i in 0..segments) {
    val frac = i.toFloat() / segments
    val env = sin(frac * PI.toFloat())
    val wave = sin(frac * 15f - time * 28f) * waveAmp * env
    val px = start.x + dx * frac + nx * wave
    val py = start.y + dy * frac + ny * wave
    if (i == 0) reusablePath.moveTo(px, py) else reusablePath.lineTo(px, py)
  }
  drawPath(
    path = reusablePath,
    color = Color(0xFFB2F5FF).copy(alpha = 0.72f + 0.25f * rampFraction),
    style = Stroke(width = 1.6f * pxScale, cap = StrokeCap.Round, join = StrokeJoin.Round),
  )

  // 6. Fast traveling energy pulses rushing from muzzle to target
  for (pIdx in 0..2) {
    val phase = ((time * (3.2f + rampFraction * 1.8f) + pIdx * 0.333f) % 1f)
    val pulsePos = Offset(start.x + dx * phase, start.y + dy * phase)
    val pulseR = (4.0f + rampFraction * 2.8f) * pxScale * levelScale
    drawCircle(
      color = baseColor.copy(alpha = 0.55f),
      radius = pulseR * 1.75f,
      center = pulsePos,
    )
    drawCircle(
      color = Color.White.copy(alpha = 0.92f),
      radius = pulseR * 0.75f,
      center = pulsePos,
    )
  }

  // 7. Muzzle energy aperture flare at beam origin
  val muzzleR = (15f + rampFraction * 6f) * pxScale * levelScale
  drawCircle(
    brush =
      Brush.radialGradient(
        colors = listOf(Color.White.copy(alpha = 0.90f), baseColor.copy(alpha = 0.50f), Color.Transparent),
        center = start,
        radius = muzzleR,
      ),
    radius = muzzleR,
    center = start,
  )
  val muzzleCross = muzzleR * 0.78f
  drawLine(
    color = Color.White.copy(alpha = 0.85f),
    start = Offset(start.x - nx * muzzleCross, start.y - ny * muzzleCross),
    end = Offset(start.x + nx * muzzleCross, start.y + ny * muzzleCross),
    strokeWidth = 1.8f * pxScale,
    cap = StrokeCap.Round,
  )

  // 8. Thermal impact scorch glow + rotating plasma star-spikes at target
  val impactR = (17f + rampFraction * 13f) * pxScale * levelScale * shimmer
  val scorchColor = if (rampFraction > 0.35f) Color(0xFFFFA726).copy(alpha = 0.45f * rampFraction) else Color(0xFF2979FF).copy(alpha = 0.35f)
  drawCircle(
    brush =
      Brush.radialGradient(
        colors = listOf(Color.White, baseColor.copy(alpha = 0.80f), scorchColor, Color.Transparent),
        center = end,
        radius = impactR,
      ),
    radius = impactR,
    center = end,
  )
  val spikeLen = impactR * 0.72f
  val spin = time * 6.5f
  for (s in 0..1) {
    val ang = spin + s * (PI.toFloat() * 0.5f)
    val sx = cos(ang) * spikeLen
    val sy = sin(ang) * spikeLen
    drawLine(
      color = Color.White.copy(alpha = 0.88f),
      start = Offset(end.x - sx, end.y - sy),
      end = Offset(end.x + sx, end.y + sy),
      strokeWidth = 2.0f * pxScale,
      cap = StrokeCap.Round,
    )
  }
}
