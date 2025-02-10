package com.section11.expenselens.framework.di

import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.home.mapper.HomeScreenUiMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class MappersModule {

    @Provides
    fun provideHomeUiMapper(resourceProvider: ResourceProvider): HomeScreenUiMapper {
        return HomeScreenUiMapper(resourceProvider)
    }
}
