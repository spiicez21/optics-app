package com.opticalshop.presentation.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import coil.compose.AsyncImage
import com.opticalshop.presentation.components.OpticalButton
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.opticalshop.presentation.components.OpticalLottieAnimation
import com.opticalshop.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val product = state.product
    var showGallery by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (product != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.addToCart() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add To Cart", fontWeight = FontWeight.Bold)
                    }
                    OpticalButton(
                        text = "Buy Now",
                        onClick = { /* Buy Now logic */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (product != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Main Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = if (product.images.isNotEmpty()) product.images[state.selectedImageIndex] else null,
                        contentDescription = product.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                             .fillMaxSize()
                             .padding(32.dp)
                             .clickable { showGallery = true }
                    )
                    
                    IconButton(
                        onClick = { viewModel.toggleWishlist() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (state.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                            contentDescription = "Wishlist", 
                            tint = if (state.isWishlisted) Color.Red else Color.Gray
                        )
                    }
                }

                // Thumbnails
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(product.images) { index, imageUrl ->
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(60.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .border(
                                    width = 2.dp,
                                    color = if (state.selectedImageIndex == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .clickable {
                                    if (state.selectedImageIndex == index) {
                                         showGallery = true 
                                    } else {
                                         viewModel.onImageSelect(index) 
                                    }
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize().padding(8.dp)
                            )
                        }
                    }
                }

                // Info Section
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "$${product.price}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Size Selector
                    Text(text = "Select Size", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("S", "M", "L", "XL").forEach { size ->
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { viewModel.onSizeSelect(size) },
                                shape = MaterialTheme.shapes.medium,
                                color = if (state.selectedSize == size) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = if (state.selectedSize == size) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = size, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Quantity Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                                .padding(horizontal = 8.dp)
                        ) {
                            IconButton(onClick = { viewModel.updateQuantity(-1) }) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                            }
                            Text(text = "${state.quantity}", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.updateQuantity(1) }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text(text = "Description", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Lens Options
                    Text(text = "Lens Options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LensOptionSelector(
                        title = "Lens Type",
                        options = listOf("Single Vision", "Bifocal", "Progressive", "No Prescription"),
                        selectedOption = state.lensType,
                        onOptionSelect = viewModel::onLensTypeSelect
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LensOptionSelector(
                        title = "Material",
                        options = listOf("Plastic", "Polycarbonate", "High Index"),
                        selectedOption = state.lensMaterial,
                        onOptionSelect = viewModel::onLensMaterialSelect
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LensOptionSelector(
                        title = "Coating",
                        options = listOf("Anti-Reflective", "Blue Light Filter", "UV Protection", "Photochromic"),
                        selectedOption = state.lensCoating,
                        onOptionSelect = viewModel::onLensCoatingSelect
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Prescription Toggle
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.togglePrescriptionForm() },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "Add Prescription Details", fontWeight = FontWeight.Bold)
                            }
                            Icon(
                                imageVector = if (state.showPrescriptionForm) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    }

                    if (state.showPrescriptionForm) {
                        Spacer(modifier = Modifier.height(16.dp))
                        PrescriptionForm(state, viewModel)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Reviews Section
                    Text(text = "Reviews", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (state.reviews.isEmpty()) {
                        Text("No reviews yet. Be the first to review!", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        state.reviews.forEach { review ->
                            ReviewItem(review)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { viewModel.toggleReviewDialog() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Write a Review")
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    if (state.showReviewDialog) {
        AddReviewDialog(
            onDismiss = { viewModel.toggleReviewDialog() },
            onSubmit = { rating, comment -> viewModel.submitReview(rating, comment) }
        )
    }


    if (showGallery && product != null) {
        FullScreenImageGallery(
            images = product.images,
            initialIndex = state.selectedImageIndex,
            onDismiss = { showGallery = false }
        )
    }

    // Success Animation Dialog
    if (viewModel.addToCartSuccess.value) {
        Dialog(onDismissRequest = { viewModel.resetAddToCartSuccess() }) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OpticalLottieAnimation(
                        rawRes = R.raw.success,
                        size = 120.dp,
                        iterations = 1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Added to Cart!", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // Auto-dismiss after 2 seconds
        LaunchedEffect(key1 = true) {
            delay(2000)
            viewModel.resetAddToCartSuccess()
        }
    }
}
@Composable
fun LensOptionSelector(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelect: (String) -> Unit
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(options) { _, option ->
                val isSelected = selectedOption == option
                Surface(
                    modifier = Modifier
                        .clickable { onOptionSelect(option) }
                        .height(40.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(text = option, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun PrescriptionForm(state: ProductDetailState, viewModel: ProductDetailViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrescriptionField(label = "SPH", value = state.sphere, onValueChange = viewModel::onSphereChange, modifier = Modifier.weight(1f))
            PrescriptionField(label = "CYL", value = state.cylinder, onValueChange = viewModel::onCylinderChange, modifier = Modifier.weight(1f))
            PrescriptionField(label = "Axis", value = state.axis, onValueChange = viewModel::onAxisChange, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrescriptionField(label = "Add", value = state.add, onValueChange = viewModel::onAddChange, modifier = Modifier.weight(1f))
            PrescriptionField(label = "PD", value = state.pupillaryDistance, onValueChange = viewModel::onPdChange, modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = MaterialTheme.shapes.small,
            singleLine = true
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageGallery(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { images.size })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(imageUrl = images[page])
            }

            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            // Page Indicator
            Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(images.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(imageUrl: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 3f)
                    if (scale > 1f) {
                        val maxTranslateX = (size.width * (scale - 1)) / 2
                        val maxTranslateY = (size.height * (scale - 1)) / 2
                        offset = androidx.compose.ui.geometry.Offset(
                            x = (offset.x + pan.x).coerceIn(-maxTranslateX, maxTranslateX),
                            y = (offset.y + pan.y).coerceIn(-maxTranslateY, maxTranslateY)
                        )
                    } else {
                        offset = androidx.compose.ui.geometry.Offset.Zero
                    }
                }
            }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun ReviewItem(review: com.opticalshop.domain.model.Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = review.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(text = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(review.timestamp)), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < review.rating.toInt()) com.opticalshop.presentation.theme.StarYellow else Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AddReviewDialog(onDismiss: () -> Unit, onSubmit: (Float, String) -> Unit) {
    var rating by remember { mutableFloatStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Write a Review") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = com.opticalshop.presentation.theme.StarYellow,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { rating = index + 1f }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = rating > 0f
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
