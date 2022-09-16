package de.inovex.pepper.intelligence.mlkit.system.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.inovex.pepper.intelligence.mlkit.pepper.PepperActions
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PepperActionsModule {

    @Provides
    @Singleton
    fun providePepperActions() = PepperActions()
}
