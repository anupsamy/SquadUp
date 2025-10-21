package com.cpen321.squadup.ui.screens

import Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cpen321.squadup.R
import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.data.remote.dto.TransitType
import com.cpen321.squadup.ui.components.MessageSnackbar
import com.cpen321.squadup.ui.components.MessageSnackbarState
import com.cpen321.squadup.ui.viewmodels.ProfileUiState
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import com.cpen321.squadup.ui.theme.LocalFontSizes
import com.cpen321.squadup.ui.theme.LocalSpacing

private data class ProfileCompletionFormState(
    val address: Address? = null,
    val transitType: TransitType? = null,
    val hasSavedProfile: Boolean = false
) {
    //fun canSave(): Boolean = address != null && transitType != null
    fun canSave(): Boolean = transitType != null
}

private data class ProfileCompletionScreenData(
    val formState: ProfileCompletionFormState,
    val isSavingProfile: Boolean,
//    val onAddressChange: (Address?) -> Unit,
    val onTransitTypeChange: (TransitType?) -> Unit,
    val onSkipClick: () -> Unit,
    val onSaveClick: () -> Unit
)

private data class ProfileCompletionScreenContentData(
    val uiState: ProfileUiState,
    val formState: ProfileCompletionFormState,
    val snackBarHostState: SnackbarHostState,
//    val onAddressChange: (Address?) -> Unit,
    val onTransitTypeChange: (TransitType?) -> Unit,
    val onSkipClick: () -> Unit,
    val onSaveClick: () -> Unit,
    val onErrorMessageShown: () -> Unit
)

@Composable
fun ProfileCompletionScreen(
    profileViewModel: ProfileViewModel,
    onProfileCompleted: () -> Unit,
    onProfileCompletedWithMessage: (String) -> Unit = { onProfileCompleted() }
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val successfulUpdateMessage = stringResource(R.string.successful_profile_update)

    // Form state
    var formState by remember {
        mutableStateOf(ProfileCompletionFormState())
    }

    // Side effects
    LaunchedEffect(Unit) {
        if (uiState.user == null) {
            profileViewModel.loadProfile()
        }
    }

    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            // Auto-complete if user already has transitType filled
            if ( //user.address != null &&
                user.transitType != null &&
                !formState.hasSavedProfile) {
                onProfileCompleted()
            }
        }
    }

    ProfileCompletionContent(
        data = ProfileCompletionScreenContentData(
            uiState = uiState,
            formState = formState,
            snackBarHostState = snackBarHostState,
//            onAddressChange = { newAddress: Address? ->
//                formState = formState.copy(address = newAddress)
//            },

            onTransitTypeChange = { formState = formState.copy(transitType = it) },
            onSkipClick = onProfileCompleted,
            onSaveClick = {
                if (formState.canSave()) {
                    formState = formState.copy(hasSavedProfile = true)
                    profileViewModel.updateProfile(
                        name = uiState.user?.name ?: "",
                        address = formState.address,
                        transitType = formState.transitType,
                        onSuccess = {
                            onProfileCompletedWithMessage(successfulUpdateMessage)
                        }
                    )
                }
            },
            onErrorMessageShown = profileViewModel::clearError
        )
    )
}

@Composable
private fun ProfileCompletionContent(
    data: ProfileCompletionScreenContentData,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            MessageSnackbar(
                hostState = data.snackBarHostState,
                messageState = MessageSnackbarState(
                    successMessage = null,
                    errorMessage = data.uiState.errorMessage,
                    onSuccessMessageShown = { },
                    onErrorMessageShown = data.onErrorMessageShown
                )
            )
        }
    ) { paddingValues ->
        ProfileCompletionBody(
            paddingValues = paddingValues,
            data = ProfileCompletionScreenData(
                formState = data.formState,
                isSavingProfile = data.uiState.isSavingProfile,
//                onAddressChange = data.onAddressChange,
                onTransitTypeChange = data.onTransitTypeChange,
                onSkipClick = data.onSkipClick,
                onSaveClick = data.onSaveClick
            )
        )
    }
}

@Composable
private fun ProfileCompletionBody(
    paddingValues: PaddingValues,
    data: ProfileCompletionScreenData,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ProfileCompletionHeader()

        Spacer(modifier = Modifier.height(spacing.extraLarge2))

//        val addressPickerViewModel: AddressPickerViewModel = hiltViewModel()
//        AddressPicker(
//            viewModel = addressPickerViewModel,
//            initialValue = data.formState.address,
//            onAddressSelected = { address ->
//                // Save to your form state or call backend
//                data.onAddressChange
//            }
//        )

//        Spacer(modifier = Modifier.height(spacing.medium))

        TransitTypeInputField(
            transitType = data.formState.transitType,
            isEnabled = !data.isSavingProfile,
            onTransitTypeChange = data.onTransitTypeChange
        )

        Spacer(modifier = Modifier.height(spacing.extraLarge))

        ActionButtons(
            isSavingProfile = data.isSavingProfile,
            isSaveEnabled = data.formState.canSave(),
            onSkipClick = data.onSkipClick,
            onSaveClick = data.onSaveClick
        )
    }
}

@Composable
private fun ProfileCompletionHeader(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WelcomeTitle()

        Spacer(modifier = Modifier.height(spacing.medium))

        ProfileDescription()
    }
}

@Composable
private fun WelcomeTitle(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.complete_profile),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
private fun ProfileDescription(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.profile_description),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
//legacy just basic text box replaced with AddressPicker.kt
//
//@Composable
//private fun AddressInputField(
//    addressText: String,
//    isEnabled: Boolean,
//    onAddressChange: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val spacing = LocalSpacing.current
//
//    OutlinedTextField(
//        value = addressText,
//        onValueChange = onAddressChange,
//        label = { Text(stringResource(R.string.address)) },
//        placeholder = { Text(stringResource(R.string.address_placeholder)) },
//        modifier = modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(spacing.medium),
//        enabled = isEnabled,
//        singleLine = true
//    )
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransitTypeInputField(
    transitType: TransitType?,
    isEnabled: Boolean,
    onTransitTypeChange: (TransitType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val transitOptions = TransitType.entries.toList() // Use enum
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled,
        onExpandedChange = { if (isEnabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = transitType?.name?.replaceFirstChar { it.uppercase() } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.transit_type)) },
            placeholder = { Text(stringResource(R.string.transit_type_placeholder)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = isEnabled
        )

        ExposedDropdownMenu(
            expanded = expanded && isEnabled,
            onDismissRequest = { expanded = false }
        ) {
            transitOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name.replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onTransitTypeChange(option) // pass enum, not string
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
private fun ActionButtons(
    isSavingProfile: Boolean,
    isSaveEnabled: Boolean,
    onSkipClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        SkipButton(
            isEnabled = !isSavingProfile,
            onClick = onSkipClick,
            modifier = Modifier.weight(1f)
        )

        SaveButton(
            isSaving = isSavingProfile,
            isEnabled = isSaveEnabled && !isSavingProfile,
            onClick = onSaveClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SkipButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fontSizes = LocalFontSizes.current

    Box(modifier = modifier) {
        Button(
            type = "secondary",
            onClick = onClick,
            enabled = isEnabled
        ) {
            Text(
                text = stringResource(R.string.skip),
                fontSize = fontSizes.medium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SaveButton(
    isSaving: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            enabled = isEnabled
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(spacing.medium),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.save),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}