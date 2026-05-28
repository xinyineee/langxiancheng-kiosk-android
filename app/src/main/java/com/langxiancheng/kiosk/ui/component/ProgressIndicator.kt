package com.langxiancheng.kiosk.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.langxiancheng.kiosk.ui.theme.OrangePrimary

/**
 * A horizontal progress indicator showing the current question number.
 * Displays filled/empty dots for each question.
 *
 * @param currentIndex Zero-based current question index
 * @param totalCount Total number of questions
 */
@Composable
fun ProgressIndicator(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalCount) {
            val isCurrentOrPast = i <= currentIndex
            Surface(
                modifier = Modifier.size(if (i == currentIndex) 12.dp else 8.dp),
                shape = CircleShape,
                color = if (isCurrentOrPast) OrangePrimary else OrangePrimary.copy(alpha = 0.2f)
            ) {}
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${currentIndex + 1} / $totalCount",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
