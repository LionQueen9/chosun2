package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class LocationPoint(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val description: String
)

data class RelatedImage(
    val url: String,
    val title: String,
    val source: String
)

data class SillokRecord(
    val id: String,
    val title: String,
    val king: String,              // e.g. 세종, 정조, 태조
    val gregorianYear: Int,        // e.g. 1443
    val reignYear: String,         // e.g. 세종 25년 12월 30일
    val dateString: String,        // e.g. 1443년 12월 30일 (경술)
    val contentKorean: String,     // 국역
    val contentHanja: String,      // 원문
    val summary: String,           // 핵심 요약
    val category: String,          // 문화/과학, 정치, 외교/전쟁, 사회/제도
    val locations: List<LocationPoint>, // 주요 관련 장소들 (위도, 경도, 설명)
    val imageUrls: List<RelatedImage>,  // 연관 이미지 2-3개
    val tags: List<String>
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val recordId: String,
    val title: String,
    val king: String,
    val reignYear: String,
    val summary: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyword: String,
    val timestamp: Long = System.currentTimeMillis()
)
