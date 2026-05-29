package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun getOptimizeProfileSuggestions(skills: String, bio: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey.startsWith("MY_GEMINI_API_KEY")) {
            // Elegant fallback simulation
            return getSimulatedProfileOptimization(skills, bio)
        }

        val prompt = """
            You are a professional freelance optimization coach in Pakistan. 
            Review the following freelancer profile bio: "$bio" and skills: "$skills".
            Provide brief, highly practical, actionable suggestions (3 bullet points) in Urdu/English mix (Hinglish/Roman Urdu) to increase local/international hires, optimize hourly rate, and target high-ticket projects. Keep it under 150 words.
        """.trimIndent()

        return makeApiCall(prompt)
    }

    suspend fun generateJobDescription(title: String, budget: Double, skillsNeeded: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey.startsWith("MY_GEMINI_API_KEY")) {
            // Elegant fallback simulation
            return getSimulatedJobDescription(title, budget, skillsNeeded)
        }

        val prompt = """
            Generate a concise, professional job description for a Pakistani freelance gig.
            Project Title: $title
            Budget: Rs. $budget
            Mandatory Skills: $skillsNeeded
            Provide standard sections: "Overview", "Key Deliverables", and "Required Expertise". Use PKR for budget details and write in clear English with a polite local tone. Under 150 words.
        """.trimIndent()

        return makeApiCall(prompt)
    }

    private suspend fun makeApiCall(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )
        return try {
            val response = api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No suggestions found at this moment."
        } catch (e: Exception) {
            "Ghalti: ${e.localizedMessage ?: "Could not connect to AI service. Reconnecting..."}"
        }
    }

    private fun getSimulatedProfileOptimization(skills: String, bio: String): String {
        val cleanSkills = skills.split(",").map { it.trim() }
        val primarySkill = cleanSkills.firstOrNull() ?: "Development"
        return """
            🌟 *SkillLink Premium Profile suggestions:*
            
            1. *Makhsoos Target Audience:* Apne bio ki pehli line mein saaf likhain ke aap kis kism ke clients ki madad karte hain. $primarySkill ke sath Pakistan aur international projects ko target karain.
            2. *Portfolio and Impact:* Serf skills na likhein balkay concrete results share karein (e.g. "Created 10+ stable Apps that loaded in 2-seconds").
            3. *Keywords optimization:* Apne profile main high-demand keywords jaise "Kotlin UI", "Scaffold layout", "Robust API integrations" shamil karein taake search me upar aa sakein.
        """.trimIndent()
    }

    private fun getSimulatedJobDescription(title: String, budget: Double, skillsNeeded: String): String {
        return """
            🎯 *Auto-Generated Job Description by SkillLink AI:*
            
            *Overview:*
            We are looking for a skilled freelancer from Pakistan to work on our project: "$title". This is a localized contract role requiring expert knowledge and rapid delivery.
            
            *Key Requirements:*
            • Deep experience in: $skillsNeeded
            • Must reside/operate in Pakistan to align with local business coordination.
            • Excellent communication and daily progress updates are required.
            
            *Deliverables:*
            • Phase 1: Interactive Prototype review & verification.
            • Phase 2: Production-grade deployment with full setup and transition documentation.
            
            *Budget & Timeline:*
            • Total Budget: Rs. ${String.format("%,.0f", budget)} (Securely locked under SkillLink Escrow)
            • Target Deadline: To be discussed based on timeline proposal.
        """.trimIndent()
    }
}
