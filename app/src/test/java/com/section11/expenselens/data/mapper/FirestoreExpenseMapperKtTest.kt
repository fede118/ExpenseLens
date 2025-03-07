package com.section11.expenselens.data.mapper

import com.google.firebase.Timestamp
import com.section11.expenselens.data.dto.FirestoreExpense
import junit.framework.TestCase.assertEquals
import org.junit.Test

class FirestoreExpenseMapperKtTest {

    @Test
    fun `toDomainExpense should return Expense`() {
        // Given
        val firestoreExpense = FirestoreExpense(
            category = "category",
            total = 100.0,
            timestamp = Timestamp.now(),
            userId = "userId",
            userDisplayName = "userDisplayName",
            note = "note",
            distributedExpense = emptyMap()
        )

        // When
        val result = firestoreExpense.toDomainExpense()

        // Then
        assertEquals(firestoreExpense.category, result.category)
        assertEquals(firestoreExpense.total, result.total)
        assertEquals(firestoreExpense.timestamp.toDate(), result.date)
        assertEquals(firestoreExpense.userId, result.userId)
        assertEquals(firestoreExpense.userDisplayName, result.userDisplayName)
        assertEquals(firestoreExpense.note, result.note)
        assertEquals(firestoreExpense.distributedExpense, result.distributedExpense)
    }
}
