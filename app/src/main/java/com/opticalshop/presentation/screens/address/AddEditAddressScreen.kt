package com.opticalshop.presentation.screens.address

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.opticalshop.presentation.components.OpticalButton
import com.opticalshop.presentation.components.OpticalTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAddressScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddressViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    LaunchedEffect(state.isAddressAdded) {
        if (state.isAddressAdded) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Address") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OpticalTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Full Name",
                isError = state.nameError != null,
                errorMessage = state.nameError
            )

            OpticalTextField(
                value = state.phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                label = "Phone Number",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = state.phoneError != null,
                errorMessage = state.phoneError
            )

            OpticalTextField(
                value = state.streetAddress,
                onValueChange = viewModel::onAddressChange,
                label = "Street Address",
                isError = state.streetError != null,
                errorMessage = state.streetError
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                OpticalTextField(
                    value = state.city,
                    onValueChange = viewModel::onCityChange,
                    label = "City",
                    modifier = Modifier.weight(1f),
                    isError = state.cityError != null,
                    errorMessage = state.cityError
                )
                OpticalTextField(
                    value = state.pincode,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 6) viewModel.onPincodeChange(it) },
                    label = "Pincode",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.pincodeError != null,
                    errorMessage = state.pincodeError
                )
            }

            OpticalTextField(
                value = state.landmark,
                onValueChange = viewModel::onLandmarkChange,
                label = "Landmark (Optional)"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Set as Default Address")
                Switch(checked = state.isDefault, onCheckedChange = viewModel::onDefaultChange)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OpticalButton(
                text = "Save Address",
                onClick = { viewModel.saveAddress() },
                isLoading = state.isLoading
            )

            if (state.error != null) {
                Text(text = state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
