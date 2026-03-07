package com.opticalshop.di

import android.content.Context
import com.opticalshop.OpticalShopApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): OpticalShopApp {
        return app as OpticalShopApp
    }

    /** Long-lived scope tied to the application process lifetime. */
    @Singleton
    @Provides
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        val handler = CoroutineExceptionHandler { _, throwable ->
            android.util.Log.e("AppScope", "Unhandled coroutine exception", throwable)
        }
        return CoroutineScope(SupervisorJob() + Dispatchers.Default + handler)
    }
}
