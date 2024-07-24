/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.user_profiling.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.StTopBar
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6
import com.st.user_profiling.LevelProficiencyFragmentDirections
import com.st.user_profiling.ProfileViewModel
import com.st.user_profiling.R
import com.st.user_profiling.levels
import com.st.user_profiling.model.LevelProficiency
import com.st.user_profiling.model.RadioButtonItem

@Composable
fun LevelProficiencyScreen(
    viewModel: ProfileViewModel,
    navController: NavController
) {
    LevelProficiencyScreen(
        levels = levels,
        levelSelected = viewModel.levelSelected.value,
        onLevelSelected = { selectedLevel ->
            viewModel.levelSelected.value = selectedLevel
        },
        goToNext = {
            val directions =
                LevelProficiencyFragmentDirections.actionLevelProficiencyFragmentToProfileSelectionFragment()
            navController.navigate(directions = directions)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelProficiencyScreen(
    modifier: Modifier = Modifier,
    levelSelected: LevelProficiency,
    levels: List<RadioButtonItem<LevelProficiency>> = emptyList(),
    onLevelSelected: (LevelProficiency) -> Unit = { /** NOOP **/ },
    goToNext: () -> Unit = { /** NOOP **/ }
) {
    Scaffold(modifier = modifier, topBar = {
        StTopBar(
            title = stringResource(id = R.string.st_userProfiling_levelProficiency_titleScreen)
        )
    }) { contentPadding ->
        LevelProficiencyContentScreen(
            modifier = Modifier.padding(paddingValues = contentPadding),
            levels = levels,
            levelSelected = levelSelected,
            onLevelSelected = onLevelSelected,
            goToNext = goToNext
        )
    }
}

@Composable
fun LevelProficiencyContentScreen(
    modifier: Modifier = Modifier,
    levelSelected: LevelProficiency,
    levels: List<RadioButtonItem<LevelProficiency>> = emptyList(),
    onLevelSelected: (LevelProficiency) -> Unit = { /** NOOP **/ },
    goToNext: () -> Unit = { /** NOOP **/ }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(weight = 0.5f))

        Text(
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            text = stringResource(id = R.string.st_userProfiling_levelProficiency_title)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Text(
            style = MaterialTheme.typography.bodyMedium,
            color = Grey6,
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.st_userProfiling_levelProficiency_description)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        levels.forEachIndexed { index, level ->
            LevelListItem(
                level = level,
                isSelected = level.data == levelSelected
            ) {
                onLevelSelected(level.data)
            }

            if (index != levels.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(vertical = LocalDimensions.current.paddingNormal))
            }
        }

        Spacer(modifier = Modifier.weight(weight = 0.5f))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(weight = 1f))

            BlueMsButton(
                text = stringResource(id = R.string.st_userProfiling_levelProficiency_nextButton),
                onClick = goToNext
            )
        }
    }
}

@Composable
fun LevelListItem(
    modifier: Modifier = Modifier,
    level: RadioButtonItem<LevelProficiency>,
    isSelected: Boolean = false,
    onLevelSelected: () -> Unit = { /** NOOP **/ }
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                text = stringResource(level.name)
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

            Text(
                color = Grey6,
                style = MaterialTheme.typography.bodySmall,
                text = stringResource(id = level.description)
            )
        }

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))

        RadioButton(selected = isSelected, onClick = onLevelSelected)
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun LevelProficiencyScreenPreview() {
    PreviewBlueMSTheme {
        LevelProficiencyScreen(
            levels = levels,
            levelSelected = LevelProficiency.BEGINNER
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LevelListItemPreview() {
    PreviewBlueMSTheme {
        LevelListItem(
            level = RadioButtonItem(
                data = LevelProficiency.BEGINNER,
                name = R.string.st_userProfiling_levelProficiency_beginnerName,
                description = R.string.st_userProfiling_levelProficiency_beginnerDescription
            )
        )
    }
}
