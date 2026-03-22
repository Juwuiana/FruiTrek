package com.example.fruitrek.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fruitrek.data.model.Fruit
import com.example.fruitrek.ui.FruiTrekViewModel
import com.example.fruitrek.ui.theme.*

@Composable
fun PassportScreen(
    viewModel: FruiTrekViewModel,
    onBack   : () -> Unit
) {
    val unlockedIds by viewModel.unlockedFruitIds.collectAsState()
    val fruits      = viewModel.getPassportFruits(unlockedIds)
    val total       = fruits.size
    val unlocked    = fruits.count { it.second }

    var selected by remember { mutableStateOf<Fruit?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), CreamWhite)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Back button
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint               = Color.White
                    )
                }

                // Title
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = "🌴 Fruit Passport",
                        color      = Color.White,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text     = "$unlocked / $total fruits discovered",
                        color    = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }

            // ── Progress bar ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            ) {
                // Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.28f))
                )
                // Fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (total == 0) 0f else unlocked.toFloat() / total)
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SunshineYellow)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Fruit Grid ────────────────────────────────────────────────────
            LazyVerticalGrid(
                columns             = GridCells.Fixed(2),
                modifier            = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(CreamWhite)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp)
            ) {
                items(fruits) { (fruit, isUnlocked) ->
                    FruitCard(
                        fruit      = fruit,
                        isUnlocked = isUnlocked,
                        onClick    = { if (isUnlocked) selected = fruit }
                    )
                }
            }
        }

        // ── Detail bottom sheet ───────────────────────────────────────────────
        selected?.let { fruit ->
            FruitDetailSheet(
                fruit     = fruit,
                onDismiss = { selected = null }
            )
        }
    }
}

// ── Fruit card ────────────────────────────────────────────────────────────────

@Composable
private fun FruitCard(
    fruit     : Fruit,
    isUnlocked: Boolean,
    onClick   : () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isUnlocked) Color(fruit.color).copy(alpha = 0.16f)
                        else            Color(0xFFEEEEEE),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "cardBg"
    )

    Card(
        onClick   = onClick,
        modifier  = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .alpha(if (isUnlocked) 1f else 0.50f)
            .then(
                if (isUnlocked)
                    Modifier.border(2.dp, Color(fruit.color), RoundedCornerShape(20.dp))
                else
                    Modifier
            ),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(if (isUnlocked) 4.dp else 0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emoji or mystery icon
            Text(
                text     = if (isUnlocked) fruit.emoji else "❓",
                fontSize = 46.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text       = if (isUnlocked) fruit.englishName else "???",
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
                textAlign  = TextAlign.Center
            )

            if (isUnlocked) {
                Text(
                    text     = fruit.filipinoName,
                    fontSize = 12.sp,
                    color    = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Baybayin badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(fruit.color).copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text       = fruit.baybayin,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(fruit.color)
                    )
                }
            }
        }
    }
}

// ── Detail bottom sheet ───────────────────────────────────────────────────────

@Composable
private fun FruitDetailSheet(fruit: Fruit, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.50f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* consume clicks so card doesn't dismiss */ },
            shape  = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = fruit.emoji, fontSize = 68.sp)

                Text(
                    text       = fruit.englishName,
                    fontSize   = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Name chips
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoChip(
                        label = "Filipino",
                        value = fruit.filipinoName,
                        color = TropicalGreen
                    )
                    InfoChip(
                        label = "Baybayin",
                        value = fruit.baybayin,
                        color = MangoOrange
                    )
                }

                // Fun fact card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(fruit.color).copy(alpha = 0.12f))
                        .padding(16.dp)
                ) {
                    Text(
                        text      = fruit.funFact,
                        fontSize  = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(fruit.color))
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
