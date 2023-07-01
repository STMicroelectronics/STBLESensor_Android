/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.preferences.di

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.st.preferences.StPreferences
import com.st.preferences.StPreferencesImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [StPreferencesModule.WithProvides::class])
@InstallIn(SingletonComponent::class)
abstract class StPreferencesModule {

    @Binds
    abstract fun bindStPreferences(stPreferences: StPreferencesImpl): StPreferences

    @Module
    @InstallIn(SingletonComponent::class)
    object WithProvides {
        private val TAG = StPreferencesModule::class.java.simpleName

        private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "CoroutineExceptionHandler got", exception)
        }

        private val coroutineScope: CoroutineScope =
            CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)

        @Singleton
        @Provides
        @PreferencesScope
        fun provideCoroutineScope(): CoroutineScope = coroutineScope

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "st-prefs"
        )

        @Singleton
        @Provides
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.dataStore
        }
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PreferencesScope
