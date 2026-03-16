package com.opticalshop.presentation.screens.auth.register

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.opticalshop.R
import com.opticalshop.presentation.components.OpticalButton
import com.opticalshop.presentation.components.OpticalTextField
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onCompleteProfile: () -> Unit = {},
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val name         = viewModel.name.value
    val email        = viewModel.email.value
    val password     = viewModel.password.value
    val phoneNumber  = viewModel.phoneNumber.value
    val gender       = viewModel.gender.value
    val age          = viewModel.age.value
    val state        = viewModel.state.value

    val nameError     = viewModel.nameError.value
    val emailError    = viewModel.emailError.value
    val passwordError = viewModel.passwordError.value
    val phoneError    = viewModel.phoneError.value
    val genderError   = viewModel.genderError.value
    val ageError      = viewModel.ageError.value

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                viewModel.onGoogleLogin(
                    idToken     = idToken,
                    displayName = account.displayName ?: "",
                    photoUrl    = account.photoUrl?.toString() ?: ""
                )
            } ?: Toast.makeText(context, "Google Sign-In: no ID token returned", Toast.LENGTH_LONG).show()
        } catch (e: ApiException) {
            if (e.statusCode == com.google.android.gms.common.api.CommonStatusCodes.CANCELED) {
                // silent
            } else {
                val hint = if (e.statusCode == 10) " (Add SHA-1 in Firebase Console)" else ""
                Toast.makeText(context, "Google Sign-In error ${e.statusCode}$hint", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is RegisterViewModel.UiEvent.NavigateToHome            -> onRegisterSuccess()
                is RegisterViewModel.UiEvent.NavigateToCompleteProfile -> onCompleteProfile()
                is RegisterViewModel.UiEvent.ShowError                 -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
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
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign up to start your journey",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Full Name
            OpticalTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = "",
                placeholder = "Full Name",
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = if (nameError != null) MaterialTheme.colorScheme.error else Color.Gray) },
                isError = nameError != null,
                errorMessage = nameError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OpticalTextField(
                value = email,
                onValueChange = viewModel::onEmailChange,
                label = "",
                placeholder = "Email Address",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (emailError != null) MaterialTheme.colorScheme.error else Color.Gray) },
                isError = emailError != null,
                errorMessage = emailError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone
            OpticalTextField(
                value = phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                label = "",
                placeholder = "Phone Number",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = if (phoneError != null) MaterialTheme.colorScheme.error else Color.Gray) },
                isError = phoneError != null,
                errorMessage = phoneError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Age + Gender row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Age
                Box(modifier = Modifier.weight(1.5f)) {
                    OpticalTextField(
                        value = age,
                        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 3) viewModel.onAgeChange(it) },
                        label = "",
                        placeholder = "Age",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = if (ageError != null) MaterialTheme.colorScheme.error else Color.Gray) },
                        isError = ageError != null,
                        errorMessage = ageError
                    )
                }

                // Gender
                Column(modifier = Modifier.weight(2f)) {
                    Row(
                        modifier = Modifier.height(52.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Male", "Female").forEach { genderOption ->
                            val isSelected = gender == genderOption
                            val hasError = genderError != null
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { viewModel.onGenderChange(genderOption) },
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    hasError   -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    else       -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                },
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                border = when {
                                    isSelected -> null
                                    hasError   -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                    else       -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(genderOption.take(1), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    if (genderError != null) {
                        Text(
                            text = genderError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OpticalTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = "",
                placeholder = "Password (min. 6 characters)",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (passwordError != null) MaterialTheme.colorScheme.error else Color.Gray) },
                isError = passwordError != null,
                errorMessage = passwordError
            )

            Spacer(modifier = Modifier.height(40.dp))

            OpticalButton(
                text = "Sign Up",
                onClick = viewModel::register,
                isLoading = state is com.opticalshop.domain.model.Result.Loading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue with Google", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(text = "Already have an account? ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
