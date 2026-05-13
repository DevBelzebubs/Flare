package com.social.flare.features.profile.presentation.components.profile

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ProfileTabSection(tabs: List<Pair<String, ImageVector>>, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab, containerColor = Color.Black, contentColor = Color(0xFFFF5722),
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Color(0xFFFF5722))
        }
    ) {
        tabs.forEachIndexed { index, pair ->
            Tab(selected = selectedTab == index, onClick = { onTabSelected(index) },
                text = { Text(pair.first, color = if (selectedTab == index) Color.White else Color.Gray) },
                icon = { Icon(pair.second, contentDescription = pair.first, tint = if (selectedTab == index) Color(0xFFFF5722) else Color.Gray) }
            )
        }
    }
}