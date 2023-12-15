/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.user_profiling.model

enum class ProfileType(
    val permissions: List<AuthorizedActions>
) {
    AI_DEVELOPER(
        permissions = AuthorizedActions.values().toList()
    ),
    DEVELOPER(
        permissions = AuthorizedActions.values().toList()
    ),
    STUDENT(
        permissions = AuthorizedActions.values().toList()
    ),
    SALES(
        permissions = listOf(
            AuthorizedActions.EXPLORE_CATALOG,
        )
    ),
    OTHER(
        permissions = emptyList()
    );

    fun isAuthorizedTo(permission: AuthorizedActions) = permissions.contains(permission)

    companion object {
        fun fromString(value: String) = ProfileType.values().firstOrNull { it.name == value }
    }
}
