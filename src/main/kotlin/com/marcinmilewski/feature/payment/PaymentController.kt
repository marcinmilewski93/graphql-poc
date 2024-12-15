package com.marcinmilewski.feature.payment

import com.marcinmilewski.config.GraphQLValidationException
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class PaymentController(private val paymentService: PaymentService) {

    @MutationMapping
    fun payment(@Argument request: PaymentDTO): PaymentResponse {
        val paymentMethod = request.paymentMethod
        if (!paymentMethod.validateAdditionalItems(request.additionalItem)) {
            throw GraphQLValidationException("Incorrect additional items")
        }
        if (!paymentMethod.validatePriceModifier(request.priceModifier)) {
            val range = paymentMethod.priceModifierRange
            throw GraphQLValidationException("Price modifier is not in allowed range: ${range.first} to ${range.second}")
        }
        return paymentService.processPayment(request)
    }
}
