package com.section11.expenselens.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.data.dto.FirestoreHousehold.Companion.NAME_FIELD
import com.section11.expenselens.domain.models.Category.HOME
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

private const val HOUSEHOLD_COLLECTION = "households"
private const val EXPENSES_COLLECTION = "expenses"

@ExperimentalCoroutinesApi
class FirestoreExpensesRepositoryTest {

    private lateinit var repository: FirestoreExpensesRepository
    private val mockFirestore: FirebaseFirestore = mock()
    private val mockCollection: CollectionReference = mock()
    private val mockDocument: DocumentReference = mock()

    @Before
    fun setUp() {
        repository = FirestoreExpensesRepository(mockFirestore)
    }

    @Test
    fun `getHousehold returns household ID when found`() = runTest {
        // Given
        val householdName = "Test Household"
        val householdId = "household123"

        // Mock the QuerySnapshot to return a document with the expected ID
        val mockDocumentSnapshot: DocumentSnapshot = mock {
            on { id } doReturn householdId
        }

        val mockQuerySnapshot: QuerySnapshot = mock {
            on { documents } doReturn listOf(mockDocumentSnapshot)
        }

        // Use the actual mockQuerySnapshot in Tasks.forResult()
        val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

        val queryMock: Query = mock()

        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.whereEqualTo(NAME_FIELD, householdName)).thenReturn(queryMock)
        whenever(queryMock.get()).thenReturn(mockTask) // Return the mock task directly

        // When
        val result = repository.getHousehold(householdName)
        advanceUntilIdle()

        // Then
        assertEquals(householdId, result)
    }

    @Test
    fun `getHousehold returns null when household is not found`() = runTest {
        // Given
        val householdName = "Nonexistent Household"

        // Mock an empty QuerySnapshot
        val mockQuerySnapshot: QuerySnapshot = mock {
            on { documents } doReturn emptyList()
        }

        val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

        val queryMock: Query = mock()

        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.whereEqualTo(NAME_FIELD, householdName)).thenReturn(queryMock)
        whenever(queryMock.get()).thenReturn(mockTask) // Return the mock task directly

        // When
        val result = repository.getHousehold(householdName)
        advanceUntilIdle()

        // Then
        assertNull(result)
    }
    @Test
    fun `addExpenseToHousehold returns success when added successfully`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household456"
        val expense = ConsolidatedExpenseInformation(
            total = 100.0,
            category = HOME,
            date = Date(),
            note = "Dinner",
            distributedExpense = mapOf(userId to 50.0)
        )

        // Mock Firestore collection
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.collection(EXPENSES_COLLECTION)).thenReturn(mockCollection)

        // Mock Firestore add() operation
        val mockTask: Task<DocumentReference> = Tasks.forResult(mockDocument)
        whenever(mockCollection.add(any())).thenReturn(mockTask)

        // When
        val result = repository.addExpenseToHousehold(userId, householdId, expense)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `createHousehold returns success with ID and name`() = runTest {
        // Given
        val householdName = "New Household"
        val userId = "user123"
        val householdId = "household456"

        val mockDocument: DocumentReference = mock()
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document()).thenReturn(mockDocument)
        whenever(mockCollection.document(any())).thenReturn(mockDocument)
        whenever(mockDocument.id).thenReturn(householdId)
        whenever(mockDocument.set(any())).thenReturn(Tasks.forResult(null))

        // When
        val result = repository.createHousehold(householdName, userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(householdId to householdName, result.getOrNull())
    }


    @Test
    fun `getAllExpensesFromHousehold returns list of expenses`() = runTest {
        // Given
        val householdId = "household456"
        val expenseList = listOf(
            FirestoreExpense(
                category = HOME.displayName,
                total = 100.0,
                date = Timestamp(Date()),
                userId = "user123",
                note = "Dinner",
                distributedExpense = emptyMap()
            )
        )

        val mockDocumentSnapshot: DocumentSnapshot = mock {
            on { toObject(FirestoreExpense::class.java) } doReturn expenseList[0]
        }

        val mockQuerySnapshot: QuerySnapshot = mock {
            on { documents } doReturn listOf(mockDocumentSnapshot)
        }

        val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.collection(EXPENSES_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.get()).thenReturn(mockTask) // Ensures get() returns mockTask

        // When
        val result = repository.getAllExpensesFromHousehold(householdId)
        advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expenseList, result.getOrNull())
    }

}
