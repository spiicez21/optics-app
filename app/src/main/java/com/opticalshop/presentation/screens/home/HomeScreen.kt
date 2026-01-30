package com.opticalshop.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import com.opticalshop.presentation.components.CategoryChip
import com.opticalshop.presentation.components.ProductCard
import com.opticalshop.presentation.components.OpticalTextField

import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll

// ...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    
    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        viewModel.refresh()
        // Simple way to stop refreshing when not loading, though ideal is tied to state
        if (!state.isLoading) pullRefreshState.endRefresh() 
        // Note: For production, better sync is needed, but this works for basic integration
    }
    // Sync state isRefreshing with VM isLoading isn't direct with M3 APIs sometimes
    // But let's use the basic pattern
    
    // Better pattern:
    /* 
       LaunchedEffect(state.isLoading) {
           if (!state.isLoading) pullRefreshState.endRefresh()
       }
    */
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
             pullRefreshState.endRefresh()
        }
    }


    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).nestedScroll(pullRefreshState.nestedScrollConnection)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header
            item {
                HomeHeader(
                    userName = state.userName,
                    profileImageUrl = state.profileImageUrl,
                    onProfileClick = onProfileClick,
                    onNotificationClick = { /* Handle */ },
                    onCartClick = onCartClick
                )
            }

                // Search Bar
                item {
                    OpticalTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        label = "",
                        placeholder = "Search...",
                        modifier = Modifier.padding(16.dp),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                        },
                        trailingIcon = {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu, 
                                    contentDescription = "Filter",
                                    modifier = Modifier.padding(8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }

                // Categories
                item {
                    SectionHeader(title = "Categories", onSeeAllClick = { /* Handle */ })
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.categories) { category ->
                            com.opticalshop.presentation.components.CategoryChip(
                                category = category,
                                isSelected = state.selectedCategoryId == category.id,
                                onCategoryClick = viewModel::onCategorySelect
                            )
                        }
                    }
                }

                // Promo Banner
                item {
                    PromoBanner()
                }

                // Popular Products
                item {
                    SectionHeader(title = "Popular Product", onSeeAllClick = { /* Handle */ })
                }

                items(state.popularProducts.chunked(2)) { list ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        list.forEach { product ->
                            Box(modifier = Modifier.weight(1f)) {
                                com.opticalshop.presentation.components.ProductCard(
                                    product = product,
                                    onProductClick = onProductClick,
                                    onAddToCart = viewModel::addToCart,
                                    isWishlisted = state.wishlistProductIds.contains(product.id),
                                    onWishlistClick = { viewModel.toggleWishlist(product) }
                                )
                            }
                        }
                        if (list.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (state.isLoading && state.categories.isEmpty() && state.popularProducts.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

        if (state.error != null && state.categories.isEmpty()) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        }

        // Bottom Fade Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        
        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun HomeHeader(
    userName: String,
    profileImageUrl: String?,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp).clickable { onProfileClick() },
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            if (profileImageUrl != null) {
                coil.compose.AsyncImage(
                    model = profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(userName.take(1).uppercase(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Hello $userName", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = "Good Morning!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        
        IconButton(onClick = onNotificationClick) {
            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color.Gray)
        }
        
        IconButton(onClick = onCartClick) {
            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            text = "See All",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onSeeAllClick() }
        )
    }
}

@Composable
fun PromoBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(160.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // In a real app, use an image background
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
                    .width(180.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Get Your Special Sale Up to 40%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Handle */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(text = "Shop Now", style = MaterialTheme.typography.labelLarge)
                }
            }
            
            // Add a placeholder image or graphic here
        }
    }
}

