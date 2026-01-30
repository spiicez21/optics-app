package com.opticalshop.presentation.screens.checkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    if (state.isOrderPlaced) {
        OrderSuccessContent(onOrderSuccess)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.currentStep == CheckoutStep.ADDRESS) onNavigateBack()
                        else viewModel.previousStep()
                    }) {
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
        ) {
            CheckoutStepper(currentStep = state.currentStep)
            
            Box(modifier = Modifier.weight(1f)) {
                when (state.currentStep) {
                    CheckoutStep.ADDRESS -> AddressStep(viewModel)
                    CheckoutStep.PAYMENT -> PaymentStep(viewModel)
                    CheckoutStep.SUMMARY -> SummaryStep(viewModel)
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Surface(tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Amount", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "$${String.format("%.2f", state.totalAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    OpticalButton(
                        text = if (state.currentStep == CheckoutStep.SUMMARY) "Place Order" else "Next",
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier.width(150.dp),
                        isLoading = state.isLoading
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutStepper(currentStep: CheckoutStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepIndicator("Address", currentStep.ordinal >= 0)
        Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
        StepIndicator("Payment", currentStep.ordinal >= 1)
        Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
        StepIndicator("Summary", currentStep.ordinal >= 2)
    }
}

@Composable
fun StepIndicator(label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = MaterialTheme.shapes.extraSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ) {
            if (isActive) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun AddressStep(viewModel: CheckoutViewModel) {
    val state = viewModel.state.value
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
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
        }
    }
}

@Composable
fun PaymentStep(viewModel: CheckoutViewModel) {
    val state = viewModel.state.value
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select Payment Method", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { viewModel.onPaymentMethodChange("COD") }
        ) {
            RadioButton(selected = state.paymentMethod == "COD", onClick = { viewModel.onPaymentMethodChange("COD") })
            Text("Cash on Delivery")
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { viewModel.onPaymentMethodChange("UPI") }
        ) {
            RadioButton(selected = state.paymentMethod == "UPI", onClick = { viewModel.onPaymentMethodChange("UPI") })
            Text("UPI / Digital Payment")
        }
    }
}

@Composable
fun SummaryStep(viewModel: CheckoutViewModel) {
    val state = viewModel.state.value
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Delivery Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(state.fullName)
            Text(state.streetAddress)
            Text("${state.city} - ${state.pincode}")
            Text("Phone: ${state.phoneNumber}")
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            Text("Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(state.cartItems) { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${item.productName} (x${item.quantity})", modifier = Modifier.weight(1f))
                Text("$${item.price * item.quantity}")
            }
        }
    }
}

@Composable
fun OrderSuccessContent(onOrderSuccess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Order Placed Successfully!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Thank you for shopping with us.", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(48.dp))
        OpticalButton(text = "Go to Home", onClick = onOrderSuccess)
    }
}
