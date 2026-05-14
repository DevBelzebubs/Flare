package com.social.flare.features.search.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.flare.features.search.presentation.components.TrendingTag
import com.social.flare.features.search.presentation.components.NewsCard
import com.social.flare.features.search.presentation.components.SearchBar


@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }

    val mockNews = listOf(
        Pair("Title News", "Description news max 2 lines to keep it clean and readable..."),
        Pair("Second News Title", "This is another description for the second news card..."),
        Pair("Global Update", "Important events happening right now around the world...")
    )

    val trendingTopics = listOf(
        Pair("#ChainsawMan", "124K"),
        Pair("#HexagonalArch", "85K"),
        Pair("#Kotlin", "45K"),
        Pair("#JetpackCompose", "32K")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "News",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mockNews) { news ->
                    NewsCard(title = news.first, description = news.second)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Trending now",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trendingTopics) { topic ->
                    TrendingTag(
                        tagName = topic.first,
                        postCount = topic.second,
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Explore",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}