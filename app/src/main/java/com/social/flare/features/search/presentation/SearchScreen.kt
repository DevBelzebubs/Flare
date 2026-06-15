package com.social.flare.features.search.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.social.flare.features.admin.domain.model.NewsItem
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.search.domain.model.TrendingHashtag
import com.social.flare.features.profile.presentation.components.ProfileGridItem
import com.social.flare.features.search.presentation.components.TrendingTag
import com.social.flare.features.search.presentation.components.NewsCard
import com.social.flare.features.search.presentation.components.SearchBar
import com.social.flare.features.search.presentation.components.SearchProfileItem


@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onPostClick: (String) -> Unit = {},
    onAuthorClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        SearchBar(
            query = uiState.query,
            onQueryChange = viewModel::onQueryChange,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.query.isBlank()) {
            ExploreContent(
                news = uiState.news,
                isLoading = uiState.isLoading,
                trendingHashtags = uiState.trendingHashtags,
                explorePosts = uiState.explorePosts,
                onHashtagClick = viewModel::onHashtagClick,
                onPostClick = onPostClick
            )
        } else {
            SearchResultsContent(
                uiState = uiState,
                onTabSelected = viewModel::selectTab,
                onPostClick = onPostClick,
                onAuthorClick = onAuthorClick
            )
        }
    }
}

@Composable
private fun ExploreContent(
    news: List<NewsItem>,
    isLoading: Boolean,
    trendingHashtags: List<TrendingHashtag>,
    explorePosts: List<Post>,
    onHashtagClick: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(title = "News")
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            } else if (news.isEmpty()) {
                Text(
                    text = "No hay noticias disponibles",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(news) { item ->
                        NewsCard(title = item.title, description = item.description)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(title = "Trending now")
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            if (trendingHashtags.isEmpty()) {
                Text(
                    text = "No hay tendencias",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingHashtags) { tag ->
                        TrendingTag(
                            tagName = "#${tag.name}",
                            postCount = tag.postCount.toString(),
                            modifier = Modifier.width(160.dp),
                            onClick = { onHashtagClick(tag.name) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(title = "Explore")
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(explorePosts) { post ->
            ProfileGridItem(post = post, onClick = { onPostClick(post.id) })
        }
    }
}

@Composable
private fun SearchResultsContent(
    uiState: SearchUiState,
    onTabSelected: (SearchTab) -> Unit,
    onPostClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            SearchTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                SearchTab.PROFILES -> "Profiles"
                                SearchTab.POSTS -> "Posts"
                                SearchTab.HASHTAGS -> "Hashtags"
                            },
                            color = if (uiState.selectedTab == tab) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (uiState.selectedTab) {
            SearchTab.PROFILES -> {
                val profiles = uiState.searchResults?.profiles ?: emptyList()
                if (profiles.isEmpty()) {
                    EmptyResultMessage()
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(profiles) { citizen ->
                            SearchProfileItem(
                                citizen = citizen,
                                onClick = { onAuthorClick(citizen.citizen_id) }
                            )
                        }
                    }
                }
            }

            SearchTab.POSTS -> {
                val posts = uiState.searchResults?.posts ?: emptyList()
                if (posts.isEmpty()) {
                    EmptyResultMessage()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(posts) { post ->
                            ProfileGridItem(post = post, onClick = { onPostClick(post.id) })
                        }
                    }
                }
            }

            SearchTab.HASHTAGS -> {
                val hashtagPosts = uiState.searchResults?.hashtagPosts ?: emptyList()
                if (hashtagPosts.isEmpty()) {
                    EmptyResultMessage()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(hashtagPosts) { post ->
                            ProfileGridItem(post = post, onClick = { onPostClick(post.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun EmptyResultMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Sin resultados",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
    }
}
