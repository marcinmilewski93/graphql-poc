package com.marcinmilewski.feature.sale

import com.marcinmilewski.db.tables.Payments.Companion.PAYMENTS
import org.jooq.DSLContext
import org.jooq.DatePart
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneOffset

@Repository
class SaleRepository(private val dslContext: DSLContext) {

    fun getSalesInTimeRange(start: LocalDateTime, end: LocalDateTime): List<Sale> {
        return dslContext
            .select(
                DSL.trunc(PAYMENTS.DATETIME, DatePart.HOUR).`as`(PAYMENTS.DATETIME),
                DSL.sum(PAYMENTS.FINAL_PRICE).`as`(PAYMENTS.FINAL_PRICE),
                DSL.sum(PAYMENTS.POINTS).`as`(PAYMENTS.POINTS)
            )
            .from(PAYMENTS)
            .where(PAYMENTS.DATETIME.between(start, end))
            .groupBy(DSL.trunc(PAYMENTS.DATETIME, DatePart.HOUR))
            .fetch {
                Sale(
                    datetime = it[PAYMENTS.DATETIME]!!.atOffset(ZoneOffset.UTC),
                    sales = it[PAYMENTS.FINAL_PRICE]!!,
                    points = it[PAYMENTS.POINTS]!!
                )
            }
    }
}
