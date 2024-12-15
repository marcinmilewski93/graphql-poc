package com.marcinmilewski.feature.payment

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class PaymentDTO(
    val customerId: String,
    val price: BigDecimal,
    val priceModifier: Double,
    val paymentMethod: PaymentEnum,
    val datetime: OffsetDateTime,
    val additionalItem: Map<String, String>
)

data class PaymentResponse(
    val finalPrice: BigDecimal,
    val points: BigDecimal
)

data class Payment(
    val customerId: String,
    val price: BigDecimal,
    val finalPrice: BigDecimal,
    val priceModifier: BigDecimal,
    val points: BigDecimal,
    val paymentMethod: String,
    val datetime: LocalDateTime,
    val additionalItem: String
)
