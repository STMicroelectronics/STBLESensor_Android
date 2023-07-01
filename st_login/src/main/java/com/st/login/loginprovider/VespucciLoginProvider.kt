package com.st.login.loginprovider

import android.app.Activity
import android.content.Context
import com.st.login.Configuration

class VespucciLoginProvider(
    activity: Activity,
    ctx: Context,
    configuration: Configuration
) : KeycloakLoginProvider(activity, ctx, configuration)