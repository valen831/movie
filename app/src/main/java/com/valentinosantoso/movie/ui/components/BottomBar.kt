package com.valentinosantoso.movie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinosantoso.movie.ui.theme.DarkBackground
import com.valentinosantoso.movie.ui.theme.DarkSurface
import com.valentinosantoso.movie.ui.theme.PrimaryRed
import com.valentinosantoso.movie.ui.theme.TextMuted
import com.valentinosantoso.movie.ui.theme.TextPrimary

enum class AppTab(val title: String) {
    HOME("Beranda"),
    SEARCH("Cari"),
    FAVORITES("Favorit")
}

@Composable
fun MovieBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = DarkSurface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == AppTab.HOME,
            onClick = { onTabSelected(AppTab.HOME) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Beranda",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Beranda",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == AppTab.HOME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryRed,
                selectedTextColor = PrimaryRed,
                indicatorColor = PrimaryRed.copy(alpha = 0.15f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            selected = selectedTab == AppTab.SEARCH,
            onClick = { onTabSelected(AppTab.SEARCH) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Cari",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == AppTab.SEARCH) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryRed,
                selectedTextColor = PrimaryRed,
                indicatorColor = PrimaryRed.copy(alpha = 0.15f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            selected = selectedTab == AppTab.FAVORITES,
            onClick = { onTabSelected(AppTab.FAVORITES) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorit",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Favorit",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == AppTab.FAVORITES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryRed,
                selectedTextColor = PrimaryRed,
                indicatorColor = PrimaryRed.copy(alpha = 0.15f),
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )
    }
}
