package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BookmarkEntity
import com.example.data.SearchHistoryEntity
import com.example.data.SillokRecord
import com.example.data.SillokRepository
import com.example.network.GeminiSillokService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SillokUiState(
    val searchQuery: String = "",
    val selectedKing: String = "전체",
    val selectedCategory: String = "전체",
    val searchResults: List<SillokRecord> = emptyList(),
    val selectedRecord: SillokRecord? = null,
    val isSearching: Boolean = false,
    val bookmarkedIds: Set<String> = emptySet(),
    val showShareBottomSheet: Boolean = false,
    val showAiDialog: Boolean = false,
    val aiAnalysisText: String = "",
    val isAiLoading: Boolean = false,
    val activeTab: Int = 0 // 0: Search List, 1: Map Explorer, 2: Bookmarks
)

class SillokViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SillokRepository
    private val aiService = GeminiSillokService()

    private val _uiState = MutableStateFlow(SillokUiState())
    val uiState: StateFlow<SillokUiState> = _uiState.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>>
    val searchHistory: StateFlow<List<SearchHistoryEntity>>

    init {
        val dao = AppDatabase.getDatabase(application).sillokDao()
        repository = SillokRepository(dao)

        bookmarks = repository.bookmarks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        searchHistory = repository.searchHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Observe bookmarks for UI reactive updates
        viewModelScope.launch {
            bookmarks.collect { list ->
                _uiState.update { it.copy(bookmarkedIds = list.map { b -> b.recordId }.toSet()) }
            }
        }

        // Initial search load
        performSearch()
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        performSearch()
    }

    fun onKingSelected(king: String) {
        _uiState.update { it.copy(selectedKing = king) }
        performSearch()
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        performSearch()
    }

    fun selectRecord(record: SillokRecord?) {
        _uiState.update { it.copy(selectedRecord = record) }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun toggleBookmark(record: SillokRecord) {
        viewModelScope.launch {
            val isBookmarked = _uiState.value.bookmarkedIds.contains(record.id)
            repository.toggleBookmark(record, isBookmarked)
        }
    }

    fun openShareBottomSheet(record: SillokRecord) {
        _uiState.update { it.copy(selectedRecord = record, showShareBottomSheet = true) }
    }

    fun dismissShareBottomSheet() {
        _uiState.update { it.copy(showShareBottomSheet = false) }
    }

    fun requestAiAnalysis(record: SillokRecord) {
        _uiState.update {
            it.copy(
                selectedRecord = record,
                showAiDialog = true,
                isAiLoading = true,
                aiAnalysisText = ""
            )
        }

        viewModelScope.launch {
            val analysis = aiService.analyzeSillokRecord(
                title = record.title,
                summary = record.summary,
                content = record.contentKorean
            )
            _uiState.update {
                it.copy(
                    aiAnalysisText = analysis,
                    isAiLoading = false
                )
            }
        }
    }

    fun dismissAiDialog() {
        _uiState.update { it.copy(showAiDialog = false) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private fun performSearch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val results = repository.searchSillok(
                query = _uiState.value.searchQuery,
                selectedKing = _uiState.value.selectedKing,
                selectedCategory = _uiState.value.selectedCategory
            )
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }
}
