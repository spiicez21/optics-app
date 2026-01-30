package com.opticalshop.di

import com.opticalshop.data.repository.AuthRepository
import com.opticalshop.data.repository.AuthRepositoryImpl
import com.opticalshop.data.repository.CartRepository
import com.opticalshop.data.repository.CartRepositoryImpl
import com.opticalshop.data.repository.OrderRepository
import com.opticalshop.data.repository.OrderRepositoryImpl
import com.opticalshop.data.repository.ProductRepository
import com.opticalshop.data.repository.ProductRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository
}
