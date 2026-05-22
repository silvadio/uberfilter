package com.uberfilter.domain

object FinanceValidator {

    fun validateAmount(text: String): Result<Double> {
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Valor obrigatório"))
        }
        val value = text.replace(',', '.').toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("Valor inválido"))
        if (value <= 0.0) {
            return Result.failure(IllegalArgumentException("Valor deve ser maior que zero"))
        }
        return Result.success(value)
    }

    fun validateDescription(text: String): Result<String> {
        if (text.length > 200) {
            return Result.failure(IllegalArgumentException("Descrição muito longa (máx. 200 caracteres)"))
        }
        return Result.success(text)
    }

    fun validateDate(millis: Long): Result<Long> {
        val now = System.currentTimeMillis()
        if (millis > now + 86_400_000) { // tolerância de 1 dia
            return Result.failure(IllegalArgumentException("Data não pode ser futura"))
        }
        return Result.success(millis)
    }
}
