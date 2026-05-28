package com.langxiancheng.kiosk.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.langxiancheng.kiosk.ui.theme.OrangePrimary

/**
 * Enhanced card-style option button for quiz answers.
 * Features smooth selection animations, elevated shadow, and refined visual feedback.
 *
 * @param label The option label (A, B, C, D)
 * @param text The full option text
 * @param isSelected Whether this option is currently selected
 * @param onClick Callback when the option is tapped
 */
@Composable
fun OptionCard(
    label: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) OrangePrimary.copy(alpha = 0.08f) else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "option_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) OrangePrimary else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "option_border"
    )

    val labelBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) OrangePrimary else OrangePrimary.copy(alpha = 0.12f),
        animationSpec = tween(durationMillis = 200),
        label = "option_label_bg"
    )

    val labelTextColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else OrangePrimary,
        animationSpec = tween(durationMillis = 200),
        label = "option_label_text"
    )

    val scale by animateDpAsState(
        targetValue = if (isSelected) 0.98.dp else 1.0.dp,
        animationSpec = tween(durationMillis = 150),
        label = "option_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 0.dp else 3.dp,
        animationSpec = tween(durationMillis = 200),
        label = "option_elevation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale.value)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 2.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label circle with refined shape
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = labelBackgroundColor
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = labelTextColor,
                            fontSize = 17.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Option text with refined typography
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    color = if (isSelected) OrangePrimary.copy(alpha = 0.9f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
