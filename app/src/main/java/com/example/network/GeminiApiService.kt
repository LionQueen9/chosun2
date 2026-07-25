package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(val text: String? = null)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiRequest(val contents: List<GeminiContent>)

data class GeminiCandidate(val content: GeminiContent?)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiSillokService {

    suspend fun analyzeSillokRecord(title: String, summary: String, content: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "AI 해설 서비스 이용을 위해서는 Gemini API 키 설정이 필요합니다. 조선왕조실록 관련 전문 주석과 원문 풀이가 제공됩니다."
        }

        val prompt = """
            당신은 조선왕조실록 전문 역사 학자이자 친절한 AI 해설사입니다.
            다음 실록 기사에 대해 일반인이 이해하기 쉽게 3가지 섹션으로 해설해주세요:
            
            기사 제목: $title
            요약: $summary
            원문/국역: $content
            
            [해설 요청 형식]
            1. 📜 **사건의 역사적 배경 및 의의**: 왜 이 사건이 중요하며, 당시 왕과 조정의 의도는 무엇이었는가?
            2. 🗺️ **주요 관련 장소 심층 설명**: 이 사건이 일어난 주요 장소(궁궐/관청/산성)의 현재 위치 및 방문 팁
            3. 💡 **한문/한자어 주요 단어 풀이**: 기사에 나온 난해한 역사 용어 2~3개 쉬운 풀이
            
            친절하고 명확한 어조로 한국어로 작성해 주세요.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "AI 해설을 불러올 수 없습니다."
        } catch (e: Exception) {
            "AI 해설 생성 중 오류 발생: ${e.localizedMessage ?: "네트워크 상태를 확인해주세요."}"
        }
    }
}
