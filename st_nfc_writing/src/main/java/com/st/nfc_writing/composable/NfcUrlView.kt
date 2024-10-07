package com.st.nfc_writing.composable

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.st.blue_sdk.features.extended.json_nfc.request.JsonCommand
import com.st.nfc_writing.NfcWritingViewModel
import com.st.nfc_writing.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey3
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.Shapes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcUrlView(
    modifier: Modifier = Modifier,
    viewModel: NfcWritingViewModel,
    nodeId: String,
    expanded: Boolean
) {
    val context = LocalContext.current

    val keyboardController = LocalSoftwareKeyboardController.current

    var isExpanded by remember { mutableStateOf(expanded) }

    val supportedUrlTypes = JsonCommand.UrlTypeString.keys.toList()

    var selectedUrlType by remember { mutableStateOf(value = supportedUrlTypes[0]) }

    var internalTextValue by remember { mutableStateOf(value = "") }

    var expandedDropDownMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
        ) {

            //Header
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    LocalDimensions.current.paddingNormal
                )
            ) {

                Icon(
                    modifier = Modifier.size(size = LocalDimensions.current.iconNormal),
                    painter = painterResource(R.drawable.link_fields),
                    tint = PrimaryBlue,
                    contentDescription = null
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = LocalDimensions.current.paddingNormal)
                        .weight(2f),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    text = "URL"
                )

                Icon(
                    modifier = Modifier.size(size = LocalDimensions.current.iconNormal),
                    painter = if (isExpanded) {
                        painterResource(R.drawable.ic_collaps_view)
                    } else {
                        painterResource(R.drawable.ic_expand_view)
                    },
                    tint = PrimaryBlue,
                    contentDescription = null
                )
            }

            //Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingNormal
                        ),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                ) {

                    HorizontalDivider(
                        //modifier = Modifier.padding(top = LocalDimensions.current.paddingNormal),
                        thickness = 2.dp,
                        color = Grey3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                    ) {

                        Text(
                            modifier = Modifier.weight(1f),
                            text = "URL Type"
                        )

                        ExposedDropdownMenuBox(
                            modifier = Modifier.weight(1f),
                            expanded = expandedDropDownMenu,
                            onExpandedChange = { newValue ->
                                expandedDropDownMenu = newValue
                            }
                        ) {
                            OutlinedTextField(
                                value = selectedUrlType,
                                onValueChange = {},
                                readOnly = true,
                                //textStyle = MaterialTheme.typography.bodySmall,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropDownMenu)
                                },
                                colors = OutlinedTextFieldDefaults.colors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedDropDownMenu,
                                onDismissRequest = {
                                    expandedDropDownMenu = false
                                },
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                supportedUrlTypes.forEach {
                                    DropdownMenuItem(
                                        onClick = {
                                            selectedUrlType = it
                                            expandedDropDownMenu = false
                                        },
                                        text = {
                                            Text(
                                                text = it,
                                                //style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        value = internalTextValue,
                        onValueChange = {
                            internalTextValue = it
                        },
                        isError = false,
                        label = { Text(text = "Insert URL") }
                    )

                    BlueMsButton(
                        text = "Write to NFC",
                        onClick = {

                            keyboardController?.hide()

                            val commandUrl = JsonCommand(
                                NFCURL = selectedUrlType + internalTextValue
                            )

                            Log.i("NfcWritingUrl", "${commandUrl.NFCURL}")

                            viewModel.writeJsonCommand(nodeId = nodeId, command = commandUrl)

                            Toast.makeText(
                                context,
                                "NDEF Record Written on NFC",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        iconPainter = painterResource(id = R.drawable.send_to_nfc)
                    )
                }
            }
        }
    }
}