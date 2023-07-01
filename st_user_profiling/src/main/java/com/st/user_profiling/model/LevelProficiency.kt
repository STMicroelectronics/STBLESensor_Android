/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.user_profiling.model

enum class LevelProficiency(val permissions: List<AuthorizedActions>) {
    BEGINNER(
        permissions = listOf(
            AuthorizedActions.UPDATE_FW,
            AuthorizedActions.SWAP_BANK
        )
    ),
    EXPERT(permissions = AuthorizedActions.values().toList());

    fun isAuthorizedTo(permission: AuthorizedActions) =
        permissions.contains(permission)

    companion object {
        fun fromString(value: String) = LevelProficiency.values().firstOrNull { it.name == value }
    }
}
