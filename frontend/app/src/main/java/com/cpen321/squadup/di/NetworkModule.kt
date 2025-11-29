package com.cpen321.squadup.di

import com.cpen321.squadup.data.remote.api.ActivityInterface
import com.cpen321.squadup.data.remote.api.AuthInterface
import com.cpen321.squadup.data.remote.api.GroupInterface
import com.cpen321.squadup.data.remote.api.ImageInterface
import com.cpen321.squadup.data.remote.api.RetrofitClient
import com.cpen321.squadup.data.remote.api.UserInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideAuthService(): AuthInterface = RetrofitClient.authInterface

    @Provides
    @Singleton
    fun provideUserService(): UserInterface = RetrofitClient.userInterface

    @Provides
    @Singleton
    fun provideGroupInterface(): GroupInterface = RetrofitClient.groupInterface

    @Provides
    @Singleton
    fun provideMediaService(): ImageInterface = RetrofitClient.imageInterface

    @Provides
    @Singleton
    fun provideActivityService(): ActivityInterface = RetrofitClient.activityInterface
}
