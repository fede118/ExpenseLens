package com.section11.expenselens.data.dto.request

import androidx.annotation.Keep

@Keep
class GeminiRequestBody(
    val contents: List<Content>
) {
    @Keep
    data class Content(
        val parts: List<Part>
    ) {
        @Keep
        data class Part(
            val text: String
        ) {
            @Keep
            data class ContentResponse(
                val total: String
            )
        }
    }
}
