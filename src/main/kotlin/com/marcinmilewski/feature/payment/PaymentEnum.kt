package com.marcinmilewski.feature.payment

enum class PaymentEnum(
    val priceModifierRange: Pair<Double, Double>,
    val pointsModifier: Double,
    val validationFunction: ((Map<String, Any>) -> Boolean)
) {
    CASH(
        priceModifierRange = Pair(0.9, 1.0),
        pointsModifier = 0.05,
        validationFunction = { it.isEmpty() }
    ),
    CASH_ON_DELIVERY(
        priceModifierRange = Pair(1.0, 1.02),
        pointsModifier = 0.05,
        validationFunction = {
            val courier = it["courier"] as? String
            val isValidCourier = courier == "YAMATO" || courier == "SAGAWA"
            val isValidFieldCount = it.size == 1
            isValidCourier && isValidFieldCount
        }
    ),
    VISA(
        priceModifierRange = Pair(0.95, 1.0),
        pointsModifier = 0.03,
        validationFunction = { validateLast4(it) }
    ),
    MASTERCARD(
        priceModifierRange = Pair(0.95, 1.0),
        pointsModifier = 0.03,
        validationFunction = { validateLast4(it) }
    ),
    AMEX(
        priceModifierRange = Pair(0.98, 1.01),
        pointsModifier = 0.02,
        validationFunction = { validateLast4(it) }
    ),
    JCB(
        priceModifierRange = Pair(0.95, 1.0),
        pointsModifier = 0.05,
        validationFunction = { validateLast4(it) }
    ),
    LINE_PAY(
        priceModifierRange = Pair(1.0, 1.0),
        pointsModifier = 0.01,
        validationFunction = { it.isEmpty() }
    ),
    PAYPAY(
        priceModifierRange = Pair(1.0, 1.0),
        pointsModifier = 0.01,
        validationFunction = { it.isEmpty() }
    ),
    POINTS(
        priceModifierRange = Pair(1.0, 1.0),
        pointsModifier = 0.0,
        validationFunction = { it.isEmpty() }
    ),
    GRAB_PAY(
        priceModifierRange = Pair(1.0, 1.0),
        pointsModifier = 0.01,
        validationFunction = { it.isEmpty() }
    ),
    BANK_TRANSFER(
        priceModifierRange = Pair(1.0, 1.0),
        pointsModifier = 0.0,
        validationFunction = {
            val isValidBankInfo = it.containsKey("bank") && it.containsKey("accountNumber")
            val isValidFieldCount = it.size == 2
            isValidBankInfo && isValidFieldCount
        }
    ),
    CHEQUE(
        priceModifierRange = Pair(0.9, 1.0),
        pointsModifier = 0.0,
        validationFunction = {
            val isValidChequeInfo = it.containsKey("bank") && it.containsKey("chequeNumber")
            val isValidFieldCount = it.size == 2
            isValidChequeInfo && isValidFieldCount
        }
    );

    fun validateAdditionalItems(fields: Map<String, Any>): Boolean {
        return validationFunction.invoke(fields)
    }

    fun validatePriceModifier(modifier: Double): Boolean {
        val (minModifier, maxModifier) = priceModifierRange
        return modifier in minModifier..maxModifier
    }
}

fun validateLast4(it: Map<String, Any>): Boolean {
    val last4 = it["last4"] as? String
    val isValidLast4 = last4?.length == 4 && last4.matches(Regex("\\d{4}"))
    val isValidFieldCount = it.size == 1
    return isValidLast4 && isValidFieldCount
}
