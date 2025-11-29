package com.cpen321.squadup.ui.screens

import Button
import Icon
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cpen321.squadup.R
import com.cpen321.squadup.data.remote.api.RetrofitClient
import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.data.remote.dto.TransitType
import com.cpen321.squadup.data.remote.dto.User
import com.cpen321.squadup.ui.components.AddressPicker
import com.cpen321.squadup.ui.components.ImagePicker
import com.cpen321.squadup.ui.components.MessageSnackbar
import com.cpen321.squadup.ui.components.MessageSnackbarState
import com.cpen321.squadup.ui.theme.LocalSpacing
import com.cpen321.squadup.ui.viewmodels.AddressPickerViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileUiState
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel

private data class ProfileFormState(
    val name: String = "",
    val originalName: String = "",
    val email: String = "",
    val transitType: TransitType? = null,
    val originalTransitType: TransitType? = null,
    val address: Address? = null,
    val originalAddress: Address? = null,
) {
    fun hasChanges(): Boolean =
        (name.isNotBlank() && name != originalName) ||
            (transitType != originalTransitType) ||
            (address != originalAddress)
}

private data class ManageProfileScreenActions(
    val onBackClick: () -> Unit,
    val onNameChange: (String) -> Unit,
    val onTransitChange: (TransitType?) -> Unit,
    val onAddressChange: (Address?) -> Unit,
    val onEditPictureClick: () -> Unit,
    val onSaveClick: () -> Unit,
    val onImagePickerDismiss: () -> Unit,
    val onImageSelected: (Uri) -> Unit,
    val onLoadingPhotoChange: (Boolean) -> Unit,
    val onSuccessMessageShown: () -> Unit,
    val onErrorMessageShown: () -> Unit,
)

private data class ProfileFormData(
    val user: User,
    val formState: ProfileFormState,
    val isLoadingPhoto: Boolean,
    val isSavingProfile: Boolean,
    val onNameChange: (String) -> Unit,
    val onTransitChange: (TransitType?) -> Unit,
    val onAddressChange: (Address?) -> Unit,
    val onEditPictureClick: () -> Unit,
    val onSaveClick: () -> Unit,
    val onLoadingPhotoChange: (Boolean) -> Unit,
)

private data class ProfileBodyData(
    val uiState: ProfileUiState,
    val formState: ProfileFormState,
    val onNameChange: (String) -> Unit,
    val onTransitChange: (TransitType?) -> Unit,
    val onAddressChange: (Address?) -> Unit,
    val onEditPictureClick: () -> Unit,
    val onSaveClick: () -> Unit,
    val onLoadingPhotoChange: (Boolean) -> Unit,
)

private data class ProfileFieldsData(
    val name: String,
    val email: String,
    val onNameChange: (String) -> Unit,
    val transitType: TransitType?,
    val address: Address?,
    val onTransitChange: (TransitType?) -> Unit,
    val onAddressChange: (Address?) -> Unit,
)

@Composable
private fun initializeProfileFormState(
    user: User?,
    onFormStateChange: (ProfileFormState) -> Unit,
) {
    LaunchedEffect(user) {
        user?.let {
            onFormStateChange(
                ProfileFormState(
                    name = it.name,
                    originalName = it.name,
                    email = it.email,
                    address = it.address,
                    originalAddress = it.address,
                    transitType = it.transitType,
                    originalTransitType = it.transitType,
                ),
            )
        }
    }
}

@Composable
private fun setupProfileScreenEffects(
    uiState: ProfileUiState,
    profileViewModel: ProfileViewModel,
) {
    LaunchedEffect(Unit) {
        profileViewModel.clearSuccessMessage()
        profileViewModel.clearError()
        if (uiState.user == null) {
            profileViewModel.loadProfile()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            profileViewModel.loadProfile()
        }
    }
}

@Composable
private fun createManageProfileActions(
    profileViewModel: ProfileViewModel,
    formState: ProfileFormState,
    showImagePickerDialog: Boolean,
    onBackClick: () -> Unit,
    onFormStateChange: (ProfileFormState) -> Unit,
    onShowImagePickerChange: (Boolean) -> Unit,
): ManageProfileScreenActions =
    ManageProfileScreenActions(
        onBackClick = onBackClick,
        onNameChange = { onFormStateChange(formState.copy(name = it)) },
        onAddressChange = { newAddress ->
            onFormStateChange(formState.copy(address = newAddress))
        },
        onTransitChange = { newTransit ->
            onFormStateChange(formState.copy(transitType = newTransit))
        },
        onEditPictureClick = { onShowImagePickerChange(true) },
        onSaveClick = {
            profileViewModel.updateProfile(formState.name, formState.address, formState.transitType)
        },
        onImagePickerDismiss = { onShowImagePickerChange(false) },
        onImageSelected = { uri ->
            onShowImagePickerChange(false)
            profileViewModel.uploadProfilePicture(uri)
        },
        onLoadingPhotoChange = profileViewModel::setLoadingPhoto,
        onSuccessMessageShown = profileViewModel::clearSuccessMessage,
        onErrorMessageShown = profileViewModel::clearError,
    )

@Composable
fun ManageProfileScreen(
    profileViewModel: ProfileViewModel,
    onBackClick: () -> Unit,
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var formState by remember { mutableStateOf(ProfileFormState()) }

    setupProfileScreenEffects(uiState, profileViewModel)
    initializeProfileFormState(uiState.user) { formState = it }

    val actions =
        createManageProfileActions(
            profileViewModel = profileViewModel,
            formState = formState,
            showImagePickerDialog = showImagePickerDialog,
            onBackClick = onBackClick,
            onFormStateChange = { formState = it },
            onShowImagePickerChange = { showImagePickerDialog = it },
        )

    ManageProfileContent(
        uiState = uiState,
        formState = formState,
        snackBarHostState = snackBarHostState,
        showImagePickerDialog = showImagePickerDialog,
        actions = actions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageProfileContent(
    uiState: ProfileUiState,
    formState: ProfileFormState,
    snackBarHostState: SnackbarHostState,
    showImagePickerDialog: Boolean,
    actions: ManageProfileScreenActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProfileTopBar(onBackClick = actions.onBackClick)
        },
        snackbarHost = {
            MessageSnackbar(
                hostState = snackBarHostState,
                messageState =
                    MessageSnackbarState(
                        successMessage = uiState.successMessage,
                        errorMessage = uiState.errorMessage,
                        onSuccessMessageShown = actions.onSuccessMessageShown,
                        onErrorMessageShown = actions.onErrorMessageShown,
                    ),
            )
        },
    ) { paddingValues ->
        ProfileBody(
            paddingValues = paddingValues,
            data =
                ProfileBodyData(
                    uiState = uiState,
                    formState = formState,
                    onNameChange = actions.onNameChange,
                    onAddressChange = actions.onAddressChange,
                    onTransitChange = actions.onTransitChange,
                    onEditPictureClick = actions.onEditPictureClick,
                    onSaveClick = actions.onSaveClick,
                    onLoadingPhotoChange = actions.onLoadingPhotoChange,
                ),
        )
    }

    if (showImagePickerDialog) {
        ImagePicker(
            onDismiss = actions.onImagePickerDismiss,
            onImageSelected = actions.onImageSelected,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.manage_profile),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(name = R.drawable.ic_arrow_back)
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
    )
}

@Composable
private fun ProfileBody(
    paddingValues: PaddingValues,
    data: ProfileBodyData,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        when {
            data.uiState.isLoadingProfile -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            data.uiState.user != null -> {
                ProfileForm(
                    data =
                        ProfileFormData(
                            user = data.uiState.user,
                            formState = data.formState,
                            isLoadingPhoto = data.uiState.isLoadingPhoto,
                            isSavingProfile = data.uiState.isSavingProfile,
                            onNameChange = data.onNameChange,
                            onAddressChange = data.onAddressChange,
                            onTransitChange = data.onTransitChange,
                            onEditPictureClick = data.onEditPictureClick,
                            onSaveClick = data.onSaveClick,
                            onLoadingPhotoChange = data.onLoadingPhotoChange,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ProfileForm(
    data: ProfileFormData,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(spacing.large)
                .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        ProfilePictureCard(
            profilePicture = data.user.profilePicture,
            isLoadingPhoto = data.isLoadingPhoto,
            onEditClick = data.onEditPictureClick,
            onLoadingChange = data.onLoadingPhotoChange,
        )

        ProfileFields(
            data =
                ProfileFieldsData(
                    name = data.formState.name,
                    email = data.user.email,
                    address = data.formState.address,
                    transitType = data.formState.transitType,
                    onNameChange = data.onNameChange,
                    onAddressChange = data.onAddressChange,
                    onTransitChange = data.onTransitChange,
                ),
        )

        SaveButton(
            isSaving = data.isSavingProfile,
            isEnabled = data.formState.hasChanges(),
            onClick = data.onSaveClick,
        )
    }
}

@Composable
private fun ProfilePictureCard(
    profilePicture: String,
    isLoadingPhoto: Boolean,
    onEditClick: () -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfilePictureWithEdit(
                profilePicture = profilePicture,
                isLoadingPhoto = isLoadingPhoto,
                onEditClick = onEditClick,
                onLoadingChange = onLoadingChange,
            )
        }
    }
}

@Composable
private fun ProfilePictureWithEdit(
    profilePicture: String,
    isLoadingPhoto: Boolean,
    onEditClick: () -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier.size(spacing.extraLarge5),
    ) {
        AsyncImage(
            model = RetrofitClient.getPictureUri(profilePicture),
            onLoading = { onLoadingChange(true) },
            onSuccess = { onLoadingChange(false) },
            onError = { onLoadingChange(false) },
            contentDescription = stringResource(R.string.profile_picture),
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
        )

        if (isLoadingPhoto) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(spacing.large),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
            }
        }

        IconButton(
            onClick = onEditClick,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(spacing.extraLarge)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
        ) {
            Icon(
                name = R.drawable.ic_edit,
                type = "light",
            )
        }
    }
}

@Composable
private fun ProfileFields(
    data: ProfileFieldsData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = data.name,
            onValueChange = data.onNameChange,
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = data.email,
            onValueChange = { /* Read-only */ },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = false,
        )
        // address field
        val addressPickerViewModel: AddressPickerViewModel = hiltViewModel()
        AddressPicker(
            viewModel = addressPickerViewModel,
            initialValue = data.address,
            onAddressSelected = { selected ->
                data.onAddressChange(selected)
            },
        )

        // transitType field

        TransitTypeDropdown(
            selectedType = data.transitType,
            onTypeSelected = data.onTransitChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitTypeDropdown(
    selectedType: TransitType?,
    onTypeSelected: (TransitType) -> Unit,
) {
    val transitOptions = TransitType.entries.toList()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedType) {
        Log.d("TransitTypeDropdown", "Selected type changed to: $selectedType (name: ${selectedType?.name})")
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedType?.name?.replaceFirstChar { it.uppercase() } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.transit_type)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier =
                Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            transitOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name.replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onTypeSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SaveButton(
    isSaving: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Button(
        onClick = onClick,
        enabled = !isSaving && isEnabled,
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(spacing.medium),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.height(spacing.small))
        }
        Text(
            text = stringResource(if (isSaving) R.string.saving else R.string.save),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
