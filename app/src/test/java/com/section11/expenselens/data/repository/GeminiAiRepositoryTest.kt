package com.section11.expenselens.data.repository

import com.section11.expenselens.data.dto.response.GeminiResponse
import com.section11.expenselens.data.dto.response.GeminiResponse.Candidate
import com.section11.expenselens.data.dto.response.GeminiResponse.Candidate.Content
import com.section11.expenselens.data.dto.response.GeminiResponse.Candidate.Content.Part
import com.section11.expenselens.data.mapper.GeminiResponseMapper
import com.section11.expenselens.data.service.GeminiService
import com.section11.expenselens.domain.exceptions.ApiErrorException
import com.section11.expenselens.domain.exceptions.ResponseBodyNullException
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

class GeminiAiRepositoryTest {

    private val mockService: GeminiService = mock()
    private val apiKey = "apiKey"
    private val geminiResponseMapper: GeminiResponseMapper = mock()

    private lateinit var repository: GeminiAiRepository

    @Before
    fun setup() {
        repository = GeminiAiRepository(mockService, apiKey, geminiResponseMapper)
    }

    @Test
    fun `when service is called then mapper should be called with the response`() = runTest {
        val extractedText = "some text"
        val mockResponse: Response<GeminiResponse> = mock()
        val expectedResponse = getGeminiResponse("some response")
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.body()).thenReturn(expectedResponse)
        whenever(mockService.generateContent(any(), any())).thenReturn(mockResponse)

        repository.getExpenseInfo(extractedText)

        verify(geminiResponseMapper).toExpenseInformation(expectedResponse)
    }

    @Test(expected = ApiErrorException::class)
    fun `when service fails then should throw exception`() = runTest {
        val extractedText = "someText"
        val mockResponse: Response<GeminiResponse> = mock()
        whenever(mockResponse.isSuccessful).thenReturn(false)
        whenever(mockResponse.code()).thenReturn(400)
        whenever(mockService.generateContent(any(), any())).thenReturn(mockResponse)

        repository.getExpenseInfo(extractedText)
    }

    @Test(expected = ResponseBodyNullException::class)
    fun `when service returns null body then should throw exception`() = runTest {
        val extractedText = "someText"
        val mockResponse: Response<GeminiResponse> = mock()
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.body()).thenReturn(null)
        whenever(mockService.generateContent(any(), any())).thenReturn(mockResponse)

        repository.getExpenseInfo(extractedText)
    }

    private fun getGeminiResponse(responseText: String): GeminiResponse {
        return GeminiResponse(
            listOf(
                Candidate(
                Content(listOf(
                    Part(responseText)
                ))
            )
            )
        )
    }
}
