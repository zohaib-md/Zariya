package com.project.zariya.feature.interaction.di

import com.project.zariya.feature.interaction.data.repository.InteractionRepositoryImpl
import com.project.zariya.feature.interaction.domain.repository.InteractionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class InteractionModule {

    @Binds
    abstract fun bindInteractionRepository(
        interactionRepositoryImpl: InteractionRepositoryImpl
    ): InteractionRepository
}
