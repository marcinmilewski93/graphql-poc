package com.marcinmilewski.feature.sale

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.graphql.spring.boot.test.GraphQLTestTemplate
import com.marcinmilewski.config.PostgresContextInitializer
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [PostgresContextInitializer::class])
class SaleControllerIT(
    @Autowired private val graphQlTest: GraphQLTestTemplate,
    @SpykBean private val salesService: SaleService
) {

    @SqlGroup(
        value = [
            Sql(
                statements = ["""
            INSERT INTO payments ( customer_id, price, final_price, points, payment_method, datetime, additional_item)
            VALUES  ( '432', 100.00, 95.00, 5.00, 'MASTERCARD', '2022-12-01T00:00:00Z', '{"last4": "1234"}'),
                    ( '12', 50.00, 90.00, 3.00, 'PREPAID', '2022-12-01T00:31:00Z', '{}'),
                    ( '5', 50.00, 90.00, 3.00, 'VISA', '2023-10-01T00:31:00Z', '{"last4": "1234"}'),
                    ( '91', 10.00, 4.00, 2.00, 'CASH', '2024-12-01T00:31:00Z', '{}');
            """], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
            ),
            Sql(
                statements = ["""
            DELETE FROM payments WHERE customer_id IN ('432', '12', '5', '91');
            """], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
            )
        ]
    )
    @Test
    fun shouldRetrievePaymentsBrokenIntoHours() {
        // given
        val query = """
            query Sales {
              sales(
                request: {startTime: "2021-09-01T00:00:00Z", endTime: "2024-09-01T00:00:00Z"}
              ) {
                datetime
                sales
                points
              }
            }
        """.trimIndent()
        // when
        val response = graphQlTest.postForString(query)
        // then
        assertTrue(response.isOk)
        val expectedResponse = """
            {
              "data" : {
                "sales" : [ {
                  "datetime" : "2022-12-01T00:00:00.000Z",
                  "sales" : "185.00",
                  "points" : 8
                }, {
                  "datetime" : "2023-10-01T00:00:00.000Z",
                  "sales" : "90.00",
                  "points" : 3
                } ]
              }
            }""".trimIndent()
        assertEquals(ObjectMapper().readTree(expectedResponse), response.readTree())
    }

    @Test
    fun shouldRetrieveEmptyListIfNoRecordsWereFound() {
        // given
        val query = """
            query Sales {
              sales(
                request: {startTime: "2021-09-01T00:00:00Z", endTime: "2024-09-01T00:00:00Z"}
              ) {
                datetime
                sales
                points
              }
            }
        """.trimIndent()
        // when
        val response = graphQlTest.postForString(query)
        // then
        assertTrue(response.isOk)
        val expectedResponse = """
            {
              "data" : {
                "sales" : []
              }
            }""".trimIndent()
        assertEquals(ObjectMapper().readTree(expectedResponse), response.readTree())
    }

    @Test
    fun shouldRetrieveMeaningfulErrorIfValidationFailed() {
        // given
        val query = """
            query Sales {
              sales(
                request: {startTime: "2021-09-01T00:00:00Z", endTime: "INVALID"}
              ) {
                datetime
                sales
                points
              }
            }
        """.trimIndent()
        // when
        val response = graphQlTest.postForString(query)
        // then
        assertTrue(response.isOk)
        val expectedResponse = """{
              "errors": [
                {
                  "message": "Validation error (WrongType@[sales]) : argument 'request.endTime' with value 'StringValue{value='INVALID'}' is not a valid 'DateTime' - Invalid RFC3339 value : 'INVALID'. because of : 'Text 'INVALID' could not be parsed at index 0'",
                  "locations": [
                    {
                      "line": 3,
                      "column": 5
                    }
                  ],
                  "extensions": {
                    "classification": "ValidationError"
                  }
                }
              ]
            }""".trimIndent()
        assertEquals(ObjectMapper().readTree(expectedResponse), response.readTree())
    }

    @Test
    fun shouldReturnInternalServerErrorIfServiceThrowsIllegalArgumentException() {
        // given
        every { salesService.payment(any(), any()) } throws IllegalArgumentException()
        val query = """
            query Sales {
              sales(
                request: {startTime: "2020-09-01T00:00:00Z", endTime: "2020-09-01T00:00:00Z"}
              ) {
                datetime
                sales
                points
              }
            }
        """.trimIndent()
        // when
        val response = graphQlTest.postForString(query)
        // then
        assertTrue(response.isOk)
        val simplifiedResponse = response.readTree()
            .apply { (get("errors")[0] as ObjectNode).put("message", "INTERNAL_ERROR") }
        val expectedResponse = """{
              "errors": [
                {
                  "message": "INTERNAL_ERROR",
                  "locations": [
                    {
                      "line": 2,
                      "column": 3
                    }
                  ],
                  "path": [
                    "sales"
                  ],
                  "extensions": {
                    "classification": "INTERNAL_ERROR"
                  }
                }
              ],
              "data": null
            }""".trimIndent()
        assertEquals(ObjectMapper().readTree(expectedResponse), simplifiedResponse)
    }
}
