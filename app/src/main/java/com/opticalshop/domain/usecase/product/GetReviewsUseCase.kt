package com.opticalshop.domain.usecase.product

import com.opticalshop.data.repository.ProductRepository
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.model.Review
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReviewsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(productId: String): Flow<Result<List<Review>>> {
        return repository.getReviews(productId)
    }
}
