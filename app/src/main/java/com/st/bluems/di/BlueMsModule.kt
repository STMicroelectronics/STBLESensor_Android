package com.st.bluems.di

import android.net.Uri
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap

@InstallIn(SingletonComponent::class)
@Module(includes = [BlueMsModule.WithProvides::class])
abstract class BlueMsModule {

    @Module
    @InstallIn(SingletonComponent::class)
    object WithProvides {

        @Provides
        @IntoMap
        @IntKey(0)
        fun provideRedirectUri(): Uri {
            return Uri.parse("stblesensor://callback")
        }
    }
}