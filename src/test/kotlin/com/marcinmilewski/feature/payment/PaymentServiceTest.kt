package com.marcinmilewski.feature.payment

import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@ExtendWith(MockKExtension::class)
class PaymentServiceTest(@MockK private val paymentRepository: PaymentRepository) {

    private val paymentService = PaymentService(paymentRepository)

    @Test
    fun `should save payment with correct values`() {
        // given
        justRun { paymentRepository.save(any()) }
        val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val paymentDTO = PaymentDTO(
            customerId = "12345",
            price = BigDecimal("100.30"),
            priceModifier = 1.01,
            paymentMethod = PaymentEnum.CASH_ON_DELIVERY,
            datetime = OffsetDateTime.parse("2022-09-01T00:00:00Z", dateTimeFormatter),
            additionalItem = mapOf("courier" to "YAMATO")
        )
        // when
        val payment = paymentService.processPayment(paymentDTO)
        // then
        assertEquals(BigDecimal("101.31"), payment.finalPrice)
        assertEquals(BigDecimal("5"), payment.points)
        val slot = slot<Payment>()
        verify { paymentRepository.save(capture(slot)) }
        val captured = slot.captured
        assertEquals("12345", captured.customerId)
        assertEquals(BigDecimal("100.30"), captured.price)
        assertEquals(BigDecimal("101.31"), captured.finalPrice)
        assertEquals(BigDecimal("1.01"), captured.priceModifier)
        assertEquals(BigDecimal("5"), captured.points)
        assertEquals("CASH_ON_DELIVERY", captured.paymentMethod)
        assertEquals(LocalDateTime.parse("2022-09-01T00:00:00Z", dateTimeFormatter), captured.datetime)
        assertEquals("""{"courier":"YAMATO"}""", captured.additionalItem)
    }
}
