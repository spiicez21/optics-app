package com.opticalshop.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.opticalshop.presentation.components.CategoryChip
import com.opticalshop.presentation.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Optical Shop", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading && state.categories.isEmpty() && state.featuredProducts.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (state.error != null && state.categories.isEmpty()) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Categories Section
                if (state.categories.isNotEmpty()) {
                    item {
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.categories) { category ->
                                CategoryChip(
                                    category = category,
                                    onCategoryClick = { /* Handle category click */ }
                                )
                            }
                        }
                    }
                }

                // Featured Products Section
                if (state.featuredProducts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Featured Products",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(state.featuredProducts.chunked(2)) { list ->
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                            list.forEach { product ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductCard(
                                        product = product,
                                        onProductClick = onProductClick
                                    )
                                }
                            }
                            if (list.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
