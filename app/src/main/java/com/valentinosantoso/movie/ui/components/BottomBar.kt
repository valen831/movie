package com.valentinosantoso.movie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinosantoso.movie.R
import com.valentinosantoso.movie.ui.theme.PrimaryRed

enum class AppTab {
    HOME,
    SEARCH,
    FAVORITES
}

@Composable
fun MovieBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navColors = NavigationBarItemDefaults.colors(
            selectedIconColor = PrimaryRed,
            selectedTextColor = PrimaryRed,
            indicatorColor = PrimaryRed.copy(alpha = 0.15f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NavigationBarItem(
            selected = selectedTab == AppTab.HOME,
            onClick = { onTabSelected(AppTab.HOME) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(R.string.tab_home),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.tab_home),
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == AppTab.HOME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navColors
        )

        NavigationBarItem(
            selected = selectedTab == AppTab.SEARCH,
            onClick = { onTabSelected(AppTab.SEARCH) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.tab_search),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.tab_search),
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == AppTab.SEARCH) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navColors
        )

        NavigationBarItem(
            selected = selectedTab == AppTab.FAVORITES,
            onClick = { onTabSelected(AppTab.FAVORITES) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = stringResource(R.string.tab_favorites),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.tab_favorites),
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == AppTab.FAVORITES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navColors
        )
    }
}

/**
 * Navigation rail positioned on the left edge for landscape mode.
 * Gives full vertical height to the screen content and prevents bottom bar obstruction.
 */
@Composable
fun MovieNavigationRail(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PrimaryRed)
                    .size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "C",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    ) {
        val railColors = NavigationRailItemDefaults.colors(
            selectedIconColor = PrimaryRed,
            selectedTextColor = PrimaryRed,
            indicatorColor = PrimaryRed.copy(alpha = 0.15f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NavigationRailItem(
            selected = selectedTab == AppTab.HOME,
            onClick = { onTabSelected(AppTab.HOME) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(R.string.tab_home),
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.tab_home),
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == AppTab.HOME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = railColors
        )

        Spacer(modifier = Modifier.height(6.dp))

        NavigationRailItem(
            selected = selectedTab == AppTab.SEARCH,
            onClick = { onTabSelected(AppTab.SEARCH) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.tab_search),
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.tab_search),
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == AppTab.SEARCH) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = railColors
        )

        Spacer(modifier = Modifier.height(6.dp))

        NavigationRailItem(
            selected = selectedTab == AppTab.FAVORITES,
            onClick = { onTabSelected(AppTab.FAVORITES) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = stringResource(R.string.tab_favorites),
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.tab_favorites),
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == AppTab.FAVORITES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = railColors
        )
    }
}
