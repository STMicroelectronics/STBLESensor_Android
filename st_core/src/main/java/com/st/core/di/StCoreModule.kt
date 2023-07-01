/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.core.di

import com.st.core.api.ApplicationAnalyticsService
import com.st.core.impl.ApplicationAnalyticsDefaultImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module(includes = [StCoreModule.WithProvides::class])
@InstallIn(SingletonComponent::class)
abstract class StCoreModule {

    @Binds
    @IntoSet
    abstract fun bindApplicationAnalyticsService(sampleService: ApplicationAnalyticsDefaultImpl): ApplicationAnalyticsService

    @Module
    @InstallIn(SingletonComponent::class)
    object WithProvides
}