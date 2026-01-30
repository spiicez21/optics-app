package com.opticalshop.presentation.screens.personal_info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.opticalshop.presentation.components.OpticalButton
import com.opticalshop.presentation.components.OpticalTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    onNavigateBack: () -> Unit,
    viewModel: PersonalInfoViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            snackbarHostState.showSnackbar("Profile updated successfully")
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Personal Information", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OpticalTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Full Name",
                        placeholder = "Enter your full name"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OpticalTextField(
                        value = state.email,
                        onValueChange = {},
                        label = "Email Address",
                        enabled = false,
                        placeholder = ""
                    )
                    Text(
                        text = "Email cannot be changed",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OpticalTextField(
                        value = state.phoneNumber,
                        onValueChange = viewModel::onPhoneChange,
                        label = "Phone Number",
                        placeholder = "Enter your phone number"
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    OpticalButton(
                        text = if (state.isUpdating) "Updating..." else "Save Changes",
                        onClick = viewModel::updateProfile,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isUpdating
                    )
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
                )
            }
        }
    }
}
