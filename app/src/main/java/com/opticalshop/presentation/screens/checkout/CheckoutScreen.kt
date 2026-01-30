package com.opticalshop.presentation.screens.checkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.currentStep == CheckoutStep.ADDRESS) onNavigateBack()
                        else viewModel.previousStep()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Amount", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text(
                            "$${String.format("%.2f", state.totalAmount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    OpticalButton(
                        text = if (state.currentStep == CheckoutStep.SUMMARY) "Place Order" else "Next",
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier.width(180.dp),
                        isLoading = state.isLoading
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodySmall
                )
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
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            "Address" to CheckoutStep.ADDRESS,
            "Payment" to CheckoutStep.PAYMENT,
            "Summary" to CheckoutStep.SUMMARY
        ).forEachIndexed { index, pair ->
            StepIndicator(pair.first, currentStep.ordinal >= pair.second.ordinal)
            if (index < 2) {
                Divider(
                    modifier = Modifier
                        .width(40.dp)
                        .padding(bottom = 16.dp),
                    color = if (currentStep.ordinal > index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun StepIndicator(label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isActive) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun AddressStep(viewModel: CheckoutViewModel) {
    val state = viewModel.state.value
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            OpticalTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "",
                placeholder = "Full Name"
            )
        }
        item {
            OpticalTextField(
                value = state.phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                label = "",
                placeholder = "Phone Number"
            )
        }
        item {
            OpticalTextField(
                value = state.streetAddress,
                onValueChange = viewModel::onAddressChange,
                label = "",
                placeholder = "Street Address"
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OpticalTextField(
                    value = state.city,
                    onValueChange = viewModel::onCityChange,
                    label = "",
                    placeholder = "City",
                    modifier = Modifier.weight(1f)
                )
                OpticalTextField(
                    value = state.pincode,
                    onValueChange = viewModel::onPincodeChange,
                    label = "",
                    placeholder = "Pincode",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            OpticalTextField(
                value = state.landmark,
                onValueChange = viewModel::onLandmarkChange,
                label = "",
                placeholder = "Landmark (Optional)"
            )
        }
    }
}

@Composable
fun PaymentStep(viewModel: CheckoutViewModel) {
    val state = viewModel.state.value
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select Payment Method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        PaymentOption(
            title = "Cash on Delivery",
            isSelected = state.paymentMethod == "COD",
            onClick = { viewModel.onPaymentMethodChange("COD") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        PaymentOption(
            title = "UPI / Digital Payment",
            isSelected = state.paymentMethod == "UPI",
            onClick = { viewModel.onPaymentMethodChange("UPI") }
        )
    }
}

@Composable
fun PaymentOption(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun SummaryStep(viewModel: CheckoutViewModel) {
    val state = viewModel.state.value
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.fullName, fontWeight = FontWeight.Medium)
                    Text(state.streetAddress, color = Color.Gray)
                    Text("${state.city} - ${state.pincode}", color = Color.Gray)
                    Text("Phone: ${state.phoneNumber}", color = Color.Gray)
                }
            }
        }
        
        item {
            Column {
                Text("Item Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                state.cartItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.productName} (x${item.quantity})", modifier = Modifier.weight(1f), color = Color.Gray)
                        Text("$${String.format("%.2f", item.price * item.quantity)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSuccessContent(onOrderSuccess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Success!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Your order has been placed successfully.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        OpticalButton(text = "Back to Exploration", onClick = onOrderSuccess)
    }
}
