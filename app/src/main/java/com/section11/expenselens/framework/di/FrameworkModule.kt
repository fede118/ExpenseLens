package com.section11.expenselens.framework.di

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.framework.deserializer.CategoryDeserializer
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManagerImpl
import com.section11.expenselens.framework.utils.ExpenseLensImageCapture
import com.section11.expenselens.framework.utils.ImageCaptureWrapper
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.framework.utils.ResourceProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FrameworkModule {

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Category::class.java, CategoryDeserializer())
            .create()
    }

    @Provides
    fun providesExternalCacheDir(@ApplicationContext context: Context): File? {
        return context.externalCacheDir
    }

    @Provides
    fun providesMainExecutor(@ApplicationContext context: Context): Executor {
        return ContextCompat.getMainExecutor(context)
    }

    @Provides
    @Singleton
    fun provideNavigationManager(): NavigationManager {
        return NavigationManagerImpl()
    }

    @Provides
    @Singleton
    fun providesImageCapture(): ImageCapture {
        return ImageCapture.Builder().build()
    }

    @Provides
    fun providesImageCaptureWrapper(imageCapture: ImageCapture): ImageCaptureWrapper {
        return ExpenseLensImageCapture(imageCapture)
    }

    @Provides
    fun provideTextRecognizer(): TextRecognizer {
        return TextRecognition.getClient(
            TextRecognizerOptions.Builder()
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
        )
    }

    @Provides
    fun provideResourceProvider(@ApplicationContext context: Context): ResourceProvider {
        return ResourceProviderImpl(context)
    }

    @Provides
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
