package com.example.game.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.example.R
import com.example.game.model.AbilityType
import com.example.game.model.EnemyType
import com.example.game.model.TowerType

@Immutable
data class GameSprites(
  val towers: Map<TowerType, ImageBitmap>,
  val enemies: Map<EnemyType, ImageBitmap>,
  val abilities: Map<AbilityType, ImageBitmap>,
  val pad: ImageBitmap,
  val portal: ImageBitmap,
  val baseGate: ImageBitmap,
  val rocks: ImageBitmap,
  val tree: ImageBitmap,
  val crystals: ImageBitmap,
  val spire: ImageBitmap,
  val planet: ImageBitmap,
)

@Composable
fun rememberGameSprites(): GameSprites {
  val cannon = ImageBitmap.imageResource(R.drawable.sprite_tower_cannon)
  val gatling = ImageBitmap.imageResource(R.drawable.sprite_tower_gatling)
  val laser = ImageBitmap.imageResource(R.drawable.sprite_tower_laser)
  val missile = ImageBitmap.imageResource(R.drawable.sprite_tower_missile)
  val mortar = ImageBitmap.imageResource(R.drawable.sprite_tower_mortar)
  val freeze = ImageBitmap.imageResource(R.drawable.sprite_tower_freeze)
  val tesla = ImageBitmap.imageResource(R.drawable.sprite_tower_tesla)
  val emp = ImageBitmap.imageResource(R.drawable.sprite_tower_emp)
  val shield = ImageBitmap.imageResource(R.drawable.sprite_tower_shield)

  val scout = ImageBitmap.imageResource(R.drawable.sprite_enemy_scout)
  val spider = ImageBitmap.imageResource(R.drawable.sprite_enemy_spider)
  val walker = ImageBitmap.imageResource(R.drawable.sprite_enemy_walker)
  val tank = ImageBitmap.imageResource(R.drawable.sprite_enemy_tank)
  val flying = ImageBitmap.imageResource(R.drawable.sprite_enemy_flying)
  val boss = ImageBitmap.imageResource(R.drawable.sprite_enemy_boss)

  val abLightning = ImageBitmap.imageResource(R.drawable.sprite_ability_lightning)
  val abFreeze = ImageBitmap.imageResource(R.drawable.sprite_ability_freeze)
  val abMissile = ImageBitmap.imageResource(R.drawable.sprite_ability_missile)
  val abOrbital = ImageBitmap.imageResource(R.drawable.sprite_ability_orbital)

  val pad = ImageBitmap.imageResource(R.drawable.sprite_env_pad)
  val portal = ImageBitmap.imageResource(R.drawable.sprite_env_portal)
  val baseGate = ImageBitmap.imageResource(R.drawable.sprite_env_base)
  val rocks = ImageBitmap.imageResource(R.drawable.sprite_env_rocks)
  val tree = ImageBitmap.imageResource(R.drawable.sprite_env_tree)
  val crystals = ImageBitmap.imageResource(R.drawable.sprite_env_crystals)
  val spire = ImageBitmap.imageResource(R.drawable.sprite_env_spire)
  val planet = ImageBitmap.imageResource(R.drawable.sprite_env_planet)

  return remember(
    cannon,
    gatling,
    laser,
    missile,
    mortar,
    freeze,
    tesla,
    emp,
    shield,
    scout,
    spider,
    walker,
    tank,
    flying,
    boss,
    abLightning,
    abFreeze,
    abMissile,
    abOrbital,
    pad,
    portal,
    baseGate,
    rocks,
    tree,
    crystals,
    spire,
    planet,
  ) {
    GameSprites(
      towers =
        mapOf(
          TowerType.CANNON to cannon,
          TowerType.GATLING to gatling,
          TowerType.LASER to laser,
          TowerType.MISSILE to missile,
          TowerType.MORTAR to mortar,
          TowerType.FREEZE to freeze,
          TowerType.TESLA to tesla,
          TowerType.EMP to emp,
          TowerType.SHIELD to shield,
        ),
      enemies =
        mapOf(
          EnemyType.SCOUT_DRONE to scout,
          EnemyType.CYBER_SPIDER to spider,
          EnemyType.WALKER to walker,
          EnemyType.TANK_BOT to tank,
          EnemyType.FLYING_DRONE to flying,
          EnemyType.ELITE_BOSS to boss,
        ),
      abilities =
        mapOf(
          AbilityType.LIGHTNING_STRIKE to abLightning,
          AbilityType.CRYO_FREEZE to abFreeze,
          AbilityType.MISSILE_BARRAGE to abMissile,
          AbilityType.ORBITAL_OVERDRIVE to abOrbital,
        ),
      pad = pad,
      portal = portal,
      baseGate = baseGate,
      rocks = rocks,
      tree = tree,
      crystals = crystals,
      spire = spire,
      planet = planet,
    )
  }
}
