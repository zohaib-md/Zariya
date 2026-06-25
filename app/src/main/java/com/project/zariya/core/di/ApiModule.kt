package com.project.zariya.core.di

import com.project.zariya.feature.interaction.data.remote.OpenFdaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideOpenFdaApi(retrofit: Retrofit): OpenFdaApi {
        return retrofit.create(OpenFdaApi::class.java)
    }
}
