package com.opticalshop.domain.usecase.product

import com.opticalshop.data.model.Product
import com.opticalshop.data.repository.ProductRepository
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductByIdUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(productId: String): Flow<Result<Product>> {
        return productRepository.getProductById(productId)
    }
}
