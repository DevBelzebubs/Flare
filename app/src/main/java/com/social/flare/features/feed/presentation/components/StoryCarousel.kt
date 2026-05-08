package com.social.flare.features.feed.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StoryCarousel(
    modifier: Modifier = Modifier
) {
    val mockStories = listOf("Arthur Morgan", "Lana_queen", "Wa", "Leftsito", "Usuario5")

    LazyRow(
        modifier = modifier.padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AddStoryItem()
        }

        items(mockStories.size) { index ->
            StoryItem(username = mockStories[index])
        }
    }
}

@Composable
private fun AddStoryItem() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                .size(68.dp)
                .clickable { /* TODO: Abrir cámara/galería */ }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .padding(2.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFFF5722))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Story",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your story",
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StoryItem(username: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFFF5722),
                    shape = CircleShape
                )
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable { /* TODO: Ver historia */ }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = username,
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}