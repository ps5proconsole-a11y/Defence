package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BossGoldRed
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.CrystalViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EnemyCrimson
import com.example.ui.theme.EnemyEmber
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.HudEmerald
import com.example.ui.theme.IceBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ShieldDomeBlue

enum class TowerType(
  val title: String,
  val shortName: String,
  val roleTag: String,
  val description: String,
  val baseCost: Int,
  val baseRange: Float, // normalized map radius
  val baseDamage: Float,
  val baseFireRate: Float, // shots per second (or ticks)
  val splashRadius: Float,
  val slowFactor: Float,
  val chainCount: Int,
  val empDuration: Float,
  val shieldCapacity: Int,
  val hitsAir: Boolean,
  val accentColor: Color,
) {
  CANNON(
    title = "Тяжёлая пушка",
    shortName = "Пушка",
    roleTag = "Одиночный урон",
    description = "Мощный кинетический выстрел с оранжевой вспышкой. Эффективна против брони.",
    baseCost = 100,
    baseRange = 0.24f,
    baseDamage = 48f,
    baseFireRate = 1.05f,
    splashRadius = 0f,
    slowFactor = 0f,
    chainCount = 0,
    empDuration = 0f,
    shieldCapacity = 0,
    hitsAir = true,
    accentColor = EnemyEmber,
  ),
  GATLING(
    title = "Пулемёт Гатлинга",
    shortName = "Гатлинг",
    roleTag = "Скорострельность",
    description = "Роторный многоствольный пулемёт, сметающий быстрых дронов и пауков.",
    baseCost = 110,
    baseRange = 0.23f,
    baseDamage = 14f,
    baseFireRate = 4.6f,
    splashRadius = 0f,
    slowFactor = 0f,
    chainCount = 0,
    empDuration = 0f,
    shieldCapacity = 0,
    hitsAir = true,
    accentColor = GoldAmber,
  ),
  LASER(
    title = "Лазерная турель",
    shortName = "Лазер",
    roleTag = "Непрерывный луч",
    description = "Фокусирует непрерывный голубой луч с нарастающим тепловым уроном.",
    baseCost = 120,
    baseRange = 0.25f,
    baseDamage = 42f, // DPS base, ramps up on lock
    baseFireRate = 10f,
    splashRadius = 0f,
    slowFactor = 0f,
    chainCount = 0,
    empDuration = 0f,
    shieldCapacity = 0,
    hitsAir = true,
    accentColor = NeonCyan,
  ),
  MISSILE(
    title = "Ракетная установка",
    shortName = "Ракеты",
    roleTag = "Дальний AoE",
    description = "Запускает самонаводящиеся красные ракеты с осколочным взрывом.",
    baseCost = 150,
    baseRange = 0.34f,
    baseDamage = 62f,
    baseFireRate = 0.68f,
    splashRadius = 0.095f,
    slowFactor = 0f,
    chainCount = 0,
    empDuration = 0f,
    shieldCapacity = 0,
    hitsAir = true,
    accentColor = EnemyCrimson,
  ),
  MORTAR(
    title = "Арт-мортира",
    shortName = "Мортира",
    roleTag = "Навесной взрыв",
    description = "Навесной огонь по скоплениям наземных врагов с мощной ударной волной.",
    baseCost = 160,
    baseRange = 0.33f,
    baseDamage = 92f,
    baseFireRate = 0.50f,
    splashRadius = 0.115f,
    slowFactor = 0f,
    chainCount = 0,
    empDuration = 0f,
    shieldCapacity = 0,
    hitsAir = false,
    accentColor = Color(0xFFFF9100),
  ),
  TESLA(
    title = "Тесла-катушка",
    shortName = "Тесла",
    roleTag = "Цепная молния",
    description = "Высоковольтный разряд перепрыгивает сразу на несколько ближайших целей.",
    baseCost = 180,
    baseRange = 0.24f,
    baseDamage = 36f,
    baseFireRate = 1.25f,
    splashRadius = 0f,
    slowFactor = 0f,
    chainCount = 3,
    empDuration = 0f,
    shieldCapacity = 0,
    hitsAir = true,
    accentColor = CrystalCyan,
  ),
  EMP(
    title = "ЭМИ-шпиль",
    shortName = "ЭМИ",
    roleTag = "Снятие щитов",
    description = "Электрический импульс отключает щиты, рывки и призыв дронов у врагов.",
    baseCost = 190,
    baseRange = 0.25f,
    baseDamage = 26f,
    baseFireRate = 0.85f,
    splashRadius = 0.13f,
    slowFactor = 0.18f,
    chainCount = 0,
    empDuration = 3.5f,
    shieldCapacity = 0,
    hitsAir = true,
    accentColor = CrystalViolet,
  ),
  FREEZE(
    title = "Крио-излучатель",
    shortName = "Крио",
    roleTag = "Ледяная аура",
    description = "Кристаллическое поле сильно замедляет всех врагов в радиусе действия.",
    baseCost = 200,
    baseRange = 0.235f,
    baseDamage = 12f, // gentle frost pulse DPS
    baseFireRate = 1.5f,
    splashRadius = 0.235f,
    slowFactor = 0.45f,
    chainCount = 0,
    empDuration = 0f,
    shieldCapacity = 0,
    hitsAir = true,
    accentColor = IceBlue,
  ),
  SHIELD(
    title = "Щит-генератор",
    shortName = "Щит",
    roleTag = "Барьер дороги",
    description = "Гексагональный купол удерживает наземных врагов на трассе под огнём.",
    baseCost = 200,
    baseRange = 0.21f,
    baseDamage = 10f,
    baseFireRate = 1.0f,
    splashRadius = 0.088f,
    slowFactor = 0.80f,
    chainCount = 0,
    empDuration = 0f,
    shieldCapacity = 3,
    hitsAir = false,
    accentColor = ShieldDomeBlue,
  );

  fun upgradeCost(currentLevel: Int): Int {
    return when (currentLevel) {
      1 -> ((baseCost * 0.75f) / 5f).toInt() * 5
      2 -> ((baseCost * 1.20f) / 5f).toInt() * 5
      else -> 0
    }
  }
}

enum class EnemyType(
  val title: String,
  val baseHp: Float,
  val speed: Float, // normalized path units per second
  val armor: Float,
  val baseShield: Float,
  val goldReward: Int,
  val crystalReward: Int,
  val baseDamage: Int,
  val isFlying: Boolean,
  val radius: Float,
  val abilityDescription: String,
  val accentColor: Color,
) {
  SCOUT_DRONE(
    title = "Дрон-разведчик",
    baseHp = 58f,
    speed = 0.115f,
    armor = 0f,
    baseShield = 0f,
    goldReward = 14,
    crystalReward = 1,
    baseDamage = 4,
    isFlying = false,
    radius = 0.017f,
    abilityDescription = "Высокая скорость, атакует числом.",
    accentColor = EnemyEmber,
  ),
  CYBER_SPIDER(
    title = "Кибер-паук",
    baseHp = 115f,
    speed = 0.092f,
    armor = 2f,
    baseShield = 0f,
    goldReward = 20,
    crystalReward = 2,
    baseDamage = 6,
    isFlying = false,
    radius = 0.020f,
    abilityDescription = "Шестиногий штурмовик со стабильным темпом.",
    accentColor = EnemyCrimson,
  ),
  WALKER(
    title = "Боевой шагоход",
    baseHp = 230f,
    speed = 0.066f,
    armor = 8f,
    baseShield = 45f,
    goldReward = 30,
    crystalReward = 3,
    baseDamage = 10,
    isFlying = false,
    radius = 0.023f,
    abilityDescription = "Усиленная броня и энергощит; уязвим к ЭМИ.",
    accentColor = Color(0xFFFF4D6D),
  ),
  TANK_BOT(
    title = "Тяжёлый танк",
    baseHp = 520f,
    speed = 0.044f,
    armor = 16f,
    baseShield = 110f,
    goldReward = 48,
    crystalReward = 5,
    baseDamage = 15,
    isFlying = false,
    radius = 0.028f,
    abilityDescription = "Сверхтяжёлая броня и щит. Медленный, но живучий.",
    accentColor = Color(0xFFFF7A00),
  ),
  FLYING_DRONE(
    title = "Реактивный истребитель",
    baseHp = 135f,
    speed = 0.112f,
    armor = 2f,
    baseShield = 25f,
    goldReward = 26,
    crystalReward = 3,
    baseDamage = 8,
    isFlying = true,
    radius = 0.021f,
    abilityDescription = "Летит над ущельем, игнорирует купол Щита и мортиру.",
    accentColor = Color(0xFFFF3366),
  ),
  ELITE_BOSS(
    title = "Титан-Джаггернаут",
    baseHp = 2100f,
    speed = 0.036f,
    armor = 22f,
    baseShield = 520f,
    goldReward = 180,
    crystalReward = 30,
    baseDamage = 35,
    isFlying = false,
    radius = 0.037f,
    abilityDescription = "Элитный босс: регенерирует щит, совершает рывки и призывает дронов.",
    accentColor = BossGoldRed,
  ),
}

enum class AbilityType(
  val title: String,
  val shortName: String,
  val crystalCost: Int,
  val cooldownSeconds: Float,
  val isTargeted: Boolean,
  val description: String,
  val accentColor: Color,
) {
  LIGHTNING_STRIKE(
    title = "Удар молнии",
    shortName = "Молния",
    crystalCost = 50,
    cooldownSeconds = 10f,
    isTargeted = true,
    description = "Точечный разряд с орбиты: огромный урон и оглушение в точке.",
    accentColor = NeonCyan,
  ),
  CRYO_FREEZE(
    title = "Крио-волна",
    shortName = "Заморозка",
    crystalCost = 75,
    cooldownSeconds = 16f,
    isTargeted = false,
    description = "Глобальное замораживание всех врагов на карте на 5 сек.",
    accentColor = IceBlue,
  ),
  MISSILE_BARRAGE(
    title = "Ракетный залп",
    shortName = "Залп",
    crystalCost = 100,
    cooldownSeconds = 18f,
    isTargeted = true,
    description = "Ковровый удар из 7 боевых ракет по выбранной зоне.",
    accentColor = EnemyEmber,
  ),
  ORBITAL_OVERDRIVE(
    title = "Орбитальный луч",
    shortName = "Орбита",
    crystalCost = 125,
    cooldownSeconds = 24f,
    isTargeted = false,
    description = "Аннигилирует сильнейшую цель и ускоряет все башни на 65% на 6 сек.",
    accentColor = GoldAmber,
  ),
}

enum class TargetPriority(val label: String) {
  FIRST("Первый"),
  STRONGEST("Сильный"),
  CLOSEST("Ближний"),
}

enum class GamePhase {
  MENU,
  PLAYING,
  PAUSED,
  VICTORY,
  DEFEAT,
}

enum class ProjectileKind {
  CANNON_SHELL,
  GATLING_TRACER,
  MISSILE,
  MORTAR_BOMB,
}

data class MapSlot(
  val id: Int,
  val x: Float,
  val y: Float,
  val roadAnchorX: Float,
  val roadAnchorY: Float,
)

class TowerInstance(
  val slotId: Int,
  val x: Float,
  val y: Float,
  val roadAnchorX: Float,
  val roadAnchorY: Float,
  val type: TowerType,
  var level: Int = 1,
  var totalInvested: Int = type.baseCost,
  var angleRad: Float = -Math.PI.toFloat() / 2f,
  var cooldownTimer: Float = 0f,
  var recoil: Float = 0f,
  var laserTargetId: Int = -1,
  var laserRamp: Float = 0f,
  var shieldEnergy: Float = 100f,
  var shieldMaxEnergy: Float = 100f,
  var shieldOverloadTimer: Float = 0f,
  var targetPriority: TargetPriority = TargetPriority.FIRST,
  var kills: Int = 0,
  var angularVelocity: Float = 0f,
  var targetLocked: Boolean = false,
) {
  fun effectiveRange(): Float {
    val mult =
      when (level) {
        1 -> 1.0f
        2 -> 1.12f
        else -> 1.24f
      }
    return type.baseRange * mult
  }

  fun effectiveDamage(): Float {
    val mult =
      when (level) {
        1 -> 1.0f
        2 -> 1.55f
        else -> 2.35f
      }
    return type.baseDamage * mult
  }

  fun effectiveFireRate(overdriveActive: Boolean): Float {
    val lvlMult =
      when (level) {
        1 -> 1.0f
        2 -> 1.18f
        else -> 1.38f
      }
    val boost = if (overdriveActive) 1.65f else 1.0f
    return type.baseFireRate * lvlMult * boost
  }

  fun effectiveSlow(): Float {
    val add = (level - 1) * 0.08f
    return (type.slowFactor + add).coerceAtMost(0.85f)
  }

  fun effectiveChainCount(): Int = type.chainCount + (level - 1)

  fun effectiveShieldCapacity(): Int = type.shieldCapacity + (level - 1) * 2

  fun sellRefund(): Int = ((totalInvested * 0.70f) / 5f).toInt() * 5
}

class EnemyEntity {
  var active: Boolean = false
  var id: Int = 0
  var type: EnemyType = EnemyType.SCOUT_DRONE
  var x: Float = 0f
  var y: Float = 0f
  var prevX: Float = 0f
  var prevY: Float = 0f
  var angleRad: Float = 0f
  var distanceAlongPath: Float = 0f
  var totalPathLength: Float = 1f
  var hp: Float = 0f
  var maxHp: Float = 1f
  var shield: Float = 0f
  var maxShield: Float = 0f
  var slowTimer: Float = 0f
  var slowStrength: Float = 0f
  var empTimer: Float = 0f
  var stunTimer: Float = 0f
  var shieldBlockTimer: Float = 0f
  var hitFlashTimer: Float = 0f
  var animPhase: Float = 0f
  var bossAbilityTimer: Float = 5f
  var dashTimer: Float = 0f
  var laneOffset: Float = 0f
  var currentSpeed: Float = 0f
  var targetAngleRad: Float = 0f

  fun progress(): Float = (distanceAlongPath / totalPathLength).coerceIn(0f, 1f)
}

class ProjectileEntity {
  var active: Boolean = false
  var kind: ProjectileKind = ProjectileKind.CANNON_SHELL
  var sourceSlotId: Int = -1
  var startX: Float = 0f
  var startY: Float = 0f
  var x: Float = 0f
  var y: Float = 0f
  var targetX: Float = 0f
  var targetY: Float = 0f
  var targetEnemyId: Int = -1
  var speed: Float = 0.6f
  var progress: Float = 0f
  var flightDuration: Float = 0.5f
  var damage: Float = 10f
  var splashRadius: Float = 0f
  var color: Color = NeonCyan
  var angleRad: Float = 0f
  var smokeTimer: Float = 0f
}

class ParticleEntity {
  var active: Boolean = false
  var x: Float = 0f
  var y: Float = 0f
  var vx: Float = 0f
  var vy: Float = 0f
  var radius: Float = 3f
  var life: Float = 0f
  var maxLife: Float = 0.4f
  var color: Color = EnemyEmber
  var glow: Boolean = true
}

class ShockwaveEntity {
  var active: Boolean = false
  var x: Float = 0f
  var y: Float = 0f
  var maxRadius: Float = 0.08f
  var life: Float = 0f
  var maxLife: Float = 0.35f
  var color: Color = EnemyEmber
}

class LightningArcEntity {
  var active: Boolean = false
  var x1: Float = 0f
  var y1: Float = 0f
  var x2: Float = 0f
  var y2: Float = 0f
  var life: Float = 0f
  var maxLife: Float = 0.22f
  var color: Color = CrystalCyan
  var thickness: Float = 3f
  var seed: Int = 1
}

class LaserBeamEntity {
  var active: Boolean = false
  var x1: Float = 0f
  var y1: Float = 0f
  var x2: Float = 0f
  var y2: Float = 0f
  var color: Color = NeonCyan
  var intensity: Float = 1f
  var rampFraction: Float = 0f
  var towerLevel: Int = 1
  var targetRadius: Float = 0.02f
}

class FloatingTextEntity {
  var active: Boolean = false
  var x: Float = 0f
  var y: Float = 0f
  var text: String = ""
  var life: Float = 0f
  var maxLife: Float = 0.8f
  var color: Color = GoldAmber
}

data class HudUiState(
  val phase: GamePhase = GamePhase.MENU,
  val baseHp: Int = 100,
  val maxBaseHp: Int = 100,
  val gold: Int = 300,
  val crystals: Int = 120,
  val wave: Int = 0,
  val totalWaves: Int = 15,
  val waveInProgress: Boolean = false,
  val betweenWaveCountdown: Float = 0f,
  val speedMultiplier: Int = 1, // 1 or 2
  val selectedSlotId: Int? = null,
  val selectedTower: TowerSnapshot? = null,
  val activeAbilityTargeting: AbilityType? = null,
  val abilityCooldowns: Map<AbilityType, Float> = emptyMap(),
  val globalFreezeTimer: Float = 0f,
  val overdriveTimer: Float = 0f,
  val totalKills: Int = 0,
  val towersBuiltCount: Int = 0,
  val statusBanner: String? = null,
  val codexOpen: Boolean = false,
)

data class TowerSnapshot(
  val slotId: Int,
  val type: TowerType,
  val level: Int,
  val damage: Float,
  val range: Float,
  val fireRate: Float,
  val upgradeCost: Int,
  val sellRefund: Int,
  val targetPriority: TargetPriority,
  val kills: Int,
  val x: Float,
  val y: Float,
)

val HudEmeraldColor = HudEmerald
val ElectricBlueColor = ElectricBlue
