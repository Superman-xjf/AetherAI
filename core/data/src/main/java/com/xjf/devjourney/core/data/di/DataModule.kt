package com.xjf.devjourney.core.data.di

import com.xjf.devjourney.core.data.DevJourneyRepository
import com.xjf.devjourney.core.data.FakeDevJourneyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindsDevJourneyRepository(
        repository: FakeDevJourneyRepository,
    ): DevJourneyRepository
}
