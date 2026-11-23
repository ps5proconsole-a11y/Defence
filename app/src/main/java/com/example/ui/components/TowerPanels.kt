package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.model.TowerInstance
import com.example.game.model.TowerSnapshot
import com.example.game.model.TowerType
import com.example.game.render.GameSprites
import com.example.game.render.drawTowerModel
import com.example.game.render.rememberGameSprites
import com.example.ui.theme.EnemyCrimson
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.HudEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardElevated
import com.example.ui.theme.SpaceObsidian
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun TowerBuildSheet(
  slotId: Int,
  gold: Int,
  onSelectTower: (TowerType) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onDismiss)

  Column(
    modifier =
      modifier
        .widthIn(max = 520.dp)
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(SpaceObsidian.copy(alpha = 0.94f))
        .border(1.5.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
        .padding(12.dp)
        .testTag("tower_build_sheet"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = "ПЛАТФОРМА #${slotId + 1} — СТРОИТЕЛЬСТВО",
          style = MaterialTheme.typography.titleMedium,
          color = NeonCyan,
          fontSize = 13.sp,
        )
        Text(
          text = "Выберите башню (9 типов арсенала)",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
        )
      }
      IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close), tint = TextMuted)
      }
    }

    Spacer(Modifier.height(8.dp))

    val sprites = rememberGameSprites()
    LazyVerticalGrid(
      columns = GridCells.Fixed(3),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth().height(256.dp),
    ) {
      items(TowerType.entries) { type ->
        val canAfford = gold >= type.baseCost
        TowerBuildTile(
          type = type,
          canAfford = canAfford,
          sprites = sprites,
          onClick = { onSelectTower(type) },
        )
      }
    }
  }
}

@Composable
private fun TowerBuildTile(
  type: TowerType,
  canAfford: Boolean,
  sprites: GameSprites,
  onClick: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .minimumInteractiveComponentSize()
        .clip(RoundedCornerShape(12.dp))
        .background(if (canAfford) SpaceCard else Color(0xFF0A101B))
        .border(
          width = 1.dp,
          color = if (canAfford) type.accentColor.copy(alpha = 0.65f) else Color(0xFF1E293B),
          shape = RoundedCornerShape(12.dp),
        )
        .clickable(enabled = true, onClick = onClick)
        .padding(horizontal = 6.dp, vertical = 8.dp)
        .testTag("build_tower_${type.name.lowercase()}"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Canvas(modifier = Modifier.size(34.dp)) {
      val preview =
        TowerInstance(
          slotId = 0,
          x = 0.5f,
          y = 0.5f,
          roadAnchorX = 0.5f,
          roadAnchorY = 0.5f,
          type = type,
          angleRad = -0.6f,
        )
      drawTowerModel(preview, center, size.minDimension * 0.42f, 0f, overdriveActive = false, sprites = sprites)
    }
    Spacer(Modifier.height(4.dp))
    Text(
      text = type.shortName,
      style = MaterialTheme.typography.labelLarge,
      color = if (canAfford) TextPrimary else TextMuted,
      fontSize = 11.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = type.roleTag,
      style = MaterialTheme.typography.bodySmall,
      color = type.accentColor.copy(alpha = if (canAfford) 0.9f else 0.45f),
      fontSize = 9.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(3.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = GoldAmber, modifier = Modifier.size(11.dp))
      Spacer(Modifier.width(2.dp))
      Text(
        text = "${type.baseCost}",
        style = MaterialTheme.typography.labelSmall,
        color = if (canAfford) GoldAmber else EnemyCrimson,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
fun TowerUpgradeSheet(
  tower: TowerSnapshot,
  gold: Int,
  onUpgrade: () -> Unit,
  onCyclePriority: () -> Unit,
  onSell: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onDismiss)

  Column(
    modifier =
      modifier
        .widthIn(max = 520.dp)
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(SpaceObsidian.copy(alpha = 0.94f))
        .border(1.5.dp, tower.type.accentColor.copy(alpha = 0.70f), RoundedCornerShape(18.dp))
        .padding(12.dp)
        .testTag("tower_upgrade_sheet"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(SpaceCardElevated)
              .border(1.dp, tower.type.accentColor, RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center,
        ) {
          val sprites = rememberGameSprites()
          Canvas(modifier = Modifier.size(32.dp)) {
            val preview =
              TowerInstance(
                slotId = tower.slotId,
                x = 0.5f,
                y = 0.5f,
                roadAnchorX = 0.5f,
                roadAnchorY = 0.5f,
                type = tower.type,
                level = tower.level,
                angleRad = -0.6f,
              )
            drawTowerModel(preview, center, size.minDimension * 0.42f, 0f, overdriveActive = false, sprites = sprites)
          }
        }
        Spacer(Modifier.width(10.dp))
        Column {
          Text(
            text = "${tower.type.title}  •  Ур. ${tower.level}/3",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontSize = 14.sp,
          )
          Text(
            text = tower.type.description,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close), tint = TextMuted)
      }
    }

    Spacer(Modifier.height(8.dp))

    // Telemetry row
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(SpaceCard)
          .padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      StatCell("УРОН", "${tower.damage.toInt()}", tower.type.accentColor)
      StatCell("ТЕМП", String.format("%.1f/с", tower.fireRate), NeonCyan)
      StatCell("ДАЛЬН.", "${(tower.range * 100).toInt()}", TextPrimary)
      StatCell("ФРАГИ", "${tower.kills}", GoldAmber)
    }

    Spacer(Modifier.height(10.dp))

    // Actions row: Upgrade, Priority, Sell
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val canUpgrade = tower.level < 3 && gold >= tower.upgradeCost
      Button(
        onClick = onUpgrade,
        enabled = tower.level < 3,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = if (canUpgrade) HudEmerald else SpaceCardElevated,
            contentColor = if (canUpgrade) SpaceObsidian else TextMuted,
          ),
        modifier = Modifier.weight(1.35f).height(48.dp).testTag("upgrade_tower_button"),
      ) {
        Icon(Icons.Filled.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(
          text = if (tower.level >= 3) "МАКС" else "АП (${tower.upgradeCost})",
          style = MaterialTheme.typography.labelLarge,
          fontSize = 12.sp,
        )
      }

      OutlinedButton(
        onClick = onCyclePriority,
        modifier = Modifier.weight(1.1f).height(48.dp).testTag("priority_tower_button"),
      ) {
        Icon(Icons.Filled.GpsFixed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text(
          text = tower.targetPriority.label,
          style = MaterialTheme.typography.labelSmall,
          color = TextPrimary,
          maxLines = 1,
        )
      }

      OutlinedButton(
        onClick = onSell,
        modifier = Modifier.weight(0.95f).height(48.dp).testTag("sell_tower_button"),
      ) {
        Icon(Icons.Filled.Sell, contentDescription = null, tint = GoldAmber, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text(
          text = "+${tower.sellRefund}",
          style = MaterialTheme.typography.labelLarge,
          color = GoldAmber,
          fontSize = 12.sp,
        )
      }
    }
  }
}

@Composable
private fun StatCell(label: String, value: String, valueColor: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
    Text(value, style = MaterialTheme.typography.labelLarge, color = valueColor, fontSize = 12.sp)
  }
}
