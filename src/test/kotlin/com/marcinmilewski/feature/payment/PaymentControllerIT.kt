package com.marcinmilewski.feature.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.graphql.spring.boot.test.GraphQLTestTemplate
import com.marcinmilewski.config.PostgresContextInitializer
import com.marcinmilewski.db.tables.Payments.Companion.PAYMENTS
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertNotNull

@ExtendWith(SpringExtension::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [PostgresContextInitializer::class])
class SaleControllerIT(
    @Autowired private val graphQlTest: GraphQLTestTemplate,
    @Autowired private val dslContext: DSLContext
) {

    @Sql(
        statements = ["""DELETE FROM payments WHERE customer_id IN ('12345', '9841');"""],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @EnumSource(PaymentEnum::class)
    @ParameterizedTest
    fun shouldRetrievePaymentsBrokenIntoHours(payment: PaymentEnum) {
        // given
        val mutation = stringFromFile("payments/valid/${payment}.gql")
        // when
        val response = graphQlTest.postForString(mutation)
        // then
        assertTrue(response.isOk)
        val expectedResponse = stringFromFile("payments/valid/${payment}-response.json")
        assertEquals(ObjectMapper().readTree(expectedResponse), response.readTree())
    }

    private fun stringFromFile(path: String): String {
        val resource = DefaultResourceLoader().getResource("classpath:$path")
        return Files.readString(Paths.get(resource.uri))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "AMEX-last4-with-3digits",
            "BANK_TRANSFER-missing-accountNumber",
            "BANK_TRANSFER-missing-bank",
            "CASH-with-additionalItem",
            "CASH_ON_DELIVERY-invalid-courier",
            "CASH_ON_DELIVERY-missing-courier",
            "CASH_ON_DELIVERY-with-AdditionalItem",
            "CHEQUE-missing-chequeNumber",
            "CHEQUE-missing-bank",
            "JCB-missing-last4",
            "MASTERCARD-letters-in-last4",
            "VISA-no-last4-field"
        ]
    )
    fun shouldValidateAdditionalItems(filename: String) {
        // given
        val mutation = stringFromFile("payments/invalid/${filename}.gql")
        // when
        val response = graphQlTest.postForString(mutation)
        // then
        assertTrue(response.isOk)
        val expectedResponse = """{
              "errors": [
                {
                  "message": "Incorrect additional items",
                  "locations": [],
                  "path": [
                    "payment"
                  ],
                  "extensions": {
                    "classification": "InvalidSyntax"
                  }
                }
              ],
              "data": null
            }"""
        assertEquals(ObjectMapper().readTree(expectedResponse), response.readTree())
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "CASH-modifier-above-limit",
            "CASH-modifier-below-limit",
        ]
    )
    fun shouldValidatePriceModifierRange(filename: String) {
        // given
        val mutation = stringFromFile("payments/invalid/${filename}.gql")
        // when
        val response = graphQlTest.postForString(mutation)
        // then
        assertTrue(response.isOk)
        val expectedResponse = """{
              "errors": [
                {
                  "message": "Price modifier is not in allowed range: 0.9 to 1.0",
                  "locations": [],
                  "path": [
                    "payment"
                  ],
                  "extensions": {
                    "classification": "InvalidSyntax"
                  }
                }
              ],
              "data": null
            }"""
        assertEquals(ObjectMapper().readTree(expectedResponse), response.readTree())
    }

    @Test
    fun shouldNotAllowInvalidPaymentMethod() {
        // given
        val mutation = stringFromFile("payments/invalid/INVALID_TYPE.gql")
        // when
        val response = graphQlTest.postForString(mutation)
        // then
        assertTrue(response.isOk)
        val expectedResponse = """{
              "errors": [
                {
                  "message": "Field error in object 'paymentDTO' on field '${'$'}.paymentMethod': rejected value [INVALID_TYPE]; codes [typeMismatch.paymentDTO,typeMismatch]; arguments []; default message [Failed to convert argument value]",
                  "locations": [],
                  "path": [
                    "payment"
                  ],
                  "extensions": {
                    "classification": "InvalidSyntax"
                  }
                }
              ],
              "data": null
            }"""
        assertEquals(ObjectMapper().readTree(expectedResponse), response.readTree())
    }

    @Sql(
        statements = ["""DELETE FROM payments WHERE customer_id IN ('9841');"""],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Test
    fun shouldInsertPaymentIntoDatabase() {
        // given
        val mutation = stringFromFile("payments/valid/MASTERCARD.gql")
        // when
        val response = graphQlTest.postForString(mutation)
        // then
        assertTrue(response.isOk)
        val expectedResponse = stringFromFile("payments/valid/MASTERCARD-response.json")
        assertEquals(ObjectMapper().readTree(expectedResponse), response.readTree())
        val dbPayment = dslContext.selectFrom(PAYMENTS).where(PAYMENTS.CUSTOMER_ID.eq("9841")).fetchSingle()
        assertNotNull(dbPayment.id)
        assertEquals("9841", dbPayment.customerId)
        assertEquals(BigDecimal("200.50"), dbPayment.price)
        assertEquals(BigDecimal("190.48"), dbPayment.finalPrice)
        assertEquals(BigDecimal("6.00"), dbPayment.points)
        assertEquals("MASTERCARD", dbPayment.paymentMethod)
        assertEquals(LocalDateTime.parse("2022-03-01T01:00:00Z", DateTimeFormatter.ISO_DATE_TIME), dbPayment.datetime)
        assertEquals("""{"last4": "5678"}""", dbPayment.additionalItem!!.data())
        assertNotNull(dbPayment.createdAt)
        assertNotNull(dbPayment.updatedAt)
    }
}
