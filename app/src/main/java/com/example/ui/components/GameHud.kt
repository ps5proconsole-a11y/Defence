package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.model.AbilityType
import com.example.game.model.HudUiState
import com.example.game.render.rememberGameSprites
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.EnemyCrimson
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.HudEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceObsidian
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun TopGameHud(
  state: HudUiState,
  onPauseClick: () -> Unit,
  onNextWaveClick: () -> Unit,
  onOpenCodexClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 10.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(SpaceObsidian.copy(alpha = 0.84f))
          .border(1.dp, NeonCyan.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
          .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      // Pause Button
      Box(
        modifier =
          Modifier
            .minimumInteractiveComponentSize()
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SpaceCard)
            .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .clickable(onClick = onPauseClick)
            .testTag("pause_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Pause,
          contentDescription = stringResource(R.string.cd_pause),
          tint = NeonCyan,
          modifier = Modifier.size(20.dp),
        )
      }

      // Base HP
      HudStatPill(
        icon = Icons.Filled.Favorite,
        iconTint = EnemyCrimson,
        value = "${state.baseHp}",
        contentDescription = stringResource(R.string.cd_base_hp),
        modifier = Modifier.testTag("base_hp_badge"),
      )

      // Gold
      HudStatPill(
        icon = Icons.Filled.MonetizationOn,
        iconTint = GoldAmber,
        value = "${state.gold}",
        contentDescription = stringResource(R.string.cd_gold),
        modifier = Modifier.testTag("gold_counter"),
      )

      // Crystals
      HudStatPill(
        icon = Icons.Filled.Diamond,
        iconTint = CrystalCyan,
        value = "${state.crystals}",
        contentDescription = stringResource(R.string.cd_crystals),
        modifier = Modifier.testTag("crystal_counter"),
      )

      // Wave Counter
      HudStatPill(
        icon = Icons.Filled.Warning,
        iconTint = Color(0xFFFF6B2B),
        value = "${state.wave}/${state.totalWaves}",
        contentDescription = stringResource(R.string.cd_wave),
        modifier = Modifier.testTag("wave_counter"),
      )

      // Codex quick button
      Box(
        modifier =
          Modifier
            .minimumInteractiveComponentSize()
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SpaceCard)
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .clickable(onClick = onOpenCodexClick)
            .testTag("hud_codex_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.MenuBook,
          contentDescription = stringResource(R.string.btn_codex),
          tint = TextPrimary,
          modifier = Modifier.size(18.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Secondary row: Early Wave Launch button + Status Banner
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      if (!state.waveInProgress && state.wave < state.totalWaves) {
        Surface(
          onClick = onNextWaveClick,
          shape = RoundedCornerShape(20.dp),
          color = HudEmerald.copy(alpha = 0.18f),
          border = androidx.compose.foundation.BorderStroke(1.dp, HudEmerald),
          modifier = Modifier.minimumInteractiveComponentSize().testTag("next_wave_button"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = HudEmerald, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(
              text =
                if (state.wave == 0) "СТАРТ (${state.betweenWaveCountdown.toInt() + 1}с)"
                else "ВОЛНА ${state.wave + 1} (${state.betweenWaveCountdown.toInt() + 1}с)",
              style = MaterialTheme.typography.labelSmall,
              color = HudEmerald,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      } else {
        Spacer(Modifier.width(4.dp))
      }

      AnimatedVisibility(visible = state.statusBanner != null, enter = fadeIn(), exit = fadeOut()) {
        state.statusBanner?.let { msg ->
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(SpaceObsidian.copy(alpha = 0.86f))
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
          ) {
            Text(
              text = msg,
              style = MaterialTheme.typography.bodySmall,
              color = TextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HudStatPill(
  icon: ImageVector,
  iconTint: Color,
  value: String,
  contentDescription: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.padding(horizontal = 3.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = iconTint,
      modifier = Modifier.size(15.dp),
    )
    Spacer(Modifier.width(3.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.labelLarge,
      color = TextPrimary,
      fontSize = 12.sp,
    )
  }
}

@Composable
fun BottomAbilityBar(
  state: HudUiState,
  onAbilityClick: (AbilityType) -> Unit,
  onSpeedToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val sprites = rememberGameSprites()
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.navigationBars)
        .padding(horizontal = 10.dp, vertical = 8.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(
          Brush.verticalGradient(
            listOf(SpaceObsidian.copy(alpha = 0.90f), Color(0xEE050810)),
          ),
        )
        .border(1.dp, NeonCyan.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
        .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    AbilityType.entries.forEach { ability ->
      val cd = state.abilityCooldowns[ability] ?: 0f
      val canAfford = state.crystals >= ability.crystalCost
      val isTargeting = state.activeAbilityTargeting == ability
      val tag =
        when (ability) {
          AbilityType.LIGHTNING_STRIKE -> "ability_lightning"
          AbilityType.CRYO_FREEZE -> "ability_freeze"
          AbilityType.MISSILE_BARRAGE -> "ability_barrage"
          AbilityType.ORBITAL_OVERDRIVE -> "ability_orbital"
        }

      AbilityButton(
        ability = ability,
        sprite = sprites.abilities[ability],
        cooldown = cd,
        canAfford = canAfford,
        isTargeting = isTargeting,
        onClick = { onAbilityClick(ability) },
        modifier = Modifier.weight(1f).testTag(tag),
      )
    }

    // 1x / 2x Speed Toggle Button
    val isFast = state.speedMultiplier == 2
    Column(
      modifier =
        Modifier
          .minimumInteractiveComponentSize()
          .width(58.dp)
          .height(54.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(if (isFast) NeonCyan.copy(alpha = 0.24f) else SpaceCard)
          .border(1.5.dp, if (isFast) NeonCyan else Color(0xFF334155), RoundedCornerShape(12.dp))
          .clickable(onClick = onSpeedToggle)
          .testTag("speed_toggle_button"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.FastForward,
        contentDescription = stringResource(R.string.cd_speed),
        tint = if (isFast) NeonCyan else TextPrimary,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = "x${state.speedMultiplier}",
        style = MaterialTheme.typography.labelLarge,
        color = if (isFast) NeonCyan else TextPrimary,
        fontSize = 12.sp,
      )
    }
  }
}

@Composable
private fun AbilityButton(
  ability: AbilityType,
  sprite: ImageBitmap?,
  cooldown: Float,
  canAfford: Boolean,
  isTargeting: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val ready = cooldown <= 0.05f && canAfford
  val borderColor =
    when {
      isTargeting -> Color.White
      ready -> ability.accentColor
      else -> Color(0xFF283548)
    }

  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .height(58.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(
          if (isTargeting) ability.accentColor.copy(alpha = 0.30f)
          else if (ready) SpaceCard
          else Color(0xFF0A101D),
        )
        .border(if (isTargeting) 2.dp else 1.2.dp, borderColor, RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 4.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
      if (sprite != null) {
        Image(
          bitmap = sprite,
          contentDescription = ability.title,
          modifier = Modifier.size(24.dp).alpha(if (ready || isTargeting) 1f else 0.42f),
        )
      }
      Text(
        text = ability.shortName,
        style = MaterialTheme.typography.labelSmall,
        color = if (ready || isTargeting) TextPrimary else TextMuted,
        fontSize = 9.sp,
        maxLines = 1,
      )
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier.size(6.dp).clip(CircleShape).background(CrystalCyan),
        )
        Spacer(Modifier.width(3.dp))
        Text(
          text = if (cooldown > 0.05f) "${cooldown.toInt() + 1}с" else "${ability.crystalCost}",
          style = MaterialTheme.typography.labelSmall,
          color = if (cooldown > 0.05f) GoldAmber else if (canAfford) CrystalCyan else EnemyCrimson,
          fontSize = 10.sp,
        )
      }
    }
  }
}
