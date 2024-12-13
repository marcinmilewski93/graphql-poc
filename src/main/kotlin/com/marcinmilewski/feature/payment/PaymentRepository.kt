package com.marcinmilewski.feature.payment

import com.marcinmilewski.db.tables.Payments.Companion.PAYMENTS
import org.jooq.DSLContext
import org.jooq.JSONB
import org.springframework.stereotype.Repository

@Repository
class PaymentRepository(private val dsl: DSLContext) {

    fun save(payment: Payment) {
        dsl.insertInto(PAYMENTS)
            .set(PAYMENTS.CUSTOMER_ID, payment.customerId)
            .set(PAYMENTS.PRICE, payment.price)
            .set(PAYMENTS.FINAL_PRICE, payment.finalPrice)
            .set(PAYMENTS.POINTS, payment.points)
            .set(PAYMENTS.PAYMENT_METHOD, payment.paymentMethod)
            .set(PAYMENTS.DATETIME, payment.datetime)
            .set(PAYMENTS.ADDITIONAL_ITEM, JSONB.jsonb(payment.additionalItem))
            .execute()
    }
}
