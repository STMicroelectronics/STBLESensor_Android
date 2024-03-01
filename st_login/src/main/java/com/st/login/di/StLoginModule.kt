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
        @LoginConfig
        fun provideSTLoginConfigs(
            provideSTLoginConfigs: Map<String, @JvmSuppressWildcards com.st.login.STLoginConfig>
        ): com.st.login.STLoginConfig {
            // Choose the available client from the options provided.
            val bestEntry = provideSTLoginConfigs["stLoginConfig"]
            return checkNotNull(bestEntry) { "No RedirectUri were provided" }
        }
    }
}