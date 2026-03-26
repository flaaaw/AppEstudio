package com.example.appestudio.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appestudio.ui.theme.Slate700
import com.example.appestudio.ui.theme.Slate800

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerColors = listOf(
        Slate800.copy(alpha = 0.6f),
        Slate700.copy(alpha = 0.3f),
        Slate800.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    background(brush)
}

@Composable
fun ShimmerPostItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .background(Slate800, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row {
            Box(modifier = Modifier.size(40.dp).background(Slate700, RoundedCornerShape(20.dp)).shimmerEffect())
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Box(modifier = Modifier.width(100.dp).height(14.dp).shimmerEffect())
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.width(60.dp).height(10.dp).shimmerEffect())
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(18.dp).shimmerEffect())
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp).shimmerEffect())
        Spacer(modifier = Modifier.height(20.dp))
        Row {
            Box(modifier = Modifier.width(50.dp).height(16.dp).shimmerEffect())
            Spacer(modifier = Modifier.width(20.dp))
            Box(modifier = Modifier.width(50.dp).height(16.dp).shimmerEffect())
        }
    }
}
