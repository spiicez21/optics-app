package com.opticalshop.presentation.screens.complete_profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.opticalshop.presentation.components.OpticalButton
import com.opticalshop.presentation.components.OpticalTextField
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CompleteProfileScreen(
    onProfileComplete: () -> Unit,
    viewModel: CompleteProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is CompleteProfileViewModel.UiEvent.NavigateToHome  -> onProfileComplete()
                is CompleteProfileViewModel.UiEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Welcome icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👋", style = MaterialTheme.typography.displaySmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (state.displayName.isNotBlank()) "Welcome, ${state.displayName.split(" ").first()}!" else "Welcome!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Just a few details to complete your profile",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Phone number
            OpticalTextField(
                value = state.phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                label = "",
                placeholder = "Phone Number",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = if (state.phoneError != null) MaterialTheme.colorScheme.error else Color.Gray
                    )
                },
                isError = state.phoneError != null,
                errorMessage = state.phoneError
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Age
            OpticalTextField(
                value = state.age,
                onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 3) viewModel.onAgeChange(it) },
                label = "",
                placeholder = "Age",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (state.ageError != null) MaterialTheme.colorScheme.error else Color.Gray
                    )
                },
                isError = state.ageError != null,
                errorMessage = state.ageError
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Gender selection
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state.genderError != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Male", "Female", "Other").forEach { option ->
                        val isSelected = state.gender == option
                        val hasError   = state.genderError != null
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable { viewModel.onGenderChange(option) },
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                hasError   -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                else       -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            },
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            border = when {
                                isSelected -> null
                                hasError   -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                else       -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(option, style = MaterialTheme.typography.labelLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                if (state.genderError != null) {
                    Text(
                        text = state.genderError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            OpticalButton(
                text = "Continue",
                onClick = viewModel::saveAndContinue,
                isLoading = state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onProfileComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for now", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
