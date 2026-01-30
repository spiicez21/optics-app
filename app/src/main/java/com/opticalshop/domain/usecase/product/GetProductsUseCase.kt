package com.opticalshop.domain.usecase.product

import com.opticalshop.data.model.Product
import com.opticalshop.data.repository.ProductRepository
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<Result<List<Product>>> {
        return productRepository.getProducts()
    }
}
