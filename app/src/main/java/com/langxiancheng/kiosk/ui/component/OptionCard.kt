package com.langxiancheng.kiosk.ui.component

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.langxiancheng.kiosk.ui.theme.OrangePrimary

/**
 * A card-style option button for quiz answers.
 * Displays the option label (A/B/C/D) and text with selection highlighting.
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
        targetValue = if (isSelected) OrangePrimary.copy(alpha = 0.1f) else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "option_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) OrangePrimary else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "option_border"
    )

    val labelBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) OrangePrimary else OrangePrimary.copy(alpha = 0.15f),
        animationSpec = tween(durationMillis = 200),
        label = "option_label_bg"
    )

    val labelTextColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else OrangePrimary,
        animationSpec = tween(durationMillis = 200),
        label = "option_label_text"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label circle
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = labelBackgroundColor
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = labelTextColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Option text
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
