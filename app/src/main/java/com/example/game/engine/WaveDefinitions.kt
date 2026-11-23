package com.example.game.engine

import com.example.game.model.EnemyType

data class SpawnGroup(
  val enemyType: EnemyType,
  val count: Int,
  val intervalSeconds: Float,
  val hpMultiplier: Float = 1f,
)

data class WaveSpec(
  val waveNumber: Int,
  val title: String,
  val groups: List<SpawnGroup>,
  val clearGoldBonus: Int,
  val clearCrystalBonus: Int,
)

object WaveDefinitions {
  val waves: List<WaveSpec> =
    listOf(
      WaveSpec(
        waveNumber = 1,
        title = "Разведка дронов",
        groups = listOf(SpawnGroup(EnemyType.SCOUT_DRONE, 8, 0.95f, 0.95f)),
        clearGoldBonus = 45,
        clearCrystalBonus = 15,
      ),
      WaveSpec(
        waveNumber = 2,
        title = "Атака пауков",
        groups =
          listOf(
            SpawnGroup(EnemyType.SCOUT_DRONE, 6, 0.85f, 1.0f),
            SpawnGroup(EnemyType.CYBER_SPIDER, 5, 1.05f, 1.0f),
          ),
        clearGoldBonus = 55,
        clearCrystalBonus = 18,
      ),
      WaveSpec(
        waveNumber = 3,
        title = "Стая каньона",
        groups =
          listOf(
            SpawnGroup(EnemyType.CYBER_SPIDER, 9, 0.80f, 1.08f),
            SpawnGroup(EnemyType.SCOUT_DRONE, 6, 0.65f, 1.1f),
          ),
        clearGoldBonus = 65,
        clearCrystalBonus = 20,
      ),
      WaveSpec(
        waveNumber = 4,
        title = "Первые шагоходы",
        groups =
          listOf(
            SpawnGroup(EnemyType.SCOUT_DRONE, 6, 0.70f, 1.15f),
            SpawnGroup(EnemyType.WALKER, 4, 1.45f, 1.0f),
          ),
        clearGoldBonus = 75,
        clearCrystalBonus = 22,
      ),
      WaveSpec(
        waveNumber = 5,
        title = "Воздушный налёт",
        groups =
          listOf(
            SpawnGroup(EnemyType.CYBER_SPIDER, 7, 0.75f, 1.2f),
            SpawnGroup(EnemyType.FLYING_DRONE, 5, 1.10f, 1.0f),
          ),
        clearGoldBonus = 85,
        clearCrystalBonus = 25,
      ),
      WaveSpec(
        waveNumber = 6,
        title = "Стальной кулак",
        groups =
          listOf(
            SpawnGroup(EnemyType.WALKER, 6, 1.25f, 1.15f),
            SpawnGroup(EnemyType.TANK_BOT, 2, 2.20f, 1.0f),
          ),
        clearGoldBonus = 95,
        clearCrystalBonus = 28,
      ),
      WaveSpec(
        waveNumber = 7,
        title = "Крылья и клыки",
        groups =
          listOf(
            SpawnGroup(EnemyType.FLYING_DRONE, 8, 0.90f, 1.18f),
            SpawnGroup(EnemyType.CYBER_SPIDER, 10, 0.60f, 1.25f),
            SpawnGroup(EnemyType.WALKER, 4, 1.20f, 1.25f),
          ),
        clearGoldBonus = 105,
        clearCrystalBonus = 30,
      ),
      WaveSpec(
        waveNumber = 8,
        title = "Авангард Титана",
        groups =
          listOf(
            SpawnGroup(EnemyType.TANK_BOT, 4, 1.90f, 1.18f),
            SpawnGroup(EnemyType.ELITE_BOSS, 1, 2.50f, 0.75f),
            SpawnGroup(EnemyType.SCOUT_DRONE, 8, 0.55f, 1.35f),
          ),
        clearGoldBonus = 135,
        clearCrystalBonus = 40,
      ),
      WaveSpec(
        waveNumber = 9,
        title = "Небесный штурм",
        groups =
          listOf(
            SpawnGroup(EnemyType.FLYING_DRONE, 11, 0.75f, 1.35f),
            SpawnGroup(EnemyType.WALKER, 7, 1.00f, 1.40f),
          ),
        clearGoldBonus = 120,
        clearCrystalBonus = 32,
      ),
      WaveSpec(
        waveNumber = 10,
        title = "Тяжёлая колонна",
        groups =
          listOf(
            SpawnGroup(EnemyType.TANK_BOT, 6, 1.60f, 1.35f),
            SpawnGroup(EnemyType.CYBER_SPIDER, 12, 0.55f, 1.50f),
            SpawnGroup(EnemyType.FLYING_DRONE, 6, 0.85f, 1.45f),
          ),
        clearGoldBonus = 140,
        clearCrystalBonus = 35,
      ),
      WaveSpec(
        waveNumber = 11,
        title = "Прорыв роя",
        groups =
          listOf(
            SpawnGroup(EnemyType.SCOUT_DRONE, 16, 0.42f, 1.65f),
            SpawnGroup(EnemyType.CYBER_SPIDER, 12, 0.50f, 1.65f),
            SpawnGroup(EnemyType.WALKER, 6, 0.95f, 1.60f),
          ),
        clearGoldBonus = 150,
        clearCrystalBonus = 38,
      ),
      WaveSpec(
        waveNumber = 12,
        title = "Штурм Джаггернаута",
        groups =
          listOf(
            SpawnGroup(EnemyType.TANK_BOT, 5, 1.50f, 1.55f),
            SpawnGroup(EnemyType.ELITE_BOSS, 1, 2.50f, 1.05f),
            SpawnGroup(EnemyType.FLYING_DRONE, 8, 0.80f, 1.60f),
          ),
        clearGoldBonus = 180,
        clearCrystalBonus = 48,
      ),
      WaveSpec(
        waveNumber = 13,
        title = "Огненный вал",
        groups =
          listOf(
            SpawnGroup(EnemyType.WALKER, 10, 0.85f, 1.80f),
            SpawnGroup(EnemyType.TANK_BOT, 6, 1.35f, 1.75f),
            SpawnGroup(EnemyType.FLYING_DRONE, 10, 0.70f, 1.75f),
          ),
        clearGoldBonus = 185,
        clearCrystalBonus = 45,
      ),
      WaveSpec(
        waveNumber = 14,
        title = "Кибер-легион",
        groups =
          listOf(
            SpawnGroup(EnemyType.CYBER_SPIDER, 16, 0.45f, 1.95f),
            SpawnGroup(EnemyType.TANK_BOT, 8, 1.25f, 1.90f),
            SpawnGroup(EnemyType.FLYING_DRONE, 12, 0.65f, 1.90f),
          ),
        clearGoldBonus = 210,
        clearCrystalBonus = 50,
      ),
      WaveSpec(
        waveNumber = 15,
        title = "Финал: Повелитель Бездны",
        groups =
          listOf(
            SpawnGroup(EnemyType.WALKER, 8, 0.90f, 2.10f),
            SpawnGroup(EnemyType.TANK_BOT, 6, 1.30f, 2.05f),
            SpawnGroup(EnemyType.ELITE_BOSS, 2, 4.50f, 1.35f),
            SpawnGroup(EnemyType.FLYING_DRONE, 12, 0.65f, 2.10f),
          ),
        clearGoldBonus = 300,
        clearCrystalBonus = 100,
      ),
    )
}
