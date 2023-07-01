/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.user_profiling.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.composables.StTopBar
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6
import com.st.user_profiling.*
import com.st.user_profiling.R
import com.st.user_profiling.model.ProfileType
import com.st.user_profiling.model.RadioButtonItem

@Composable
fun ProfileSelectionScreen(
    viewModel: ProfileViewModel,
    navController: NavController
) {
    ProfileSelectionScreen(
        profileTypes = profileTypes,
        profileTypeSelected = viewModel.profileTypeSelected.value,
        onProfileTypeSelected = {
            viewModel.profileTypeSelected.value = it
        },
        onSelectionDone = {
            StUserProfilingConfig.defaultProfileType = viewModel.profileTypeSelected.value
            StUserProfilingConfig.defaultLevelProficiency = viewModel.levelSelected.value
            StUserProfilingConfig.onDone(
                viewModel.levelSelected.value,
                viewModel.profileTypeSelected.value
            )
        },
        goBack = {
            navController.popBackStack()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSelectionScreen(
    modifier: Modifier = Modifier,
    profileTypes: List<RadioButtonItem<ProfileType>> = emptyList(),
    profileTypeSelected: ProfileType,
    onProfileTypeSelected: (ProfileType) -> Unit = { /** NOOP **/ },
    onSelectionDone: () -> Unit = { /** NOOP **/ },
    goBack: () -> Unit = { /** NOOP **/ }
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            StTopBar(
                title = stringResource(id = R.string.st_userProfiling_profileSelection_titleScreen)
            )
        }
    ) { contentPadding ->
        ProfileSelectionContentScreen(
            modifier = Modifier.padding(paddingValues = contentPadding),
            profileTypes = profileTypes,
            profileTypeSelected = profileTypeSelected,
            onProfileTypeSelected = onProfileTypeSelected,
            onSelectionDone = onSelectionDone,
            goBack = goBack
        )
    }
}

@Composable
fun ProfileSelectionContentScreen(
    modifier: Modifier = Modifier,
    profileTypeSelected: ProfileType,
    profileTypes: List<RadioButtonItem<ProfileType>> = emptyList(),
    onProfileTypeSelected: (ProfileType) -> Unit = { /** NOOP **/ },
    onSelectionDone: () -> Unit = { /** NOOP **/ },
    goBack: () -> Unit = { /** NOOP **/ }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(weight = 0.5f))

        Text(
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            text = stringResource(id = R.string.st_userProfiling_profileSelection_title)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Text(
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Grey6,
            text = stringResource(id = R.string.st_userProfiling_profileSelection_description)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        profileTypes.forEachIndexed { index, type ->
            ProfileTypeListItem(
                type = type,
                isSelected = profileTypeSelected == type.data
            ) {
                onProfileTypeSelected(type.data)
            }

            if (index != levels.lastIndex) {
                Divider(
                    modifier = Modifier.padding(
                        vertical = LocalDimensions.current.paddingNormal
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(weight = 0.5f))

        Row(modifier = Modifier.fillMaxWidth()) {
            BlueMsButtonOutlined(
                text = stringResource(id = R.string.st_userProfiling_profileSelection_backButton),
                onClick = goBack
            )

            Spacer(modifier = Modifier.weight(weight = 1f))

            BlueMsButton(
                text = stringResource(id = R.string.st_userProfiling_profileSelection_doneButton),
                onClick = onSelectionDone
            )
        }
    }
}

@Composable
fun ProfileTypeListItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    type: RadioButtonItem<ProfileType>,
    onProfileTypeSelected: () -> Unit = { /**NOOP**/ }
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        type.image?.let { imageLevel ->
            Image(
                modifier = Modifier.size(size = LocalDimensions.current.imageNormal),
                painter = painterResource(id = imageLevel),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

        Column(
            modifier = Modifier.weight(weight = 1f)
        ) {
            Text(
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                text = stringResource(id = type.name)
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

            Text(
                color = Grey6,
                style = MaterialTheme.typography.bodySmall,
                text = stringResource(id = type.description)
            )
        }

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))

        RadioButton(
            selected = isSelected,
            onClick = onProfileTypeSelected
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun ProfileSelectionScreenPreview() {
    PreviewBlueMSTheme {
        ProfileSelectionScreen(
            profileTypes = profileTypes,
            profileTypeSelected = ProfileType.DEVELOPER
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileListItemPreview() {
    PreviewBlueMSTheme {
        ProfileTypeListItem(
            type = RadioButtonItem(
                data = ProfileType.DEVELOPER,
                name = R.string.st_userProfiling_profileSelection_developerName,
                description = R.string.st_userProfiling_profileSelection_developerDescription,
                image = R.drawable.developer
            )
        )
    }
}
