package com.social.flare.features.feed.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StoryCarousel(
    modifier: Modifier = Modifier
    // stories: List<Story>
) {
    LazyRow(
        modifier = modifier.padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AddStoryItem()
        }

        // Ítems dinámicos para los demás usuarios
        // items(stories) { story ->
        //     StoryItem(story = story)
        // }
    }
}

@Composable
private fun AddStoryItem() {
    // Implementación del círculo con tu avatar y el ícono '+'
}

@Composable
private fun StoryItem(/* story: Story */) {
    // Implementación del avatar con borde naranja y el nombre de usuario
}