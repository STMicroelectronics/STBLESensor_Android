/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login.loginprovider

import android.app.Activity
import android.content.Context

class LoginProviderFactory {

    enum class LoginProviderType {
        VESPUCCI_PROD,
        VESPUCCI_DEV,
        COGNITO,
        KEYCLOAK,
        PREDMNT
    }

    companion object {
        fun getLoginProvider(
            activity: Activity,
            ctx: Context,
            loginProviderType: LoginProviderType,
            configuration: com.st.login.Configuration
        ): ILoginProvider {
            return when (loginProviderType) {
                LoginProviderType.VESPUCCI_DEV -> VespucciLoginProvider(
                    activity,
                    ctx,
                    configuration
                )

                LoginProviderType.VESPUCCI_PROD -> VespucciLoginProvider(
                    activity,
                    ctx,
                    configuration
                )

                LoginProviderType.COGNITO -> CognitoLoginProvider(activity, ctx, configuration)
                LoginProviderType.KEYCLOAK -> KeycloakLoginProvider(activity, ctx, configuration)
                LoginProviderType.PREDMNT -> PredMntLoginProvider(activity, ctx, configuration)
            }

        }
    }

}