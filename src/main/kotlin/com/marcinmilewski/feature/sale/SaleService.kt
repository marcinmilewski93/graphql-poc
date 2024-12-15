package com.marcinmilewski.feature.sale

import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class SaleService(private val saleRepository: SaleRepository) {

    fun payment(start: OffsetDateTime, end: OffsetDateTime): List<Sale> {
        return saleRepository.getSalesInTimeRange(start.toLocalDateTime(), end.toLocalDateTime())
    }
}
