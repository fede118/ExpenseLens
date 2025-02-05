package com.section11.expenselens.data.repository

import com.google.gson.GsonBuilder
import com.section11.expenselens.data.dto.request.GeminiRequestBody
import com.section11.expenselens.data.dto.request.GeminiRequestBody.Content
import com.section11.expenselens.data.mapper.GeminiResponseMapper
import com.section11.expenselens.data.service.GeminiService
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ExpenseInformation
import com.section11.expenselens.domain.repository.ExpenseInfoExtractorRepository
import com.section11.expenselens.framework.networking.safeApiCall
import javax.inject.Inject

class GeminiAiRepository @Inject constructor(
    private val service: GeminiService,
    private val apiKey: String,
    private val geminiResponseMapper: GeminiResponseMapper
) : ExpenseInfoExtractorRepository {

    override suspend fun getExpenseInfo(text: String): ExpenseInformation {
        val requestBody = getGeminiRequestBody(EXPENSE_INFO_EXTRACTION_PROMPT + text)
        val response = safeApiCall {
            service.generateContent(requestBody, apiKey)
        }

        return geminiResponseMapper.toExpenseInformation(response)
    }

    private fun getGeminiRequestBody(text: String): GeminiRequestBody {
        return GeminiRequestBody(listOf(Content(listOf(Content.Part(text)))))
    }

    companion object {
        private const val TOTAL_EXAMPLE = "\$xxx.xx"
        private var jsonStructure: String = generateJsonStructure()
        private var categoriesToString = Category.getCategoryDescriptions()
        private var EXPENSE_INFO_EXTRACTION_PROMPT: String = """
            Following is text extracted from an image of a receipt or bill (maybe even a screenshot
            of a bank expense, etc. 
            I want you to reply ONLY with a json structured like this:
            ```
            $jsonStructure
            ```
            WHERE:
            - total: a representation of the total in that text including gratuity/tips if it applies.
            - category: OPTIONAL, the best estimation of the Category of the receipt. Based on the following categories:
            $categoriesToString.
            IMPORTANT: the category must be in lowercase
            If it doesn't match any category leave it as null
            
            Here is the INPUT text from the receipt: \n"
            """

        private fun generateJsonStructure(): String {
            val exampleInstance = ExpenseInformation(TOTAL_EXAMPLE, Category.GROCERIES)
            val gson = GsonBuilder().setPrettyPrinting().create()
            val json = gson.toJson(exampleInstance)

            return json
        }
    }
}
