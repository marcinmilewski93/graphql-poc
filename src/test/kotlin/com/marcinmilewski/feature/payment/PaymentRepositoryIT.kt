package com.marcinmilewski.feature.payment

import com.marcinmilewski.config.PostgresContextInitializer
import com.marcinmilewski.db.tables.Payments.Companion.PAYMENTS
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertNotNull

@Transactional
@ExtendWith(SpringExtension::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [PostgresContextInitializer::class])
class PaymentRepositoryIT(
    @Autowired private val dslContext: DSLContext,
    @Autowired private val paymentRepository: PaymentRepository
) {

    @Test
    fun shouldCreateNewPaymentEntityWithAllRequiredFields() {
        // given
        val payment = Payment(
            customerId = "12345",
            price = BigDecimal("100.00"),
            finalPrice = BigDecimal("95.00"),
            priceModifier = BigDecimal("0.95"),
            points = BigDecimal("5.00"),
            paymentMethod = "MASTERCARD",
            datetime = LocalDateTime.parse("2022-09-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
            additionalItem = """{"last4": "1234"}"""
        )
        // when
        paymentRepository.save(payment)
        // then
        val dbPayment = dslContext.selectFrom(PAYMENTS).where(PAYMENTS.CUSTOMER_ID.eq("12345")).fetchSingle()
        assertNotNull(dbPayment.id)
        assertEquals(payment.customerId, dbPayment.customerId)
        assertEquals(payment.price, dbPayment.price)
        assertEquals(payment.finalPrice, dbPayment.finalPrice)
        assertEquals(payment.points, dbPayment.points)
        assertEquals(payment.paymentMethod, dbPayment.paymentMethod)
        assertEquals(payment.datetime, dbPayment.datetime)
        assertEquals(payment.additionalItem, dbPayment.additionalItem!!.data())
        assertNotNull(dbPayment.createdAt)
        assertNotNull(dbPayment.updatedAt)
    }
}
