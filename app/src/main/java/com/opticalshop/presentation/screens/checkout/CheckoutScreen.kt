package com.opticalshop.presentation.screens.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.opticalshop.presentation.components.OpticalButton
import com.opticalshop.presentation.components.OpticalTextField

// UPI apps shown in the gateway
private val UPI_APPS = listOf(
    UpiApp("GPay", "G", Color(0xFF4285F4)),
    UpiApp("PhonePe", "P", Color(0xFF5F259F)),
    UpiApp("Paytm", "T", Color(0xFF00BAF2)),
    UpiApp("BHIM", "B", Color(0xFF006DB8))
)

data class UpiApp(val name: String, val initial: String, val color: Color)

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

    // UPI Payment Gateway Dialog
    if (state.showPaymentGateway) {
        UpiPaymentGatewayDialog(state = state, viewModel = viewModel)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            "Rs ${String.format("%.2f", state.totalAmount)}",
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

// ── UPI Payment Gateway Dialog ────────────────────────────────────────────────

@Composable
fun UpiPaymentGatewayDialog(state: CheckoutState, viewModel: CheckoutViewModel) {
    Dialog(
        onDismissRequest = { if (!state.isPaymentProcessing) viewModel.dismissPaymentGateway() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                when {
                    state.isPaymentSuccess -> UpiPaymentSuccessContent()
                    state.isPaymentProcessing -> UpiPaymentProcessingContent()
                    else -> UpiEntryContent(state = state, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun UpiEntryContent(state: CheckoutState, viewModel: CheckoutViewModel) {
    Column {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF006DB8)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("₹", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("UPI Payment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Powered by DummyPay™", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Amount banner
        Surface(
            color = Color(0xFF006DB8).copy(alpha = 0.08f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Paying to OpticShop", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(
                    "₹${String.format("%.2f", state.totalAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006DB8)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // UPI App selector
        Text("Pay using", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UPI_APPS.forEach { app ->
                val isSelected = state.selectedUpiApp == app.name
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) app.color else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .background(if (isSelected) app.color.copy(alpha = 0.06f) else Color.Transparent)
                        .clickable { viewModel.onUpiAppSelect(app.name) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = app.color
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(app.initial, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(app.name, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider with OR
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("  or enter UPI ID  ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // UPI ID input
        OutlinedTextField(
            value = state.upiId,
            onValueChange = viewModel::onUpiIdChange,
            label = { Text("UPI ID") },
            placeholder = { Text("yourname@upi") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        if (state.paymentError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.paymentError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pay button
        Button(
            onClick = { viewModel.processUpiPayment() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006DB8))
        ) {
            Text("Pay ₹${String.format("%.2f", state.totalAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { viewModel.dismissPaymentGateway() }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "🔒 256-bit SSL encrypted · Demo only",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UpiPaymentProcessingContent() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp), strokeWidth = 5.dp, color = Color(0xFF006DB8))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Processing UPI Payment…", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Waiting for bank confirmation", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Please do not close or go back", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun UpiPaymentSuccessContent() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = true, enter = scaleIn() + fadeIn()) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = Color(0xFF22C55E).copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(52.dp), tint = Color(0xFF22C55E))
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Payment Successful!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
        Spacer(modifier = Modifier.height(6.dp))
        Text("Redirecting to order summary…", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

// ── Checkout Stepper ──────────────────────────────────────────────────────────

@Composable
fun CheckoutStepper(currentStep: CheckoutStep) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Address" to CheckoutStep.ADDRESS, "Payment" to CheckoutStep.PAYMENT, "Summary" to CheckoutStep.SUMMARY)
            .forEachIndexed { index, pair ->
                StepIndicator(pair.first, currentStep.ordinal >= pair.second.ordinal)
                if (index < 2) {
                    HorizontalDivider(
                        modifier = Modifier.width(40.dp).padding(bottom = 16.dp),
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
                if (isActive) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}

// ── Address Step ──────────────────────────────────────────────────────────────

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
                value = state.fullName, onValueChange = viewModel::onFullNameChange,
                label = "", placeholder = "Full Name",
                isError = state.nameError != null, errorMessage = state.nameError
            )
        }
        item {
            OpticalTextField(
                value = state.phoneNumber, onValueChange = viewModel::onPhoneChange,
                label = "", placeholder = "Phone Number",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = state.phoneError != null, errorMessage = state.phoneError
            )
        }
        item {
            OpticalTextField(
                value = state.streetAddress, onValueChange = viewModel::onAddressChange,
                label = "", placeholder = "Street Address",
                isError = state.streetError != null, errorMessage = state.streetError
            )
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                OpticalTextField(
                    value = state.city, onValueChange = viewModel::onCityChange,
                    label = "", placeholder = "City", modifier = Modifier.weight(1f),
                    isError = state.cityError != null, errorMessage = state.cityError
                )
                OpticalTextField(
                    value = state.pincode,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 6) viewModel.onPincodeChange(it) },
                    label = "", placeholder = "Pincode", modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.pincodeError != null, errorMessage = state.pincodeError
                )
            }
        }
        item { OpticalTextField(value = state.landmark, onValueChange = viewModel::onLandmarkChange, label = "", placeholder = "Landmark (Optional)") }
    }
}

// ── Payment Step ──────────────────────────────────────────────────────────────

@Composable
fun PaymentStep(viewModel: CheckoutViewModel) {
    val state = viewModel.state.value
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select Payment Method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        PaymentOption(title = "Cash on Delivery", subtitle = "Pay when your order arrives", isSelected = state.paymentMethod == "COD", onClick = { viewModel.onPaymentMethodChange("COD") })
        Spacer(modifier = Modifier.height(16.dp))
        PaymentOption(title = "UPI / Digital Payment", subtitle = "GPay, PhonePe, Paytm, BHIM & more", isSelected = state.paymentMethod == "UPI", onClick = { viewModel.onPaymentMethodChange("UPI") })
    }
}

@Composable
fun PaymentOption(title: String, subtitle: String = "", isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                if (subtitle.isNotEmpty()) Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
        }
    }
}

// ── Summary Step ──────────────────────────────────────────────────────────────

@Composable
fun SummaryStep(viewModel: CheckoutViewModel) {
    val state = viewModel.state.value
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = MaterialTheme.shapes.medium) {
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (state.paymentMethod == "UPI") "UPI / Digital Payment" else "Cash on Delivery", color = Color.Gray)
                }
            }
        }
        item {
            Column {
                Text("Item Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                state.cartItems.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.productName} (x${item.quantity})", modifier = Modifier.weight(1f), color = Color.Gray)
                        Text("Rs ${String.format("%.2f", item.price * item.quantity)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Order Success ─────────────────────────────────────────────────────────────

@Composable
fun OrderSuccessContent(onOrderSuccess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(modifier = Modifier.size(120.dp), shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Order Placed!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Your order has been placed successfully.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(48.dp))
        OpticalButton(text = "Back to Exploration", onClick = onOrderSuccess)
    }
}
