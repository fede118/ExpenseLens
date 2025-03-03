package com.section11.expenselens.ui.review.validator

open class ExpenseValidationException : RuntimeException()

class InvalidExpenseTotalException : ExpenseValidationException()

class InvalidExpenseCategoryException : ExpenseValidationException()

class InvalidExpenseDateException : ExpenseValidationException()
