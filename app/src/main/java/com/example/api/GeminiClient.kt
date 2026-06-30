package com.example.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Compress to JPEG with 80% quality to fit within Gemini's limits and reduce payload size
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    data class CivicAnalysis(
        val category: String, // pothole, water_leak, streetlight, waste, drainage, other
        val severity: Int,    // 1-5
        val description: String
    )

    suspend fun analyzeCivicIssue(
        bitmap: Bitmap,
        userDescription: String
    ): CivicAnalysis = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        Log.d(TAG, "API Key: $apiKey")

        // 1. Graceful Local Fallback if API Key is empty or placeholder
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.w(TAG, "No valid Gemini API key found. Using smart local analyzer fallback.")
            return@withContext runLocalFallback(userDescription)
        }

        try {
            val imageBase64 = bitmap.toBase64()
            val prompt = """
                You are an expert civil engineering inspector and civic issue analyzer.
                Analyze this photo representing an urban infrastructure problem in an Indian city (such as Jaipur).
                
                Optional details from the reporter: "$userDescription"
                
                Please evaluate:
                1. Category: Must be EXACTLY one of: 'pothole', 'water_leak', 'streetlight', 'waste', 'drainage', or 'other'.
                2. Severity: Rate from 1 (minor cosmetic issue, e.g. littering) to 5 (extreme safety hazard/injury risk, e.g. deep pothole on major road, dark bridge flyover, broken water mains flooding streets).
                3. Description: Write a brief, polite, action-oriented public description of what is seen in the image, blending user notes if helpful. Under 150 characters.
                
                Return a RAW, clean JSON response with keys: "category" (string), "severity" (integer), and "description" (string).
                Do NOT warp the JSON in markdown blocks like ```json or ```. Return pure JSON.
            """.trimIndent()

            // Construct payload with manual JSONObject to bypass serialization reflection bugs
            val requestJson = JSONObject().apply {
                val contentsArray = org.json.JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = org.json.JSONArray().apply {
                            val textPart = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(textPart)

                            val imagePart = JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", imageBase64)
                                }
                                put("inlineData", inlineData)
                            }
                            put(imagePart)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                // Enforce JSON format in generation config
                val config = JSONObject().apply {
                    val format = JSONObject().apply {
                        put("mimeType", "application/json")
                    }
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", config)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini API Request failed: ${response.code} - $errBody")
                    return@withContext runLocalFallback(userDescription)
                }

                val responseString = response.body?.string()
                if (responseString.isNullOrEmpty()) {
                    Log.e(TAG, "Empty response body from Gemini API")
                    return@withContext runLocalFallback(userDescription)
                }

                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                val text = firstPart?.optString("text")

                if (text.isNullOrEmpty()) {
                    Log.e(TAG, "No text found in candidate parts")
                    return@withContext runLocalFallback(userDescription)
                }

                val parsedText = text.trim()
                val jsonStartIndex = parsedText.indexOf("{")
                val jsonEndIndex = parsedText.lastIndexOf("}")
                val cleanJson = if (jsonStartIndex != -1 && jsonEndIndex != -1 && jsonEndIndex > jsonStartIndex) {
                    parsedText.substring(jsonStartIndex, jsonEndIndex + 1)
                } else {
                    parsedText
                }

                val resObj = JSONObject(cleanJson)
                val category = resObj.optString("category", "other").lowercase()
                val severity = resObj.optInt("severity", 3)
                val description = resObj.optString("description", userDescription.ifEmpty { "Civic issue reported for resolution." })

                val validatedCategory = if (category in listOf("pothole", "water_leak", "streetlight", "waste", "drainage", "other")) {
                    category
                } else {
                    "other"
                }

                Log.d(TAG, "Successfully analyzed issue: Category=$validatedCategory, Severity=$severity")
                CivicAnalysis(validatedCategory, severity.coerceIn(1, 5), description)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            runLocalFallback(userDescription)
        }
    }

    private fun runLocalFallback(userDescription: String): CivicAnalysis {
        val textLower = userDescription.lowercase()
        val category = when {
            textLower.contains("pothole") || textLower.contains("road") || textLower.contains("ditch") -> "pothole"
            textLower.contains("leak") || textLower.contains("water") || textLower.contains("pipe") -> "water_leak"
            textLower.contains("light") || textLower.contains("lamp") || textLower.contains("bulb") || textLower.contains("dark") -> "streetlight"
            textLower.contains("garbage") || textLower.contains("waste") || textLower.contains("pile") || textLower.contains("trash") -> "waste"
            textLower.contains("drain") || textLower.contains("sewer") || textLower.contains("clog") -> "drainage"
            else -> "other"
        }
        val severity = when {
            textLower.contains("dangerous") || textLower.contains("accident") || textLower.contains("injury") || textLower.contains("severe") -> 5
            textLower.contains("major") || textLower.contains("bad") -> 4
            textLower.contains("small") || textLower.contains("minor") -> 2
            else -> 3
        }
        val desc = userDescription.ifEmpty {
            when (category) {
                "pothole" -> "Pothole detected on the road surface."
                "water_leak" -> "Water leakage detected from piping."
                "streetlight" -> "Non-functional streetlight reported."
                "waste" -> "Piles of waste or litter requiring cleanup."
                "drainage" -> "Drainage congestion or overflow."
                else -> "Civic concern reported."
            }
        }
        return CivicAnalysis(category, severity, desc)
    }
}
