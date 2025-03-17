package com.section11.expenselens.framework.di

import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.history.mapper.ExpenseHistoryUiMapper
import com.section11.expenselens.ui.home.mapper.HomeScreenUiMapper
import com.section11.expenselens.ui.household.HouseholdDetailsUiMapper
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

    @Provides
    fun provideExpenseHistoryUiMapper(): ExpenseHistoryUiMapper {
        return ExpenseHistoryUiMapper()
    }

    @Provides
    fun provideHouseholdDetailsUiMapper(resourceProvider: ResourceProvider): HouseholdDetailsUiMapper {
        return HouseholdDetailsUiMapper(resourceProvider)
    }
}
