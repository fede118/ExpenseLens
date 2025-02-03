package com.section11.expenselens.data.service

import com.section11.expenselens.data.dto.request.GeminiRequestBody
import com.section11.expenselens.data.dto.response.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

private const val PATH = "v1beta/models/gemini-1.5-flash:generateContent"
private const val KEY = "key"

interface GeminiService {

    @POST(PATH)
    suspend fun generateContent(
        @Body request: GeminiRequestBody,
        @Query(KEY) key: String
    ): Response<GeminiResponse>
}
