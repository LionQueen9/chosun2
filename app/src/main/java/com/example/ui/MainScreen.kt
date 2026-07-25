package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DefaultSillokData
import com.example.data.SillokRecord
import com.example.ui.components.*
import com.example.ui.theme.JoseonGoldSecondary
import com.example.ui.theme.JoseonIndigoTertiary
import com.example.ui.theme.JoseonRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SillokViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val focusManager = LocalFocusManager.current

    // If a record detail is open, show SillokDetailScreen
    if (uiState.selectedRecord != null && !uiState.showShareBottomSheet && !uiState.showAiDialog) {
        val selected = uiState.selectedRecord!!
        val isBookmarked = uiState.bookmarkedIds.contains(selected.id)
        
        SillokDetailScreen(
            record = selected,
            isBookmarked = isBookmarked,
            onBackClick = { viewModel.selectRecord(null) },
            onBookmarkToggle = { viewModel.toggleBookmark(selected) },
            onShareClick = { viewModel.openShareBottomSheet(selected) },
            onAiAnalysisClick = { viewModel.requestAiAnalysis(selected) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(JoseonRedPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "조선왕조실록",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "실록 검색 • 구글 맵 지도 • SNS 연동",
                                style = MaterialTheme.typography.labelSmall,
                                color = JoseonGoldSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                    label = { Text("실록 검색", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = { Icon(Icons.Outlined.Map, contentDescription = "지도") },
                    label = { Text("구글 맵 지도", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "스크랩") },
                    label = { Text("즐겨찾기 (${bookmarks.size})", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.activeTab) {
                0 -> SearchTabContent(
                    uiState = uiState,
                    searchHistory = searchHistory,
                    onQueryChange = { viewModel.onQueryChanged(it) },
                    onKingSelect = { viewModel.onKingSelected(it) },
                    onCategorySelect = { viewModel.onCategorySelected(it) },
                    onRecordSelect = { viewModel.selectRecord(it) },
                    onBookmarkToggle = { viewModel.toggleBookmark(it) },
                    onShareClick = { viewModel.openShareBottomSheet(it) },
                    onClearHistory = { viewModel.clearSearchHistory() }
                )
                1 -> MapExplorerView(
                    onRecordSelect = { viewModel.selectRecord(it) }
                )
                2 -> BookmarksTabContent(
                    bookmarkedIds = uiState.bookmarkedIds,
                    onRecordSelect = { recordId ->
                        val found = DefaultSillokData.sampleRecords.find { it.id == recordId }
                        if (found != null) viewModel.selectRecord(found)
                    },
                    onRemoveBookmark = { recordId ->
                        val found = DefaultSillokData.sampleRecords.find { it.id == recordId }
                        if (found != null) viewModel.toggleBookmark(found)
                    }
                )
            }
        }
    }

    // Modal Overlays
    if (uiState.showShareBottomSheet && uiState.selectedRecord != null) {
        SnsShareBottomSheet(
            record = uiState.selectedRecord!!,
            onDismissRequest = { viewModel.dismissShareBottomSheet() }
        )
    }

    if (uiState.showAiDialog && uiState.selectedRecord != null) {
        GeminiAiDialog(
            recordTitle = uiState.selectedRecord!!.title,
            aiAnalysisText = uiState.aiAnalysisText,
            isLoading = uiState.isAiLoading,
            onDismissRequest = { viewModel.dismissAiDialog() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTabContent(
    uiState: SillokUiState,
    searchHistory: List<com.example.data.SearchHistoryEntity>,
    onQueryChange: (String) -> Unit,
    onKingSelect: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onRecordSelect: (SillokRecord) -> Unit,
    onBookmarkToggle: (SillokRecord) -> Unit,
    onShareClick: (SillokRecord) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("검색어 입력 (예: 훈민정음, 수원화성, 측우기, 이순신)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = JoseonRedPrimary) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JoseonRedPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Popular Keyword Suggestions
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "🔥 인기 검색어:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = JoseonRedPrimary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(DefaultSillokData.popularSearchKeywords) { keyword ->
                SuggestionChip(
                    onClick = { onQueryChange(keyword) },
                    label = { Text(keyword, fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // King Filter Chips
        Column {
            Text(
                text = "👑 왕대별 검색 필터",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(DefaultSillokData.kingNames) { king ->
                    val isSelected = uiState.selectedKing == king
                    FilterChip(
                        selected = isSelected,
                        onClick = { onKingSelect(king) },
                        label = { Text(king, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JoseonRedPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        Column {
            Text(
                text = "🏷️ 분야별 필터",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(DefaultSillokData.categoryNames) { category ->
                    val isSelected = uiState.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JoseonIndigoTertiary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Results Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "검색 결과 (${uiState.searchResults.size}건)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "검색 결과가 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "다른 키워드(예: 훈민정음, 수원화성, 임진왜란)로 검색해 보세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.searchResults) { record ->
                    val isBookmarked = uiState.bookmarkedIds.contains(record.id)
                    SillokCard(
                        record = record,
                        isBookmarked = isBookmarked,
                        onClick = { onRecordSelect(record) },
                        onBookmarkClick = { onBookmarkToggle(record) },
                        onShareClick = { onShareClick(record) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarksTabContent(
    bookmarkedIds: Set<String>,
    onRecordSelect: (String) -> Unit,
    onRemoveBookmark: (String) -> Unit
) {
    val savedRecords = DefaultSillokData.sampleRecords.filter { bookmarkedIds.contains(it.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "내가 즐겨찾기한 실록 기사 (${savedRecords.size}건)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "저장된 핵심 사건 및 유적지 정보를 다시 확인해보세요.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (savedRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("저장된 즐겨찾기가 없습니다.", fontWeight = FontWeight.Bold)
                    Text("검색 목록에서 북마크 아이콘을 눌러 저장하세요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedRecords) { record ->
                    SillokCard(
                        record = record,
                        isBookmarked = true,
                        onClick = { onRecordSelect(record.id) },
                        onBookmarkClick = { onRemoveBookmark(record.id) },
                        onShareClick = {}
                    )
                }
            }
        }
    }
}
