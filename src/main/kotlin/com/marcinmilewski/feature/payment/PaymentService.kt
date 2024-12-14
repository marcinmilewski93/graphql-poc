package com.marcinmilewski.feature.payment

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class PaymentService(private val paymentRepository: PaymentRepository) {

    fun processPayment(paymentDTO: PaymentDTO): PaymentResponse {
        val paymentMethod = paymentDTO.paymentMethod
        val priceModifier = BigDecimal.valueOf(paymentDTO.priceModifier)
        val finalPrice = paymentDTO.price.multiply(priceModifier).setScale(2, RoundingMode.UP)
        val pointsModifier = BigDecimal.valueOf(paymentMethod.pointsModifier)
        val points = paymentDTO.price.multiply(pointsModifier).setScale(0, RoundingMode.DOWN)
        val payment = Payment(
            customerId = paymentDTO.customerId,
            price = paymentDTO.price,
            finalPrice = finalPrice,
            priceModifier = priceModifier,
            points = points,
            paymentMethod = paymentMethod.name,
            datetime = paymentDTO.datetime.toLocalDateTime(),
            additionalItem = ObjectMapper().writeValueAsString(paymentDTO.additionalItem)
        )
        paymentRepository.save(payment)
        return PaymentResponse(finalPrice, points)
    }
}
