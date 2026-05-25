package com.social.flare.features.feed.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PollDisplay(
    question: String,
    options: List<String>,
    voteCounts: List<Int>?,
    userSelectedOptionIndex: Int?,
    onVote: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVotes = voteCounts?.sum() ?: 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Poll,
                contentDescription = null,
                tint = Color(0xFFFF5722),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = question,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        options.forEachIndexed { index, option ->
            val isUserSelection = userSelectedOptionIndex == index
            val hasVoted = userSelectedOptionIndex != null
            val percentage = if (totalVotes > 0 && voteCounts != null && index < voteCounts.size) {
                (voteCounts[index].toFloat() / totalVotes * 100)
            } else 0f

            PollOptionRow(
                index = index,
                text = option,
                percentage = percentage,
                isUserSelection = isUserSelection,
                hasVoted = hasVoted,
                onClick = { if (!hasVoted) onVote(index) },
                modifier = Modifier.fillMaxWidth()
            )
            if (index < options.lastIndex) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        if (totalVotes > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$totalVotes votos",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PollOptionRow(
    index: Int,
    text: String,
    percentage: Float,
    isUserSelection: Boolean,
    hasVoted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isUserSelection) Color(0xFFFF5722).copy(alpha = 0.15f)
        else Color(0xFF262626),
        animationSpec = tween(300),
        label = "optionBg"
    )

    val animatedPercentage by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "pollBar"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        if (hasVoted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage)
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF5722).copy(alpha = 0.3f))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUserSelection) Color(0xFFFF5722)
                            else Color(0xFFFF5722).copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUserSelection) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    } else if (!hasVoted) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5722))
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            if (hasVoted) {
                Text(
                    text = "${percentage.toInt()}%",
                    color = if (isUserSelection) Color(0xFFFF5722) else Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = if (isUserSelection) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
