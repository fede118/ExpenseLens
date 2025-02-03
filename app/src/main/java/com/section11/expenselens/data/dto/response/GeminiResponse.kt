package com.section11.expenselens.data.dto.response

import androidx.annotation.Keep

@Keep
data class GeminiResponse(
    val candidates: List<Candidate>
) {
    @Keep
    data class Candidate(
        val content: Content
    ) {
        @Keep
        data class Content(
            val parts: List<Part>
        ) {
            @Keep
            data class Part(
                val text: String
            )
        }
    }
}
