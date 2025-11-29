package com.cpen321.squadup.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.ui.viewmodels.AddressPickerViewModel
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.launch

@Composable
fun AddressPicker(
    viewModel: AddressPickerViewModel,
    modifier: Modifier = Modifier,
    initialValue: Address? = null,
    onAddressSelected: (Address) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Initialize query from initial value
    LaunchedEffect(initialValue) {
        initialValue?.let {
            viewModel.selectedAddress = it
            viewModel.query = it.formatted
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = viewModel.query,
            onValueChange = { query ->
                viewModel.onQueryChanged(query)
                expanded = query.isNotBlank()
            },
            label = { Text("Address") },
            trailingIcon = {
                if (viewModel.query.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.clear()
                        expanded = false
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (expanded && viewModel.predictions.isNotEmpty()) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .padding(top = 4.dp),
            ) {
                items(viewModel.predictions) { prediction ->
                    PredictionItem(prediction = prediction) {
                        scope.launch {
                            // Fetch full place details using placeId
                            val fullAddress = viewModel.fetchPlace(prediction.placeId)
                            if (fullAddress != null) {
                                viewModel.selectedAddress = fullAddress
                                viewModel.query = fullAddress.formatted
                                onAddressSelected(fullAddress)
                            }
                            expanded = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionItem(
    prediction: AutocompletePrediction,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(12.dp),
    ) {
        Text(
            text = prediction.getPrimaryText(null).toString(),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = prediction.getSecondaryText(null).toString(),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
