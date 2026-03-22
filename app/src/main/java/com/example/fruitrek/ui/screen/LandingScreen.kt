package com.example.fruitrek.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LandingScreen(onStart: () -> Unit) {

    // Bouncing animation for the fruit emojis
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounce1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "b1"
    )
    val bounce2 by infiniteTransition.animateFloat(
        initialValue = 1.12f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "b2"
    )
    val bounce3 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "b3"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Top decorative fruit strip ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E7D32))
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("🥭","🍌","🍍","🥥","🟢").forEach { emoji ->
                    Text(text = emoji, fontSize = 32.sp)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── App logo area ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E7D32)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🌿", fontSize = 80.sp, modifier = Modifier.scale(bounce1))
        }

        Spacer(Modifier.height(20.dp))

        // ── App name ──────────────────────────────────────────────────────────
        Text(
            text = "FruiTrek",
            color = Color.White,
            fontSize = 56.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Interactive Fruit Explorer",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // ── School info badge ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Mapúa Malayan College Laguna  •  CS1240",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(36.dp))

        // ── Bouncing fruit row ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🥭", fontSize = 52.sp, modifier = Modifier.scale(bounce1))
            Text(text = "🍌", fontSize = 52.sp, modifier = Modifier.scale(bounce2))
            Text(text = "🍍", fontSize = 52.sp, modifier = Modifier.scale(bounce3))
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🥥", fontSize = 52.sp, modifier = Modifier.scale(bounce2))
            Text(text = "⭐", fontSize = 40.sp, modifier = Modifier.scale(bounce1))
            Text(text = "🟢", fontSize = 52.sp, modifier = Modifier.scale(bounce3))
        }

        Spacer(Modifier.height(36.dp))

        // ── Description card ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Point your camera at a fruit and discover its name in Filipino and Baybayin script!",
                color = Color.White,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                lineHeight = 25.sp
            )
        }

        Spacer(Modifier.height(32.dp))

        // ── Big START button ──────────────────────────────────────────────────
        Button(
            onClick = onStart,
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF8F00)
            )
        ) {
            Text(
                text = "Start Exploring!  🔍",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Team credits ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Created by",
                    color = Color.White.copy(alpha = 0.60f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Espiritu  •  Sabandal  •  Tidalgo  •  Tingson",
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "March 2026",
                    color = Color.White.copy(alpha = 0.50f),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}