package com.andyl.esme.domain.helper

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun evaluateSimpleMath(expr: String): String {
    val cleanExpr = expr.replace("(", "").replace(")", "").replace("=", "").trim()

    return try {
        val parts = cleanExpr.split(Regex("(?<=[\\+\\-\\*\\/])|(?=[\\+\\-\\*\\/])"))
        if (parts.size < 3) return expr

        val num1 = parts[0].trim().toDouble()
        val op = parts[1].trim()
        val num2 = parts[2].trim().toDouble()

        val result = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*" -> num1 * num2
            "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
            else -> return expr
        }

        if (result % 1 == 0.0) result.toInt().toString() else result.toString()
    } catch (e: Exception) {
        "?"
    }
}

fun processSmartTokens(input: String): String {
    var output = input

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    val tokenMap = mapOf(
        "//hoy" to "${now.day}/${now.month.number}",
        "//hora" to "${now.hour}:${now.minute.toString().padStart(2, '0')}",
        "//ahora" to "${now.hour}:${now.minute.toString().padStart(2, '0')}",
        "//año" to now.year.toString(),
        "//mes" to now.month.number.toString(),
        "//dia" to now.day.toString()
    )

    tokenMap.forEach { (token, value) ->
        val regex = Regex("${Regex.escape(token)}(?=\\s|\$)")
        output = output.replace(regex, "'$value'")
    }

    // --- CALCULADORA (la dejamos como está) ---
    val calcRegex = Regex("""\(([\d\. ]+[\+\-\*\/][\d\. ]+)\)=""")
    calcRegex.findAll(output).forEach { match ->
        val expression = match.groups[1]?.value ?: ""
        val result = evaluateSimpleMath(expression)
        output = output.replace(match.value, result)
    }

    return output
}