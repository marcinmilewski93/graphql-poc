package com.marcinmilewski.feature.sale

import java.math.BigDecimal
import java.time.OffsetDateTime

data class SalesDTO(
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime
)

data class Sale(
    val datetime: OffsetDateTime,
    val sales: BigDecimal,
    val points: BigDecimal
)
