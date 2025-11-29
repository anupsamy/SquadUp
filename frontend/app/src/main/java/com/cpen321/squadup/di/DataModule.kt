package com.cpen321.squadup.di

import android.content.Context
import com.cpen321.squadup.data.local.preferences.TokenManager
import com.cpen321.squadup.ui.navigation.NavigationStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context,
    ): TokenManager = TokenManager(context)

    @Provides
    @Singleton
    fun provideNavigationStateManager(): NavigationStateManager = NavigationStateManager()
}
