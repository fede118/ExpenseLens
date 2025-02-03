package com.section11.expenselens.framework.di

import com.section11.expenselens.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val DEFAULT_TIME_OUT = 15L
private const val WRITE_TIME_OUT = 45L

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.GEMINI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(provideOkHttpClient())
            .build()
    }
}
