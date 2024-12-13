package com.marcinmilewski.feature.sale

import com.marcinmilewski.config.PostgresContextInitializer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@Transactional
@ExtendWith(SpringExtension::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [PostgresContextInitializer::class])
class SaleRepositoryIT(
    @Autowired private val saleRepository: SaleRepository
) {

    @Sql(
        statements = ["""
    INSERT INTO payments ( customer_id, price, final_price, points, payment_method, datetime, additional_item)
    VALUES  ( '432', 100.00, 95.00, 5.00, 'MASTERCARD', '2022-12-01T00:00:00Z', '{"last4": "1234"}'),
            ( '12', 50.00, 90.00, 3.00, 'PREPAID', '2022-12-01T00:31:00Z', '{}'),
            ( '5', 50.00, 90.00, 3.00, 'VISA', '2023-10-01T00:31:00Z', '{"last4": "1234"}'),
            ( '91', 10.00, 4.00, 2.00, 'CASH', '2024-12-01T00:31:00Z', '{}');
        """], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Test
    fun shouldRetrievePaymentsInTheRangeBrokenIntoHours() {
        // given
        val start = LocalDateTime.parse("2022-01-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2023-12-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
        // when
        val sales = saleRepository.getSalesInTimeRange(start, end)
        // then
        assertEquals(2, sales.size)
        val firstHour = sales[0]
        assertEquals(OffsetDateTime.parse("2022-12-01T00:00:00Z"), firstHour.datetime)
        assertEquals(BigDecimal("185.00"), firstHour.sales)
        assertEquals(BigDecimal("8.00"), firstHour.points)
        val secondHour = sales[1]
        assertEquals(OffsetDateTime.parse("2023-10-01T00:00:00Z"), secondHour.datetime)
        assertEquals(BigDecimal("90.00"), secondHour.sales)
        assertEquals(BigDecimal("3.00"), secondHour.points)
    }

    @Test
    fun shouldRetrieveEmptyListIfNoPaymentInTheRange() {
        // given
        val start = LocalDateTime.parse("2022-01-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2023-12-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
        // when
        val sales = saleRepository.getSalesInTimeRange(start, end)
        // then
        assertEquals(0, sales.size)
    }
}
