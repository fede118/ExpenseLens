package com.section11.expenselens.data.mapper

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.section11.expenselens.data.dto.response.GeminiResponse
import com.section11.expenselens.domain.exceptions.ExpenseInformationNotFoundException
import com.section11.expenselens.domain.models.ExpenseInformation
import java.util.regex.Pattern
import javax.inject.Inject

private const val ERROR_PARSING_JSON = "Error parsing JSON"
private const val GEMINI_RESPONSE = "Gemini response: "

class GeminiResponseMapper @Inject constructor(private val gson: Gson) {

    @Suppress("SwallowedException") // TODO: I need to add a Result class to handle errors
    fun toExpenseInformation(geminiResponse: GeminiResponse): ExpenseInformation {
        val geminiResponseText = geminiResponse.candidates.first().content.parts.first().text

        val jsonRegex = Pattern.compile("```json\\n(.*)\\n```", Pattern.DOTALL) // Regex to find the JSON
        val matcher = jsonRegex.matcher(geminiResponseText)

        if (matcher.find()) {
            val jsonString = matcher.group(1)
            try {
                return gson.fromJson(jsonString, ExpenseInformation::class.java)

            } catch (e: JsonSyntaxException) {
                throw ExpenseInformationNotFoundException(ERROR_PARSING_JSON + e.message)
            }

        } else {
            throw ExpenseInformationNotFoundException(GEMINI_RESPONSE + geminiResponseText)
        }
    }
}


