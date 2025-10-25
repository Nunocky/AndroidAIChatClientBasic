package org.nunocky.androidaichatclientbasic.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.nunocky.androidaichatclientbasic.datastore.DataStoreRepository
import org.nunocky.androidaichatclientbasic.datastore.PreferencesDataStoreImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStoreRepository(@ApplicationContext context: Context): DataStoreRepository {
        return PreferencesDataStoreImpl(context)
    }
}

