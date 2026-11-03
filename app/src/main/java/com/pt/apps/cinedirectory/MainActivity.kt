package com.pt.apps.cinedirectory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pt.apps.cinedirectory.NotificationHelper.MovieNotificationHelper
import com.pt.apps.cinedirectory.data.local.AppDatabase
import com.pt.apps.cinedirectory.data.remote.MovieRepoImp
import com.pt.apps.cinedirectory.data.remote.NetworkModule
import com.pt.apps.cinedirectory.ui.components.CineVaultBottomBar
import com.pt.apps.cinedirectory.ui.screens.FavoritesScreen
import com.pt.apps.cinedirectory.ui.screens.HomeScreen
import com.pt.apps.cinedirectory.ui.screens.MovieDetailScreen
import com.pt.apps.cinedirectory.ui.screens.NotificationsScreen
import com.pt.apps.cinedirectory.ui.screens.WatchlistScreen
import com.pt.apps.cinedirectory.ui.theme.CineDirectoryTheme
import com.pt.apps.cinedirectory.ui.viewmodel.BottomNavTab
import com.pt.apps.cinedirectory.ui.viewmodel.MovieViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MovieViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MovieRepoImp(
            apiService = NetworkModule.movieApiService,
            movieDao = database.movieDao()
        )
        MovieViewModel.MovieViewModelFactory(repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MovieNotificationHelper.createNotificationChannels(this)

        // Handle potential push notification extra
        val initialMovieId = intent?.getStringExtra("EXTRA_MOVIE_ID")
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDark = uiState.isDarkMode ?: systemDark

            // If launched from notification with movie ID, auto select it
            LaunchedEffect(initialMovieId, uiState.movies) {
                if (!initialMovieId.isNullOrBlank() && uiState.selectedMovie == null && uiState.movies.isNotEmpty()) {
                    val targetMovie = uiState.movies.find { it.id == initialMovieId }
                    if (targetMovie != null) {
                        viewModel.selectMovie(targetMovie)
                    }
                }
            }
            CineDirectoryTheme {
                CineVaultApp(
                    viewModel = viewModel
                )

            }
        }
    }

    @Composable
    fun CineVaultApp(
        viewModel: MovieViewModel,
        modifier: Modifier = Modifier
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current

        // Show Snackbars for feedback messages
        LaunchedEffect(uiState.userMessage) {
            uiState.userMessage?.let { msg ->
                snackbarHostState.showSnackbar(msg)
                viewModel.clearUserMessage()
            }
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                // Only show bottom navigation bar when not in detail screen
                if (uiState.selectedMovie == null) {
                    CineVaultBottomBar(
                        currentTab = uiState.currentTab,
                        favoriteCount = uiState.favoriteCount,
                        watchlistCount = uiState.watchlistCount,
                        onTabSelected = { tab -> viewModel.setCurrentTab(tab) }
                    )
                }
            }
        ) { innerPadding ->
            if (uiState.selectedMovie != null) {
                // Movie Detail Screen
                MovieDetailScreen(
                    movie = uiState.selectedMovie!!,
                    onBack = { viewModel.selectMovie(null) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onToggleWatchlist = { viewModel.toggleWatchlist(it) },
                    onShareMovie = { viewModel.shareMovie(context, it) },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                // Main Content Tabs
                when (uiState.currentTab) {
                    BottomNavTab.EXPLORE -> {
                        HomeScreen(
                            uiState = uiState,
                            onMovieClick = { viewModel.selectMovie(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleWatchlist = { viewModel.toggleWatchlist(it) },
                            onShareMovie = { viewModel.shareMovie(context, it) },
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onGenreSelect = { viewModel.setSelectedGenre(it) },
                            onRefresh = { viewModel.refresh() },
                            onLoadMore = { viewModel.loadNextPage() },
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    BottomNavTab.FAVORITES -> {
                        FavoritesScreen(
                            movies = uiState.movies,
                            onMovieClick = { viewModel.selectMovie(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleWatchlist = { viewModel.toggleWatchlist(it) },
                            onShareMovie = { viewModel.shareMovie(context, it) },
                            onShareFavoritesList = { ctx, list ->
                                viewModel.shareFavoritesList(
                                    ctx,
                                    list
                                )
                            },
                            onExploreClick = { viewModel.setCurrentTab(BottomNavTab.EXPLORE) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    BottomNavTab.WATCHLIST -> {
                        WatchlistScreen(
                            movies = uiState.movies,
                            onMovieClick = { viewModel.selectMovie(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleWatchlist = { viewModel.toggleWatchlist(it) },
                            onShareMovie = { viewModel.shareMovie(context, it) },
                            onShareWatchlist = { ctx, list ->
                                viewModel.shareFavoritesList(
                                    ctx,
                                    list
                                )
                            },
                            onExploreClick = { viewModel.setCurrentTab(BottomNavTab.EXPLORE) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    BottomNavTab.NOTIFICATIONS -> {
                        NotificationsScreen(
                            onTriggerTrending = { ctx -> /*viewModel.triggerTrendingNotification(ctx)*/ },
                            onTriggerReleaseUpdate = { ctx -> /*viewModel.triggerReleaseUpdateNotification(ctx)*/ },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}


