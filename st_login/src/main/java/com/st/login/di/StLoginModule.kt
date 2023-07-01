/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login.di

import android.net.Uri
import com.st.login.api.StLoginManager
import com.st.login.impl.StLoginManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module(includes = [StLoginModule.WithProvides::class])
abstract class StLoginModule {

    @Binds
    abstract fun bindStLoginManager(stLoginManager: StLoginManagerImpl): StLoginManager

    @Module
    @InstallIn(SingletonComponent::class)
    object WithProvides {

        @Provides
        @RedirectUri
        fun provideRedirectUris(
            availableRedirectUris: Map<Int, @JvmSuppressWildcards Uri>
        ): Uri {
            // Choose the available client from the options provided.
            val bestEntry = availableRedirectUris.maxBy { it.key }
            return checkNotNull(bestEntry.value) { "No RedirectUri were provided" }
        }
    }
}