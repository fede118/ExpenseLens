package com.section11.expenselens.framework.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.framework.deserializer.CategoryDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class FrameworkModule {

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Category::class.java, CategoryDeserializer())
            .create()
    }
}
