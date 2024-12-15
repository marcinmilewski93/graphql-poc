package com.marcinmilewski.feature.sale

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class SaleController(private val saleService: SaleService) {

    @QueryMapping
    fun sales(@Argument request: SalesDTO): List<Sale> {
        return saleService.payment(request.startTime, request.endTime)
    }
}
