package com.section11.expenselens.data.mapper

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.section11.expenselens.data.dto.response.GeminiResponse
import com.section11.expenselens.data.dto.response.GeminiResponse.Candidate
import com.section11.expenselens.data.dto.response.GeminiResponse.Candidate.Content
import com.section11.expenselens.data.dto.response.GeminiResponse.Candidate.Content.Part
import com.section11.expenselens.domain.exceptions.ExpenseInformationNotFoundException
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GeminiResponseMapperTest {

    private val gsonMock: Gson = mock()

    private lateinit var mapper: GeminiResponseMapper

    @Before
    fun setup() {
        mapper = GeminiResponseMapper(gsonMock)
    }

    @Test
    fun `mapper calls gson fromJson with expected string value`() {
        val expectedString = "```json\n{\n  \"estimatedCategory\": groceries,\n  \"total\": \$125.01\n}\n```\n"
        val response = getGeminiResponse(expectedString)
        whenever(gsonMock.fromJson(anyString(), eq(SuggestedExpenseInformation::class.java)))
            .thenReturn(SuggestedExpenseInformation("$125.01", Category.GROCERIES))

        mapper.toExpenseInformation(response)

        verify(gsonMock).fromJson(anyString(), eq(SuggestedExpenseInformation::class.java))
    }

    @Test(expected = ExpenseInformationNotFoundException::class)
    fun `gson throws JsonSyntaxException should be caught as receiptInformation not found`() {
        val expectedString = "```json\n{\n  \"estimatedCategory\": groceries,\n  \"total\": \$125.01\n}\n```\n"
        val response = getGeminiResponse(expectedString)
        whenever(gsonMock.fromJson(anyString(), eq(SuggestedExpenseInformation::class.java)))
            .thenThrow(JsonSyntaxException("Invalid JSON"))

        mapper.toExpenseInformation(response)

        verify(gsonMock).fromJson(anyString(), eq(SuggestedExpenseInformation::class.java))
    }

    @Test(expected = ExpenseInformationNotFoundException::class)
    fun `mapper with invalid json throws exception`() {
        val response = getGeminiResponse("invalid json")

        mapper.toExpenseInformation(response)

        verify(gsonMock).fromJson(anyString(), eq(SuggestedExpenseInformation::class.java))
    }

    private fun getGeminiResponse(responseText: String): GeminiResponse {
        return GeminiResponse(
            listOf(Candidate(
                Content(listOf(
                    Part(responseText)
                ))
            ))
        )
    }
}
