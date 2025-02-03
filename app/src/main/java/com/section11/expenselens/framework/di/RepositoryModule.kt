package com.section11.expenselens.framework.di

import com.google.gson.Gson
import com.section11.expenselens.BuildConfig
import com.section11.expenselens.data.mapper.GeminiResponseMapper
import com.section11.expenselens.data.repository.GeminiAiRepository
import com.section11.expenselens.data.service.GeminiService
import com.section11.expenselens.domain.repository.ReceiptInfoExtractorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideGeminiAiRepository(retrofit: Retrofit, gson: Gson): ReceiptInfoExtractorRepository {
        return GeminiAiRepository(
            retrofit.create(GeminiService::class.java),
            BuildConfig.GEMINI_API_KEY,
            GeminiResponseMapper(gson)
        )
    }
}
