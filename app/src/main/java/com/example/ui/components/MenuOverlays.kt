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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.model.EnemyEntity
import com.example.game.model.EnemyType
import com.example.game.model.HudUiState
import com.example.game.model.TowerInstance
import com.example.game.model.TowerType
import com.example.game.render.drawEnemyUnit
import com.example.game.render.drawTowerModel
import com.example.game.render.rememberGameSprites
import com.example.ui.theme.CrystalCyan
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
fun MainMenuOverlay(
  onStartGame: () -> Unit,
  onOpenCodex: () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(SpaceObsidian.copy(alpha = 0.70f), SpaceObsidian.copy(alpha = 0.88f)),
          ),
        )
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(20.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 460.dp)
          .fillMaxWidth()
          .clip(RoundedCornerShape(22.dp))
          .background(SpaceObsidian.copy(alpha = 0.92f))
          .border(1.5.dp, NeonCyan.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
          .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        imageVector = Icons.Filled.Shield,
        contentDescription = null,
        tint = NeonCyan,
        modifier = Modifier.size(48.dp),
      )
      Spacer(Modifier.height(10.dp))
      Text(
        text = stringResource(R.string.app_name).uppercase(),
        style = MaterialTheme.typography.displayLarge,
        color = TextPrimary,
        textAlign = TextAlign.Center,
      )
      Text(
        text = stringResource(R.string.game_subtitle),
        style = MaterialTheme.typography.labelLarge,
        color = NeonCyan,
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(16.dp))
      Text(
        text =
          "Защитите энергетический шлюз базы в скалистом каньоне от 15 волн кибернетических машин. Комбинируйте 9 типов башен с апгрейдами и применяйте орбитальные способности за кристаллы.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextMuted,
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        FeatureTag("9 БАШЕН", NeonCyan)
        FeatureTag("6 ВРАГОВ", EnemyCrimson)
        FeatureTag("4 УЛЬТЫ", CrystalCyan)
      }

      Spacer(Modifier.height(22.dp))

      Button(
        onClick = onStartGame,
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = SpaceObsidian),
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("start_game_button"),
      ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.btn_start_defense), style = MaterialTheme.typography.titleMedium)
      }

      Spacer(Modifier.height(10.dp))

      OutlinedButton(
        onClick = onOpenCodex,
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_codex_button"),
      ) {
        Icon(Icons.Filled.MenuBook, contentDescription = null, tint = NeonCyan)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.btn_codex), style = MaterialTheme.typography.labelLarge, color = TextPrimary)
      }
    }
  }
}

@Composable
private fun FeatureTag(text: String, color: Color) {
  Box(
    modifier =
      Modifier
        .clip(RoundedCornerShape(10.dp))
        .background(SpaceCard)
        .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp),
  ) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = color)
  }
}

@Composable
fun PauseOverlay(
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onOpenCodex: () -> Unit,
) {
  BackHandler(onBack = onResume)

  Box(
    modifier = Modifier.fillMaxSize().background(SpaceObsidian.copy(alpha = 0.78f)).padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 380.dp)
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(SpaceCard)
          .border(1.5.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
          .padding(22.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(stringResource(R.string.pause_title), style = MaterialTheme.typography.headlineMedium, color = NeonCyan)
      Spacer(Modifier.height(18.dp))

      Button(
        onClick = onResume,
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = SpaceObsidian),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("resume_button"),
      ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null)
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.btn_resume), style = MaterialTheme.typography.labelLarge)
      }
      Spacer(Modifier.height(10.dp))
      OutlinedButton(
        onClick = onOpenCodex,
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("pause_codex_button"),
      ) {
        Icon(Icons.Filled.MenuBook, contentDescription = null, tint = NeonCyan)
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.btn_codex), style = MaterialTheme.typography.labelLarge, color = TextPrimary)
      }
      Spacer(Modifier.height(10.dp))
      OutlinedButton(
        onClick = onRestart,
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("restart_button"),
      ) {
        Icon(Icons.Filled.Replay, contentDescription = null, tint = GoldAmber)
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.btn_restart), style = MaterialTheme.typography.labelLarge, color = GoldAmber)
      }
    }
  }
}

@Composable
fun EndGameOverlay(
  victory: Boolean,
  state: HudUiState,
  onRestart: () -> Unit,
) {
  val accent = if (victory) HudEmerald else EnemyCrimson
  Box(
    modifier = Modifier.fillMaxSize().background(SpaceObsidian.copy(alpha = 0.84f)).padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 420.dp)
          .fillMaxWidth()
          .clip(RoundedCornerShape(22.dp))
          .background(SpaceCard)
          .border(2.dp, accent, RoundedCornerShape(22.dp))
          .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = if (victory) stringResource(R.string.victory_title) else stringResource(R.string.defeat_title),
        style = MaterialTheme.typography.headlineMedium,
        color = accent,
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(12.dp))
      Text(
        text =
          if (victory) "Все 15 волн кибер-легиона отбиты! Энергетический шлюз сектора в безопасности."
          else "Враги прорвали оборону на волне ${state.wave}/${state.totalWaves}. Перегруппируйте башни и повторите оборону!",
        style = MaterialTheme.typography.bodyLarge,
        color = TextMuted,
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(16.dp))

      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SpaceCardElevated)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        EndStat("ВОЛНА", "${state.wave}/${state.totalWaves}", NeonCyan)
        EndStat("УНИЧТОЖЕНО", "${state.totalKills}", GoldAmber)
        EndStat("HP БАЗЫ", "${state.baseHp}", accent)
      }

      Spacer(Modifier.height(20.dp))
      Button(
        onClick = onRestart,
        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = SpaceObsidian),
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("play_again_button"),
      ) {
        Icon(Icons.Filled.Replay, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("ИГРАТЬ СНОВА", style = MaterialTheme.typography.titleMedium)
      }
    }
  }
}

@Composable
private fun EndStat(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
    Text(value, style = MaterialTheme.typography.titleMedium, color = color)
  }
}

@Composable
fun CodexOverlay(onClose: () -> Unit) {
  BackHandler(onBack = onClose)
  var selectedTab by remember { mutableIntStateOf(0) }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(SpaceObsidian.copy(alpha = 0.90f))
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 560.dp)
          .fillMaxSize()
          .clip(RoundedCornerShape(20.dp))
          .background(SpaceCard)
          .border(1.5.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
          .padding(16.dp)
          .testTag("codex_overlay"),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("ТАКТИЧЕСКИЙ СПРАВОЧНИК", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        IconButton(onClick = onClose, modifier = Modifier.minimumInteractiveComponentSize()) {
          Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close), tint = TextPrimary)
        }
      }

      Spacer(Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CodexTab("Башни (9)", selected = selectedTab == 0, onClick = { selectedTab = 0 }, modifier = Modifier.weight(1f))
        CodexTab("Враги (6)", selected = selectedTab == 1, onClick = { selectedTab = 1 }, modifier = Modifier.weight(1f))
      }

      Spacer(Modifier.height(10.dp))

      val sprites = rememberGameSprites()

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f).fillMaxWidth(),
      ) {
        if (selectedTab == 0) {
          items(TowerType.entries) { tower ->
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(SpaceCardElevated)
                  .border(1.dp, tower.accentColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                  .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Canvas(modifier = Modifier.size(44.dp)) {
                val t = TowerInstance(0, 0.5f, 0.5f, 0.5f, 0.5f, tower, angleRad = -0.5f)
                drawTowerModel(t, center, size.minDimension * 0.42f, 0f, false, sprites = sprites)
              }
              Spacer(Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(tower.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontSize = 14.sp)
                  Text("${tower.baseCost}g", style = MaterialTheme.typography.labelLarge, color = GoldAmber)
                }
                Text(tower.roleTag, style = MaterialTheme.typography.labelSmall, color = tower.accentColor)
                Text(tower.description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
              }
            }
          }
        } else {
          items(EnemyType.entries) { enemy ->
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(SpaceCardElevated)
                  .border(1.dp, enemy.accentColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                  .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Canvas(modifier = Modifier.size(44.dp)) {
                val e = EnemyEntity().apply {
                  active = true
                  type = enemy
                  hp = enemy.baseHp
                  maxHp = enemy.baseHp
                  shield = enemy.baseShield
                  maxShield = enemy.baseShield
                  angleRad = -0.4f
                }
                drawEnemyUnit(e, center, size.minDimension * 0.36f, 0f, sprites = sprites)
              }
              Spacer(Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(enemy.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontSize = 14.sp)
                  Text("HP ${enemy.baseHp.toInt()}", style = MaterialTheme.typography.labelLarge, color = EnemyCrimson)
                }
                Text(
                  text = if (enemy.isFlying) "АВИАЦИЯ • +${enemy.goldReward}g" else "НАЗЕМНЫЙ • +${enemy.goldReward}g",
                  style = MaterialTheme.typography.labelSmall,
                  color = enemy.accentColor,
                )
                Text(enemy.abilityDescription, style = MaterialTheme.typography.bodySmall, color = TextMuted)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CodexTab(
  title: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .height(42.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(if (selected) NeonCyan.copy(alpha = 0.2f) else SpaceCardElevated)
        .border(1.dp, if (selected) NeonCyan else Color(0xFF334155), RoundedCornerShape(10.dp))
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = if (selected) NeonCyan else TextMuted)
  }
}
