package com.opticalshop.presentation.screens.checkout

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.opticalshop.BuildConfig
import com.opticalshop.presentation.components.OpticalButton
import com.opticalshop.presentation.components.OpticalTextField
import com.opticalshop.presentation.payment.RazorpayEventBus
import com.opticalshop.presentation.payment.RazorpayPaymentEvent
import com.razorpay.Checkout
import kotlin.math.roundToInt
import org.json.JSONObject
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val context = LocalContext.current

    if (state.isOrderPlaced) {
        OrderSuccessContent(onOrderSuccess)
        return
    }

    LaunchedEffect(Unit) {
        RazorpayEventBus.events.collect { event ->
            when (event) {
                is RazorpayPaymentEvent.Success -> viewModel.onRazorpayPaymentSuccess(event.paymentId)
                is RazorpayPaymentEvent.Failure -> viewModel.onRazorpayPaymentFailed(event.message)
            }
        }
    }

    LaunchedEffect(state.shouldLaunchRazorpay) {
        if (state.shouldLaunchRazorpay) {
            viewModel.onRazorpayFlowLaunched()
            val activity = context.findActivity()
            if (activity == null) {
                viewModel.onRazorpayPaymentFailed("Unable to start payment screen")
            } else {
                launchRazorpayCheckout(activity = activity, state = state, onFailure = viewModel::onRazorpayPaymentFailed)
            }
        }
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

            val paymentError = state.paymentError
            if (paymentError != null) {
                Text(
                    text = paymentError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
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

private fun launchRazorpayCheckout(
    activity: Activity,
    state: CheckoutState,
    onFailure: (String?) -> Unit
) {
    val keyId = BuildConfig.RAZORPAY_KEY_ID
    if (keyId.isBlank()) {
        onFailure("Razorpay key is missing. Add RAZORPAY_KEY_ID in your local.properties")
        return
    }

    runCatching {
        val checkout = Checkout().apply {
            setKeyID(keyId)
        }

        val amountInPaise = (state.totalAmount * 100).roundToInt().coerceAtLeast(100)
        val options = JSONObject().apply {
            put("name", "Optical Shop")
            put("description", "Order payment")
            put("currency", "INR")
            put("amount", amountInPaise)
            put("prefill", JSONObject().apply {
                put("name", state.fullName)
                put("contact", state.phoneNumber)
            })
            put("theme", JSONObject().apply {
                put("color", "#0E5A8A")
            })
        }

        checkout.open(activity, options)
    }.onFailure {
        onFailure(it.message ?: "Unable to start payment")
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
        listOf("Address" to CheckoutStep.ADDRESS, "Payment" to CheckoutStep.PAYMENT, "Summary" to CheckoutStep.SUMMARY)
            .forEachIndexed { index, pair ->
                StepIndicator(pair.first, currentStep.ordinal >= pair.second.ordinal)
                if (index < 2) {
                    HorizontalDivider(
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
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
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
                placeholder = "Full Name",
                isError = state.nameError != null,
                errorMessage = state.nameError
            )
        }
        item {
            OpticalTextField(
                value = state.phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                label = "",
                placeholder = "Phone Number",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = state.phoneError != null,
                errorMessage = state.phoneError
            )
        }
        item {
            OpticalTextField(
                value = state.streetAddress,
                onValueChange = viewModel::onAddressChange,
                label = "",
                placeholder = "Street Address",
                isError = state.streetError != null,
                errorMessage = state.streetError
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                OpticalTextField(
                    value = state.city,
                    onValueChange = viewModel::onCityChange,
                    label = "",
                    placeholder = "City",
                    modifier = Modifier.weight(1f),
                    isError = state.cityError != null,
                    errorMessage = state.cityError,
                    trailingIcon = if (state.isFetchingCity) {
                        { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) }
                    } else null
                )
                OpticalTextField(
                    value = state.pincode,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 6) viewModel.onPincodeChange(it) },
                    label = "",
                    placeholder = "Pincode",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.pincodeError != null,
                    errorMessage = state.pincodeError
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
            subtitle = "Pay when your order arrives",
            isSelected = state.paymentMethod == "COD",
            onClick = { viewModel.onPaymentMethodChange("COD") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        PaymentOption(
            title = "Razorpay (UPI, Cards, Wallets)",
            subtitle = if (state.isRazorpayPaymentSuccessful) "Payment complete" else "Secure online payment",
            isSelected = state.paymentMethod == "RAZORPAY",
            onClick = { viewModel.onPaymentMethodChange("RAZORPAY") }
        )
    }
}

@Composable
fun PaymentOption(title: String, subtitle: String = "", isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                if (subtitle.isNotEmpty()) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
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
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (state.paymentMethod == "RAZORPAY") {
                        Text("Razorpay", color = Color.Gray)
                        if (state.razorpayPaymentId.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Payment ID: ${state.razorpayPaymentId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    } else {
                        Text("Cash on Delivery", color = Color.Gray)
                    }
                }
            }
        }
        item {
            Column {
                Text("Item Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                state.cartItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.productName} (x${item.quantity})", modifier = Modifier.weight(1f), color = Color.Gray)
                        Text("Rs ${String.format("%.2f", item.price * item.quantity)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSuccessContent(onOrderSuccess: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
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
        Text("Order Placed!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Your order has been placed successfully.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        OpticalButton(text = "Back to Exploration", onClick = onOrderSuccess)
    }
}
