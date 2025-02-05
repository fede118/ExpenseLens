package com.section11.expenselens.framework.di

import androidx.camera.core.ImageCapture
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ImageCaptureEntryPoint {
    fun getImageCapture(): ImageCapture
}
