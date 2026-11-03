package com.pt.apps.cinedirectory.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.pt.apps.cinedirectory.ui.theme.CinemaGold
import com.pt.apps.cinedirectory.ui.viewmodel.BottomNavTab


@Composable
fun CineVaultBottomBar(
    currentTab: BottomNavTab,
    favoriteCount: Int,
    watchlistCount: Int,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Explore Tab
        NavigationBarItem(
            selected = currentTab == BottomNavTab.EXPLORE,
            onClick = { onTabSelected(BottomNavTab.EXPLORE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BottomNavTab.EXPLORE) Icons.Filled.Movie else Icons.Outlined.Movie,
                    contentDescription = "Explore movies"
                )
            },
            label = { Text("Top Rated") },
            modifier = Modifier.testTag("nav_explore"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CinemaGold,
                indicatorColor = CinemaGold,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Favorites Tab with Badge
        NavigationBarItem(
            selected = currentTab == BottomNavTab.FAVORITES,
            onClick = { onTabSelected(BottomNavTab.FAVORITES) },
            icon = {
                BadgedBox(
                    badge = {
                        if (favoriteCount > 0) {
                            Badge(
                                containerColor = CinemaGold,
                                contentColor = Color.Black
                            ) {
                                Text("$favoriteCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == BottomNavTab.FAVORITES) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite movies"
                    )
                }
            },
            label = { Text("Favorites") },
            modifier = Modifier.testTag("nav_favorites"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CinemaGold,
                indicatorColor = CinemaGold,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Watchlist Tab with Badge
        NavigationBarItem(
            selected = currentTab == BottomNavTab.WATCHLIST,
            onClick = { onTabSelected(BottomNavTab.WATCHLIST) },
            icon = {
                BadgedBox(
                    badge = {
                        if (watchlistCount > 0) {
                            Badge(
                                containerColor = CinemaGold,
                                contentColor = Color.Black
                            ) {
                                Text("$watchlistCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == BottomNavTab.WATCHLIST) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Movie watchlist"
                    )
                }
            },
            label = { Text("Watchlist") },
            modifier = Modifier.testTag("nav_watchlist"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CinemaGold,
                indicatorColor = CinemaGold,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Notifications & Alerts Tab
        NavigationBarItem(
            selected = currentTab == BottomNavTab.NOTIFICATIONS,
            onClick = { onTabSelected(BottomNavTab.NOTIFICATIONS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BottomNavTab.NOTIFICATIONS) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
                    contentDescription = "Alerts and updates"
                )
            },
            label = { Text("Alerts") },
            modifier = Modifier.testTag("nav_alerts"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CinemaGold,
                indicatorColor = CinemaGold,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
