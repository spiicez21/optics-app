package com.opticalshop.presentation.screens.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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
        if (state.isAddressAdded) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Address") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
        ) {
            OpticalTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Full Name"
            )
            Spacer(modifier = Modifier.height(12.dp))
            OpticalTextField(
                value = state.phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                label = "Phone Number"
            )
            Spacer(modifier = Modifier.height(12.dp))
            OpticalTextField(
                value = state.streetAddress,
                onValueChange = viewModel::onAddressChange,
                label = "Street Address"
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                OpticalTextField(
                    value = state.city,
                    onValueChange = viewModel::onCityChange,
                    label = "City",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OpticalTextField(
                    value = state.pincode,
                    onValueChange = viewModel::onPincodeChange,
                    label = "Pincode",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OpticalTextField(
                value = state.landmark,
                onValueChange = viewModel::onLandmarkChange,
                label = "Landmark (Optional)"
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Set as Default Address")
                Switch(checked = state.isDefault, onCheckedChange = viewModel::onDefaultChange)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OpticalButton(
                text = "Save Address",
                onClick = { viewModel.saveAddress() },
                isLoading = state.isLoading
            )
            
            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = state.error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
