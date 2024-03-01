package com.st.bluems.di

import android.net.Uri
import com.st.bluems.BuildConfig
import com.st.bluems.util.ENVIRONMENT
import com.st.login.STLoginConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@InstallIn(SingletonComponent::class)
@Module(includes = [BlueMsModule.WithProvides::class])
abstract class BlueMsModule {

    @Module
    @InstallIn(SingletonComponent::class)
    object WithProvides {

        @Provides
        @IntoMap
        @StringKey("stLoginConfig")
        fun provideSTLoginConfig(): STLoginConfig {
            return STLoginConfig(
                Uri.parse("stblesensor://callback"),
                if (BuildConfig.VESPUCCI_ENVIRONMENT != ENVIRONMENT.DEV.name) {
                    com.st.login.R.raw.prod_auth_config_vespucci
                } else {
                    com.st.login.R.raw.dev_auth_config_vespucci
                },
                if (BuildConfig.VESPUCCI_ENVIRONMENT != ENVIRONMENT.DEV.name) {
                    Uri.parse("https://www.st.com/cas/logout?service=https%3A%2F%2Fvespucci.st.com%2Fsvc%2Fwebtomobile%2Fstblesensor")
                } else {
                    Uri.parse("")
                },
                BuildConfig.VESPUCCI_ENVIRONMENT == ENVIRONMENT.PROD.name
            )
        }
    }
}