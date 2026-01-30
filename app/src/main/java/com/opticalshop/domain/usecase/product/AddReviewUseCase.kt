package com.opticalshop.domain.usecase.product

import com.opticalshop.data.repository.ProductRepository
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.model.Review
import javax.inject.Inject

class AddReviewUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(review: Review): Result<Unit> {
        return repository.addReview(review)
    }
}
