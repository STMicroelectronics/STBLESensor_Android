/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class LoginActivityResultContract(val automaticLoginButtonClick: Boolean?) :
    ActivityResultContract<String, AuthData>() {

    override fun createIntent(context: Context, input: String): Intent {
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra("PROVIDER", input)
            if (automaticLoginButtonClick != null) {
                if (automaticLoginButtonClick) {
                    putExtra("automaticClickLogin", true)
                }
            }
        }
        return intent
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): AuthData = when {
        resultCode != Activity.RESULT_OK ->
            AuthData("", "", "", "")

        else ->
            AuthData(
                intent?.getBundleExtra("AuthData")!!.getString("accessKey")!!,
                intent.getBundleExtra("AuthData")!!.getString("secretKey")!!,
                intent.getBundleExtra("AuthData")!!.getString("token")!!,
                intent.getBundleExtra("AuthData")!!.getString("expiration")!!
            )
    }
}