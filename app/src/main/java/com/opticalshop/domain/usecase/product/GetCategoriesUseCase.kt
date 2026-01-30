package com.opticalshop.domain.usecase.product

import com.opticalshop.data.model.Category
import com.opticalshop.data.repository.ProductRepository
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<Result<List<Category>>> {
        return productRepository.getCategories()
    }
}
