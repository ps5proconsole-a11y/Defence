package com.example.game.map

import androidx.compose.ui.geometry.Offset
import com.example.game.model.MapSlot
import kotlin.math.hypot

data class CrystalCluster(val x: Float, val y: Float, val scale: Float, val phase: Float)

data class AlienTree(val x: Float, val y: Float, val scale: Float)

data class RockFormation(val x: Float, val y: Float, val rx: Float, val ry: Float, val seed: Int)

object CanyonMap {
  // Raw corner vertices of the winding S-shaped canyon serpentine (normalized 0..1)
  private val rawWaypoints =
    listOf(
      Offset(0.20f, 0.145f), // Enemy Portal Gate (top-left canyon)
      Offset(0.20f, 0.235f),
      Offset(0.77f, 0.235f),
      Offset(0.77f, 0.420f),
      Offset(0.21f, 0.420f),
      Offset(0.21f, 0.605f),
      Offset(0.77f, 0.605f),
      Offset(0.77f, 0.745f),
      Offset(0.50f, 0.745f),
      Offset(0.50f, 0.848f), // Defended Energy Gate Base (bottom center)
    )

  // Smooth rounded corner polyline for natural serpentine curves
  val roadPoints: List<Offset> = buildSmoothedPath(rawWaypoints, cornerRadius = 0.055f, steps = 7)

  val segmentLengths: FloatArray
  val cumulativeDistances: FloatArray
  val totalPathLength: Float

  init {
    val count = roadPoints.size - 1
    segmentLengths = FloatArray(count)
    cumulativeDistances = FloatArray(roadPoints.size)
    var acc = 0f
    cumulativeDistances[0] = 0f
    for (i in 0 until count) {
      val a = roadPoints[i]
      val b = roadPoints[i + 1]
      val len = hypot(b.x - a.x, b.y - a.y)
      segmentLengths[i] = len
      acc += len
      cumulativeDistances[i + 1] = acc
    }
    totalPathLength = acc
  }

  val portalPosition: Offset = roadPoints.first()
  val baseGatePosition: Offset = roadPoints.last()

  // 11 tactical hexagonal platform slots along the bends of the serpentine
  val buildSlots: List<MapSlot> =
    listOf(
      MapSlot(id = 0, x = 0.48f, y = 0.165f, roadAnchorX = 0.48f, roadAnchorY = 0.235f),
      MapSlot(id = 1, x = 0.39f, y = 0.328f, roadAnchorX = 0.39f, roadAnchorY = 0.235f),
      MapSlot(id = 2, x = 0.59f, y = 0.328f, roadAnchorX = 0.59f, roadAnchorY = 0.420f),
      MapSlot(id = 3, x = 0.89f, y = 0.328f, roadAnchorX = 0.77f, roadAnchorY = 0.328f),
      MapSlot(id = 4, x = 0.10f, y = 0.512f, roadAnchorX = 0.21f, roadAnchorY = 0.512f),
      MapSlot(id = 5, x = 0.39f, y = 0.512f, roadAnchorX = 0.39f, roadAnchorY = 0.420f),
      MapSlot(id = 6, x = 0.59f, y = 0.512f, roadAnchorX = 0.59f, roadAnchorY = 0.605f),
      MapSlot(id = 7, x = 0.89f, y = 0.675f, roadAnchorX = 0.77f, roadAnchorY = 0.675f),
      MapSlot(id = 8, x = 0.31f, y = 0.695f, roadAnchorX = 0.45f, roadAnchorY = 0.605f),
      MapSlot(id = 9, x = 0.66f, y = 0.818f, roadAnchorX = 0.50f, roadAnchorY = 0.805f),
      MapSlot(id = 10, x = 0.34f, y = 0.818f, roadAnchorX = 0.50f, roadAnchorY = 0.805f),
    )

  // Glowing blue crystal clusters decorating canyon ledges
  val crystals: List<CrystalCluster> =
    listOf(
      CrystalCluster(0.08f, 0.18f, 1.0f, 0.2f),
      CrystalCluster(0.66f, 0.145f, 1.15f, 1.4f),
      CrystalCluster(0.91f, 0.21f, 0.9f, 2.3f),
      CrystalCluster(0.09f, 0.34f, 1.1f, 0.8f),
      CrystalCluster(0.49f, 0.328f, 0.75f, 3.1f),
      CrystalCluster(0.89f, 0.48f, 1.05f, 1.9f),
      CrystalCluster(0.11f, 0.67f, 1.1f, 2.7f),
      CrystalCluster(0.89f, 0.81f, 1.2f, 0.5f),
      CrystalCluster(0.14f, 0.83f, 0.95f, 3.8f),
    )

  // Dark alien pine / crystalline trees
  val trees: List<AlienTree> =
    listOf(
      AlienTree(0.07f, 0.26f, 0.9f),
      AlienTree(0.34f, 0.15f, 0.75f),
      AlienTree(0.88f, 0.15f, 0.85f),
      AlienTree(0.08f, 0.41f, 0.95f),
      AlienTree(0.92f, 0.41f, 0.8f),
      AlienTree(0.49f, 0.515f, 0.7f),
      AlienTree(0.10f, 0.75f, 1.0f),
      AlienTree(0.84f, 0.77f, 0.85f),
      AlienTree(0.20f, 0.84f, 0.8f),
      AlienTree(0.79f, 0.85f, 0.9f),
    )

  // Canyon cliff rock formations
  val rocks: List<RockFormation> =
    listOf(
      RockFormation(0.05f, 0.14f, 0.08f, 0.05f, 1),
      RockFormation(0.93f, 0.14f, 0.09f, 0.06f, 2),
      RockFormation(0.04f, 0.29f, 0.07f, 0.06f, 3),
      RockFormation(0.95f, 0.56f, 0.07f, 0.07f, 4),
      RockFormation(0.05f, 0.60f, 0.06f, 0.05f, 5),
      RockFormation(0.14f, 0.70f, 0.06f, 0.04f, 6),
      RockFormation(0.92f, 0.85f, 0.08f, 0.06f, 7),
      RockFormation(0.08f, 0.86f, 0.08f, 0.05f, 8),
    )

  fun samplePathPosition(distance: Float): Pair<Offset, Float> {
    val clamped = distance.coerceIn(0f, totalPathLength)
    for (i in 0 until segmentLengths.size) {
      val startDist = cumulativeDistances[i]
      val endDist = cumulativeDistances[i + 1]
      if (clamped <= endDist || i == segmentLengths.lastIndex) {
        val segLen = segmentLengths[i].coerceAtLeast(0.0001f)
        val t = ((clamped - startDist) / segLen).coerceIn(0f, 1f)
        val a = roadPoints[i]
        val b = roadPoints[i + 1]
        val pos = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
        val angle = kotlin.math.atan2(b.y - a.y, b.x - a.x)
        return pos to angle
      }
    }
    return portalPosition to 0f
  }

  private fun buildSmoothedPath(
    corners: List<Offset>,
    cornerRadius: Float,
    steps: Int,
  ): List<Offset> {
    if (corners.size < 3) return corners
    val result = mutableListOf<Offset>()
    result.add(corners.first())
    for (i in 1 until corners.lastIndex) {
      val prev = corners[i - 1]
      val curr = corners[i]
      val next = corners[i + 1]
      val d1 = hypot(curr.x - prev.x, curr.y - prev.y)
      val d2 = hypot(next.x - curr.x, next.y - curr.y)
      val r = minOf(cornerRadius, d1 * 0.42f, d2 * 0.42f)

      val entry =
        Offset(
          curr.x - (curr.x - prev.x) / d1 * r,
          curr.y - (curr.y - prev.y) / d1 * r,
        )
      val exit =
        Offset(
          curr.x + (next.x - curr.x) / d2 * r,
          curr.y + (next.y - curr.y) / d2 * r,
        )
      for (s in 0..steps) {
        val t = s.toFloat() / steps
        val u = 1f - t
        val px = u * u * entry.x + 2f * u * t * curr.x + t * t * exit.x
        val py = u * u * entry.y + 2f * u * t * curr.y + t * t * exit.y
        result.add(Offset(px, py))
      }
    }
    result.add(corners.last())
    return result
  }
}
