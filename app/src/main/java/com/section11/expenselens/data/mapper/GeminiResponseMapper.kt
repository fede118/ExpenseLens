package com.section11.expenselens.data.mapper

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.section11.expenselens.data.dto.response.GeminiResponse
import com.section11.expenselens.domain.exceptions.ReceiptInformationNotFoundException
import com.section11.expenselens.domain.models.ReceiptInformation
import java.util.regex.Pattern
import javax.inject.Inject

private const val ERROR_PARSING_JSON = "Error parsing JSON"
private const val GEMINI_RESPONSE = "Gemini response: "

class GeminiResponseMapper @Inject constructor(private val gson: Gson) {

    @Suppress("SwallowedException") // TODO: I need to add a Result class to handle errors
    fun toReceiptInformation(geminiResponse: GeminiResponse): ReceiptInformation {
        val geminiResponseText = geminiResponse.candidates.first().content.parts.first().text

        val jsonRegex = Pattern.compile("```json\\n(.*)\\n```", Pattern.DOTALL) // Regex to find the JSON
        val matcher = jsonRegex.matcher(geminiResponseText)

        if (matcher.find()) {
            val jsonString = matcher.group(1)
            try {
                return gson.fromJson(jsonString, ReceiptInformation::class.java)

            } catch (e: JsonSyntaxException) {
                throw ReceiptInformationNotFoundException(ERROR_PARSING_JSON + e.message)
            }

        } else {
            throw ReceiptInformationNotFoundException(GEMINI_RESPONSE + geminiResponseText)
        }
    }
}


