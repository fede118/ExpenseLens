package com.section11.expenselens.domain.exceptions

private const val NULL_BODY_ERROR = "Response body is null"
private const val API_ERROR = "API Error:"
private const val INVALID_CREDENTIALS = "Invalid credentials"
private const val INVALID_CREDENTIALS_TYPE = "Invalid credentials type"
private const val NO_HOUSEHOLD_IN_FIRESTONE = "No household in firestone with that name"
private const val FIELD_WAS_NULL_MESSAGE = "A mandatory field for firebase was null"
private const val USER_NOT_FOUND = "The provided userId was not found in firestore"

/**
 * Exception representing an API error.
 */
class ApiErrorException(
    responseCode: Int,
    errorBody: String?
) : RuntimeException("$API_ERROR $responseCode - ${errorBody ?: NULL_BODY_ERROR}")

/**
 * Exception representing a network error where the body is null.
 */
class ResponseBodyNullException(message: String = NULL_BODY_ERROR) : RuntimeException(message)

class ExpenseInformationNotFoundException(message: String = NULL_BODY_ERROR) : RuntimeException(message)

class InvalidCredentialException(message: String = INVALID_CREDENTIALS): RuntimeException(message)

class InvalidCredentialTypeException(message: String = INVALID_CREDENTIALS_TYPE): RuntimeException(message)

class HouseholdNotFoundException(message: String = NO_HOUSEHOLD_IN_FIRESTONE): RuntimeException(message)

class IllegalUserInfoException(message: String) : RuntimeException(message)

class UserNotFoundException(message: String = USER_NOT_FOUND) : RuntimeException(message)

class NullFieldOnFirebaseException(message: String = FIELD_WAS_NULL_MESSAGE) : RuntimeException(message)
