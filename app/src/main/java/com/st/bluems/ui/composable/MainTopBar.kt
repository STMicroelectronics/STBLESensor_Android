/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.st.bluems.BuildConfig
import com.st.bluems.R
import com.st.ui.composables.ActionItem
import com.st.ui.composables.BlueMsMenuActions
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    modifier: Modifier = Modifier,
    goToProfile: () -> Unit = { /** NOOP **/ },
    goToSourceCode: () -> Unit = { /** NOOP **/ },
    goToAboutST: () -> Unit = { /** NOOP **/ },
    goToPrivacyPolicy: () -> Unit = { /** NOOP **/ },
    readBetaCatalog: () -> Unit = { /** NOOP **/ },
    readReleaseCatalog: () -> Unit = { /** NOOP **/ },
    switchVersionBetaRelease: () -> Unit = { /** NOOP **/ },
    switchServerForced: () -> Unit = { /** NOOP **/ },
    isLoggedIn: Boolean = false,
    isExpert: Boolean = false,
    isServerForced: Boolean = false,
    isBetaRelease: Boolean = false,
    login: () -> Unit = { /** NOOP **/ },
    logout: () -> Unit = { /** NOOP **/ },
    onAddCatalogEntryFromFile: () -> Unit = { /** NOOP **/ }
) {
    val actions by rememberActions(
        isLoggedIn = isLoggedIn,
        isExpert = isExpert,
        isServerForced = isServerForced,
        login = login,
        logout = logout,
        goToProfile = goToProfile,
        goToAboutST = goToAboutST,
        goToPrivacyPolicy = goToPrivacyPolicy,
        goToSourceCode = goToSourceCode,
        onAddCatalogEntryFromFile = onAddCatalogEntryFromFile,
        switchServerForced = switchServerForced
    )

    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            Text(text = stringResource(id = R.string.st_home_deviceList_screenTitle))
        },
        actions = {
            BlueMsMenuActions(
                actions = actions,
                menuIcon = Icons.Default.ManageAccounts
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Divider()

                    if (isBetaRelease) {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME} ${BuildConfig.VERSION_CODE} [Beta]",
                            modifier = Modifier.clickable {
                                switchVersionBetaRelease()
                            })

                        Divider(modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal))

                        Text(text = "Read Beta Catalog", modifier = Modifier.clickable {
                            readBetaCatalog()
                        })

                        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

                        Text(text = "Read Release Catalog", modifier = Modifier.clickable {
                            readReleaseCatalog()
                        })
                    } else {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME} [Stable]",
                            modifier = Modifier.clickable {
                                switchVersionBetaRelease()
                            })
                    }

                    Divider()
                }
            }
        }
    )
}

@Composable
fun rememberActions(
    isLoggedIn: Boolean,
    isExpert: Boolean,
    isServerForced: Boolean,
    login: () -> Unit = { /** NOOP **/ },
    goToProfile: () -> Unit = { /** NOOP **/ },
    goToSourceCode: () -> Unit = { /** NOOP **/ },
    goToAboutST: () -> Unit = { /** NOOP **/ },
    goToPrivacyPolicy: () -> Unit = { /** NOOP **/ },
    logout: () -> Unit = { /** NOOP **/ },
    onAddCatalogEntryFromFile: () -> Unit = { /** NOOP **/ },
    switchServerForced: () -> Unit = { /** NOOP **/ }
): State<List<ActionItem>> {
    val context = LocalContext.current
    return remember(key1 = isLoggedIn, key2 = isExpert, key3 = isServerForced) {
        if (isExpert) {
            mutableStateOf(
                value =
                listOf(
                    ActionItem(
                        label = context.getString(R.string.st_home_menuActions_addCatalog),
                        action = onAddCatalogEntryFromFile
                    ),
                    ActionItem(
                        label = context.getString(R.string.st_home_menuActions_profile),
                        action = goToProfile
                    ),
                    ActionItem(
                        label = context.getString(R.string.st_home_menuActions_privacy),
                        action = goToPrivacyPolicy
                    ),
                    ActionItem(
                        label = context.getString(R.string.st_home_menuActions_appSourceCode),
                        action = goToSourceCode
                    ),
                    ActionItem(
                        label = context.getString(R.string.st_home_menuActions_about),
                        action = goToAboutST
                    ),
                    ActionItem(
                        label = if (isLoggedIn) {
                            context.getString(R.string.st_home_menuActions_logout)
                        } else {
                            context.getString(R.string.st_home_menuActions_login)
                        },
                        action = if (isLoggedIn) logout else login
                    ),
                    ActionItem(
                        label = if (isServerForced) {
                            context.getString(R.string.st_home_menuAction_no_force_server)
                        } else {
                            context.getString(R.string.st_home_menuAction_force_server)
                        },
                        action = switchServerForced
                    )
                )
            )
        } else {
            mutableStateOf(
                value =
                listOf(
                    ActionItem(
                        label = context.getString(R.string.st_home_menuActions_profile),
                        action = goToProfile
                    ),
                    ActionItem(
                        label = context.getString(R.string.st_home_menuActions_privacy),
                        action = goToPrivacyPolicy
                    ),
                    ActionItem(
                        label = context.getString(R.string.st_home_menuActions_appSourceCode),
                        action = goToSourceCode
                    ),
                    ActionItem(
                        label = context.getString(R.string.st_home_menuActions_about),
                        action = goToAboutST
                    ),
                    ActionItem(
                        label = if (isLoggedIn) {
                            context.getString(R.string.st_home_menuActions_logout)
                        } else {
                            context.getString(R.string.st_home_menuActions_login)
                        },
                        action = if (isLoggedIn) logout else login
                    )
                )
            )
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun MainTopBarPreview() {
    PreviewBlueMSTheme {
        MainTopBar()
    }
}
