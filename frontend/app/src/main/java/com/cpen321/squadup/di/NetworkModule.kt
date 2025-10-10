package com.cpen321.squadup.di

import com.cpen321.squadup.data.remote.api.AuthInterface
import com.cpen321.squadup.data.remote.api.HobbyInterface
import com.cpen321.squadup.data.remote.api.ImageInterface
import com.cpen321.squadup.data.remote.api.NewsInterface
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
    fun provideAuthService(): AuthInterface {
        return RetrofitClient.authInterface
    }

    @Provides
    @Singleton
    fun provideUserService(): UserInterface {
        return RetrofitClient.userInterface
    }

    @Provides
    @Singleton
    fun provideMediaService(): ImageInterface {
        return RetrofitClient.imageInterface
    }

    @Provides
    @Singleton
    fun provideHobbyService(): HobbyInterface {
        return RetrofitClient.hobbyInterface
    }

    @Provides
    @Singleton
    fun provideNewsService(): NewsInterface {
        return RetrofitClient.newsInterface
    }
}
