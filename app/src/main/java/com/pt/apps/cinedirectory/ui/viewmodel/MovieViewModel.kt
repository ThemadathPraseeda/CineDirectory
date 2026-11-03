package com.pt.apps.cinedirectory.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pt.apps.cinedirectory.domain.MovieRepository
import com.pt.apps.cinedirectory.domain.model.FilterType
import com.pt.apps.cinedirectory.domain.model.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BottomNavTab {
    EXPLORE,
    FAVORITES,
    WATCHLIST,
    NOTIFICATIONS
}

data class MovieUiState(
    val movies: List<Movie> = emptyList(),
    val favoriteCount: Int = 0,
    val watchlistCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = true,
    val searchQuery: String = "",
    val selectedGenre: String = "All",
    val currentTab: BottomNavTab = BottomNavTab.EXPLORE,
    val selectedMovie: Movie? = null,
    val isDarkMode: Boolean? = true,
    val errorMessage: String? = null,
    val userMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MovieViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedGenre = MutableStateFlow("All")
    private val _currentTab = MutableStateFlow(BottomNavTab.EXPLORE)
    private val _selectedMovie = MutableStateFlow<Movie?>(null)
    private val _isDarkMode = MutableStateFlow<Boolean?>(true)

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _isLoadingMore = MutableStateFlow(false)
    private val _hasMorePages = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)

    // Flow of filtered movies based on query, genre, and tab
    private val filteredMoviesFlow = combine(
        _searchQuery,
        _selectedGenre,
        _currentTab
    ) { query, genre, tab ->
        val filterType = when (tab) {
            BottomNavTab.EXPLORE, BottomNavTab.NOTIFICATIONS -> FilterType.ALL
            BottomNavTab.FAVORITES -> FilterType.FAVORITES
            BottomNavTab.WATCHLIST -> FilterType.WATCHLIST
        }
        Triple(query, genre, filterType)
    }.flatMapLatest { (query, genre, filterType) ->
        repository.getMoviesFlow(
            query = query,
            genre = if (genre == "All") null else genre,
            filterType = filterType
        )
    }

    private val allFavoritesFlow = repository.getMoviesFlow(filterType = FilterType.FAVORITES)
    private val allWatchlistFlow = repository.getMoviesFlow(filterType = FilterType.WATCHLIST)

    // Combine into main UI state
    val uiState: StateFlow<MovieUiState> = combine(
        filteredMoviesFlow,
        allFavoritesFlow,
        allWatchlistFlow,
        _selectedMovie,
        _isDarkMode
    ) { movies, favs, watchlist, selected, darkMode ->
        // Keep selectedMovie in sync with updated database favorite/watchlist state
        val updatedSelected = if (selected != null) {
            movies.find { it.id == selected.id } ?: selected
        } else null

        MovieUiState(
            movies = movies,
            favoriteCount = favs.size,
            watchlistCount = watchlist.size,
            isLoading = _isLoading.value,
            isRefreshing = _isRefreshing.value,
            isLoadingMore = _isLoadingMore.value,
            hasMorePages = _hasMorePages.value,
            searchQuery = _searchQuery.value,
            selectedGenre = _selectedGenre.value,
            currentTab = _currentTab.value,
            selectedMovie = updatedSelected,
            isDarkMode = darkMode,
            errorMessage = _errorMessage.value,
            userMessage = _userMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MovieUiState(isLoading = true)
    )

    init {
        initialLoad()
    }

    private fun initialLoad() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.refreshMovies()
            _isLoading.value = false
            if (result.isFailure) {
                _errorMessage.value = "Unable to connect. Showing cached movies."
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            val result = repository.refreshMovies()
            _isRefreshing.value = false
            if (result.isSuccess) {
                _userMessage.value = "Movies updated successfully"
            } else {
                _errorMessage.value = "Failed to update latest data from server."
            }
        }
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || !_hasMorePages.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val result = repository.loadNextPage()
            _isLoadingMore.value = false
            result.onSuccess { hasMore ->
                _hasMorePages.value = hasMore
            }.onFailure {
                _errorMessage.value = "Unable to load more movies."
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedGenre(genre: String) {
        _selectedGenre.value = genre
    }

    fun setCurrentTab(tab: BottomNavTab) {
        _currentTab.value = tab
    }

    fun selectMovie(movie: Movie?) {
        _selectedMovie.value = movie
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            repository.toggleFavorite(movie.id)
            val updatedState = !movie.isFavorite
            _userMessage.value =
                if (updatedState) "Added to Favorites" else "Removed from Favorites"
            if (_selectedMovie.value?.id == movie.id) {
                _selectedMovie.value = _selectedMovie.value?.copy(isFavorite = updatedState)
            }
        }
    }

    fun toggleWatchlist(movie: Movie) {
        viewModelScope.launch {
            repository.toggleWatchlist(movie.id)
            val updatedState = !movie.isInWatchlist
            _userMessage.value =
                if (updatedState) "Added to Watchlist" else "Removed from Watchlist"
            if (_selectedMovie.value?.id == movie.id) {
                _selectedMovie.value = _selectedMovie.value?.copy(isInWatchlist = updatedState)
            }
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.update { current ->
            when (current) {
                null -> true
                true -> false
                false -> true
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Social Sharing Features
    fun shareMovie(context: Context, movie: Movie) {
        val shareText = """
            🍿 Check out this IMDb Top-Rated masterpiece on CineVault!
            
            🎬 ${movie.title} (${movie.year})
            ⭐ IMDb Rating: ${movie.rating}/10 (${movie.voteCount} votes)
            ⏱️ Duration: ${movie.duration}
            🎭 Genres: ${movie.genres.joinToString(", ")}
            👤 Directed by: ${movie.director}
            
            "${movie.overview}"
            
            Shared via CineVault Android App
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, movie.title)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Movie Recommendation")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }

    fun shareFavoritesList(context: Context, movies: List<Movie>) {
        if (movies.isEmpty()) {
            _userMessage.value = "No favorites to share yet! Add some movies first."
            return
        }

        val listText = StringBuilder()
        listText.append("🌟 My Top IMDb Favorite Movies from CineVault:\n\n")
        movies.take(15).forEachIndexed { index, movie ->
            listText.append("${index + 1}. ${movie.title} (${movie.year}) - ⭐ ${movie.rating}/10\n")
        }
        listText.append("\nTotal favorites saved: ${movies.size}\nShared from CineVault Android App")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, listText.toString())
            putExtra(Intent.EXTRA_TITLE, "My CineVault Top Movie List")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Your Favorites List")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }



    class MovieViewModelFactory(
        private val repository: MovieRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
                return MovieViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
