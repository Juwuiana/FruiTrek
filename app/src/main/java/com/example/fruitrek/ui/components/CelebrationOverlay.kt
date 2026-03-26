package com.example.fruitrek.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.fruitrek.data.model.Fruit
import com.example.fruitrek.ui.theme.TropicalGreen

@Composable
fun CelebrationOverlay(
    fruit: Fruit?,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible && fruit != null,
        enter = fadeIn(tween(300)) + scaleIn(
            initialScale = 0.5f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.8f)
    ) {
        if (fruit == null) return@AnimatedVisibility

        // Bouncy emoji animation
        val infiniteTransition = rememberInfiniteTransition(label = "emoji_bounce")
        val emojiScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emoji_scale"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(36.dp))
                    .background(fruit.color.copy(alpha = 0.97f))
                    .padding(28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Stars row — kids love stars
                    Text(
                        text = "⭐ ⭐ ⭐",
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )

                    // Bouncy big emoji
                    Text(
                        text = fruit.emoji,
                        fontSize = 88.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.scale(emojiScale)
                    )

                    // "You found it!" — big, exciting, readable
                    Text(
                        text = "You found it!",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    // Fruit name — very large for early readers
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(vertical = 12.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = fruit.englishName,
                                color = Color.White,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "sa Filipino: ${fruit.filipinoName}",
                                color = Color.White.copy(alpha = 0.90f),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Baybayin badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.20f))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Baybayin",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                            Text(
                                text = fruit.baybayin,
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Fun fact in a friendly box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Did you know?",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = fruit.funFact,
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    // Passport added notice
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Added to your Passport!", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(4.dp))

                    // Tap to continue — big enough for small fingers
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.30f))
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap anywhere to keep exploring!",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}