package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SillokRepository(private val dao: SillokDao) {

    val bookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val searchHistory: Flow<List<SearchHistoryEntity>> = dao.getRecentSearches()

    suspend fun searchSillok(
        query: String,
        selectedKing: String = "전체",
        selectedCategory: String = "전체"
    ): List<SillokRecord> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()

        if (trimmed.isNotEmpty()) {
            dao.insertSearchHistory(SearchHistoryEntity(keyword = trimmed))
        }

        var results = DefaultSillokData.sampleRecords

        // 1. Keyword search
        if (trimmed.isNotEmpty()) {
            results = results.filter { record ->
                record.title.contains(trimmed, ignoreCase = true) ||
                record.contentKorean.contains(trimmed, ignoreCase = true) ||
                record.contentHanja.contains(trimmed, ignoreCase = true) ||
                record.summary.contains(trimmed, ignoreCase = true) ||
                record.king.contains(trimmed, ignoreCase = true) ||
                record.tags.any { tag -> tag.contains(trimmed, ignoreCase = true) } ||
                record.locations.any { loc -> loc.name.contains(trimmed, ignoreCase = true) || loc.address.contains(trimmed, ignoreCase = true) }
            }
        }

        // 2. King filter
        if (selectedKing != "전체") {
            results = results.filter { it.king == selectedKing }
        }

        // 3. Category filter
        if (selectedCategory != "전체") {
            results = results.filter { it.category == selectedCategory }
        }

        results
    }

    fun isBookmarked(recordId: String): Flow<Boolean> = dao.isBookmarked(recordId)

    suspend fun toggleBookmark(record: SillokRecord, currentlyBookmarked: Boolean) = withContext(Dispatchers.IO) {
        if (currentlyBookmarked) {
            dao.deleteBookmark(record.id)
        } else {
            dao.insertBookmark(
                BookmarkEntity(
                    recordId = record.id,
                    title = record.title,
                    king = record.king,
                    reignYear = record.reignYear,
                    summary = record.summary
                )
            )
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearSearchHistory()
    }
}
