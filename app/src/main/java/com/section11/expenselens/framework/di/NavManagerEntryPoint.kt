package com.section11.expenselens.framework.di

import com.section11.expenselens.framework.navigation.NavigationManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NavManagerEntryPoint {
    fun getNavigationManager(): NavigationManager
}
