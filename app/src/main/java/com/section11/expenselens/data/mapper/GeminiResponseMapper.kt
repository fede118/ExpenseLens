package com.section11.expenselens.data.mapper

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.section11.expenselens.data.dto.response.GeminiResponse
import com.section11.expenselens.domain.exceptions.ExpenseInformationNotFoundException
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

private const val ERROR_PARSING_JSON = "Error parsing JSON"
private const val GEMINI_RESPONSE = "Gemini response: "

class GeminiResponseMapper @Inject constructor(private val gson: Gson) {

    fun toExpenseInformation(geminiResponse: GeminiResponse): Result<SuggestedExpenseInformation> {
        val geminiResponseText = geminiResponse.candidates.first().content.parts.first().text

        val jsonRegex = Pattern.compile("```json\\n(.*)\\n```", Pattern.DOTALL) // Regex to find the JSON
        val matcher = jsonRegex.matcher(geminiResponseText)

        return if (matcher.find()) {
            val jsonString = matcher.group(1)
            try {
                success(gson.fromJson(jsonString, SuggestedExpenseInformation::class.java))
            } catch (e: JsonSyntaxException) {
                failure(ExpenseInformationNotFoundException(ERROR_PARSING_JSON + e.message))
            }
        } else {
            failure(ExpenseInformationNotFoundException(GEMINI_RESPONSE + geminiResponseText))
        }
    }
}
