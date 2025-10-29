package com.cpen321.squadup.di

import com.cpen321.squadup.data.remote.api.GroupInterface
import com.cpen321.squadup.data.repository.AuthRepository
import com.cpen321.squadup.data.repository.AuthRepositoryImpl
import com.cpen321.squadup.data.repository.GroupRepository
import com.cpen321.squadup.data.repository.GroupRepositoryImpl
import com.cpen321.squadup.data.repository.ProfileRepository
import com.cpen321.squadup.data.repository.ProfileRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.cpen321.squadup.data.local.preferences.TokenManager
import com.cpen321.squadup.data.remote.api.ActivityInterface

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository {
        return authRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository {
        return profileRepositoryImpl
    }

    // Add this for GroupRepository
    @Provides
    @Singleton
    fun provideGroupRepository(
        groupInterface: GroupInterface,
        activityInterface: ActivityInterface,
        tokenManager: TokenManager
    ): GroupRepository {
        return GroupRepositoryImpl(groupInterface,activityInterface, tokenManager)
    }
}