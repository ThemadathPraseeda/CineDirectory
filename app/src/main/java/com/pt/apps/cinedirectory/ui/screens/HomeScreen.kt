package com.pt.apps.cinedirectory.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.apps.cinedirectory.domain.model.Movie
import com.pt.apps.cinedirectory.ui.components.MovieCard
import com.pt.apps.cinedirectory.ui.components.SearchBarWithGenreFilters
import com.pt.apps.cinedirectory.ui.theme.CinemaGold
import com.pt.apps.cinedirectory.ui.viewmodel.MovieUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: MovieUiState,
    onMovieClick: (Movie) -> Unit,
    onToggleFavorite: (Movie) -> Unit,
    onToggleWatchlist: (Movie) -> Unit,
    onShareMovie: (Movie) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onGenreSelect: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleDarkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val pullToRefreshState = rememberPullToRefreshState()

    // Smooth pagination trigger: when user scrolls near the end, load next page
    val shouldLoadMoreList by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMoreList) {
        if (shouldLoadMoreList && !uiState.isLoadingMore && uiState.hasMorePages) {
            onLoadMore()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header / Top App Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CinemaGold,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text(
                            text = "IMDb",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CineVault",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Top 250 Rated Movies",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            },
            actions = {
                // Dark/Light Theme Toggle
                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isDarkMode == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle theme mode",
                        tint = CinemaGold
                    )
                }

                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Pull to refresh",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Sticky Search Bar & Genre Filters
        SearchBarWithGenreFilters(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            selectedGenre = uiState.selectedGenre,
            onGenreSelect = onGenreSelect
        )

        // Offline / Cache banner if error occurred
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📡 Offline Mode: Displaying saved movies from local cache",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Retry", fontSize = 11.sp)
                    }
                }
            }
        }

        // Pull to refresh and movie list
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.movies.isEmpty()) {
                // Initial Loading State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = CinemaGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading IMDb Top Movies...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (uiState.movies.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No movies found",
                            tint = CinemaGold,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No movies match your filters",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try searching for a different title or resetting the genre filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onSearchQueryChange("")
                                onGenreSelect("All")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CinemaGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("reset_filters_button")
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else {
                // Responsive Layout: Single column on phone, 2+ columns on wide screens/tablets
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth > 600.dp

                    if (isWideScreen) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 340.dp),
                            state = gridState,
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.movies, key = { it.id }) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onMovieClick = onMovieClick,
                                    onToggleFavorite = onToggleFavorite,
                                    onToggleWatchlist = onToggleWatchlist,
                                    onShareMovie = onShareMovie
                                )
                            }

                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = CinemaGold,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.movies, key = { it.id }) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onMovieClick = onMovieClick,
                                    onToggleFavorite = onToggleFavorite,
                                    onToggleWatchlist = onToggleWatchlist,
                                    onShareMovie = onShareMovie
                                )
                            }

                            // Smooth pagination loading indicator at bottom
                            if (uiState.isLoadingMore) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = CinemaGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Loading more top rated movies...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else if (!uiState.hasMorePages && uiState.movies.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✨ You've reached the end of the top rated list!",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
