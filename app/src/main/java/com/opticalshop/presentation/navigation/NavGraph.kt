package com.opticalshop.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.opticalshop.presentation.screens.address.AddEditAddressScreen
import com.opticalshop.presentation.screens.address.AddressBookScreen
import com.opticalshop.presentation.screens.auth.login.LoginScreen
import com.opticalshop.presentation.screens.auth.register.RegisterScreen
import com.opticalshop.presentation.screens.cart.CartScreen
import com.opticalshop.presentation.screens.checkout.CheckoutScreen
import com.opticalshop.presentation.screens.home.HomeScreen
import com.opticalshop.presentation.screens.profile.ProfileScreen
import com.opticalshop.presentation.screens.orders.OrderDetailScreen
import com.opticalshop.presentation.screens.orders.OrdersScreen
import com.opticalshop.presentation.screens.product.ProductDetailScreen
import com.opticalshop.presentation.screens.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddresses = { navController.navigate(Screen.AddressBook.route) },
                onNavigateToOrders = { navController.navigate(Screen.Orders.route) },
                onNavigateToWishlist = { navController.navigate(Screen.Wishlist.route) },
                onNavigateToPersonalInfo = { navController.navigate(Screen.PersonalInfo.route) }
            )
        }

        composable(Screen.AddressBook.route) {
            AddressBookScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddAddress = { navController.navigate(Screen.AddEditAddress.route) }
            )
        }

        composable(Screen.AddEditAddress.route) {
            AddEditAddressScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCheckoutClick = {
                    navController.navigate(Screen.Checkout.route)
                }
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = Screen.ProductDetail.arguments
        ) {
            ProductDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                }
            )
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Wishlist.route) {
            com.opticalshop.presentation.screens.wishlist.WishlistScreen(
                onNavigateBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }

        composable(Screen.Orders.route) {
            com.opticalshop.presentation.screens.orders.OrdersScreen(
                onNavigateBack = { navController.popBackStack() },
                onOrderClick = { orderId: String ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                }
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType }
            )
        ) {
            com.opticalshop.presentation.screens.orders.OrderDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PersonalInfo.route) {
            com.opticalshop.presentation.screens.personal_info.PersonalInfoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
