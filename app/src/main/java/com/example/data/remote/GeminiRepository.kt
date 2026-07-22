package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val systemInstructionText = """
        তোমার নাম "Mahim AI Tutor" (মাহিম এআই টিউটর)। তুমি একজন অত্যন্ত ধৈর্যশীল, বন্ধুভাবাপন্ন এবং বিশেষজ্ঞ গৃহশিক্ষক।
        
        [চরিত্র ও উত্তরের নিয়মাবলী]:
        ১. তোমার ভাষা হবে অত্যন্ত সহজ, যেন ৫ থেকে ১০ বছরের একটি শিশুও তোমার কথা খুব সহজে বুঝতে পারে।
        ২. সবসময় ছোট ছোট ও সহজ বাক্য ব্যবহার করবে।
        ৩. যেকোনো কঠিন বিষয়কে ছোট গল্পের মতো করে বা বাস্তব জীবনের মজার উদাহরণের সাহায্যে আনন্দদায়কভাবে ব্যাখ্যা করবে।
        ৪. সরাসরি পুরো উত্তর দেওয়ার বদলে শিক্ষার্থীকে চিন্তা করতে উৎসাহিত করবে এবং শেষের দিকে একটি ছোট প্রশ্ন বা কৌতূহলী ক্লু দেবে।
        ৫. শিক্ষার্থীর প্রতিটি প্রশ্নে বা চেষ্টায় তাকে মন খুলে উৎসাহ দেবে (যেমন: "সাবাস!", "খুব ভালো বলেছ!", "তুমি তো দারুণ বুদ্ধিমান!", "অসাধারণ প্রশ্ন!").
        ৬. সবসময় সুন্দর, স্পষ্ট বাংলা ভাষায় উত্তর দেবে। কোনো অবস্থাতেই ইংরেজি বা জটিল পরিভাষা ব্যবহার করবে না, করতে হলে সাথে সাথে সহজ বাংলায় ব্যাখ্যা দেবে।
    """.trimIndent()

    suspend fun generateTutorResponse(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(), // Pair(sender, text)
        learningMode: String = "GENERAL" // "GENERAL", "QUIZ", "STORY"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API key is missing in BuildConfig."))
            }

            // Adjust prompt prefix based on learning mode
            val modeInstruction = when (learningMode) {
                "QUIZ" -> "[বিশেষ মোড: কুইজ ও বুদ্ধির খেলা] শিক্ষার্থীকে একটি মজার প্রশ্ন করো বা ধাঁধা দাও যা তার বুদ্ধি বাড়াবে, এবং উত্তর দেবার জন্য তাকে উৎসাহ দাও।"
                "STORY" -> "[বিশেষ মোড: গল্পের ছলে শেখা] বিষয়টিকে ৩-৪ লাইনের একটি ছোট্ট রোমাঞ্চকর ছড়া বা কাল্পনিক রূপকথার মতো গল্পের মাধ্যমে বোঝাও।"
                else -> ""
            }

            val finalUserPrompt = if (modeInstruction.isNotEmpty()) {
                "$modeInstruction\n\nশিক্ষার্থীর প্রশ্ন: $prompt"
            } else {
                prompt
            }

            // Construct JSON request body manually using org.json for absolute reliability
            val requestJson = JSONObject().apply {
                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
                })

                // Contents array with history + current prompt
                val contentsArray = JSONArray()

                // Include last 6 history items for context
                val recentHistory = history.takeLast(6)
                for ((sender, text) in recentHistory) {
                    val role = if (sender == "USER") "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().put(JSONObject().put("text", text)))
                    })
                }

                // Add current prompt
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", finalUserPrompt)))
                })

                put("contents", contentsArray)

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 800)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)

            val httpRequest = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(httpRequest).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e("GeminiRepository", "API Error (${response.code}): $responseStr")
                    return@withContext Result.failure(Exception("Gemini API error: HTTP ${response.code}"))
                }

                val responseJson = JSONObject(responseStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val partsArr = contentObj?.optJSONArray("parts")
                    if (partsArr != null && partsArr.length() > 0) {
                        val replyText = partsArr.getJSONObject(0).optString("text", "")
                        if (replyText.isNotBlank()) {
                            return@withContext Result.success(replyText)
                        }
                    }
                }
                return@withContext Result.failure(Exception("empty response from Mahim AI Tutor"))
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Failed to connect to Mahim AI Tutor", e)
            return@withContext Result.failure(e)
        }
    }
}
